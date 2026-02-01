package org.plugin.gatetools.service;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.config.ConfigManager;
import org.plugin.gatetools.model.Gate;
import org.plugin.gatetools.model.GateCondition;

import java.util.logging.Level;

/**
 * 条件验证服务类
 * 负责验证和处理传送门的各种条件
 * 
 * @author NSrank, Augment
 */
public class ConditionService {
    private final GateTools plugin;
    private final ConfigManager configManager;
    private Economy economy;
    private final EconomyTransferService transferService;
    
    public ConditionService(GateTools plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        setupEconomy();
        this.transferService = new EconomyTransferService(plugin, configManager, economy);
    }
    
    /**
     * 设置经济系统
     */
    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("未找到Vault插件，金钱相关功能将不可用");
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("未找到经济插件，金钱相关功能将不可用");
            return;
        }
        
        economy = rsp.getProvider();
        plugin.getLogger().info("已连接到经济系统: " + economy.getName());
    }
    
    /**
     * 条件检查结果类
     */
    public static class ConditionResult {
        private final boolean success;
        private final String errorMessage;
        
        public ConditionResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public static ConditionResult success() {
            return new ConditionResult(true, null);
        }
        
        public static ConditionResult failure(String errorMessage) {
            return new ConditionResult(false, errorMessage);
        }
        
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * 检查玩家是否满足传送门的所有条件
     * 
     * @param player 玩家
     * @param gate 传送门
     * @return 检查结果
     */
    public ConditionResult checkConditions(Player player, Gate gate) {
        for (GateCondition condition : gate.getConditions().values()) {
            ConditionResult result = checkSingleCondition(player, condition);
            if (!result.isSuccess()) {
                return result;
            }
        }
        return ConditionResult.success();
    }
    
    /**
     * 检查单个条件
     * 
     * @param player 玩家
     * @param condition 条件
     * @return 检查结果
     */
    private ConditionResult checkSingleCondition(Player player, GateCondition condition) {
        switch (condition.getConditionType()) {
            case EXPERIENCE:
                return checkExperienceCondition(player, condition);
            case PERMISSION:
                return checkPermissionCondition(player, condition);
            case MONEY:
                return checkMoneyCondition(player, condition);
            case TELEPORT:
                // 传送条件不需要检查，只是存储目标位置
                return ConditionResult.success();
            default:
                return ConditionResult.failure("未知的条件类型");
        }
    }
    
    /**
     * 检查经验条件
     */
    private ConditionResult checkExperienceCondition(Player player, GateCondition condition) {
        try {
            int requiredLevel = Integer.parseInt(condition.getValue());
            int playerLevel = player.getLevel();
            
            if (condition.getJudgeType() == GateCondition.JudgeType.SET) {
                boolean result = compareValues(playerLevel, requiredLevel, condition.getCompareOperator());
                if (!result) {
                    String message = configManager.getMessage("condition.experience-insufficient")
                            .replace("%message_need_experience%", String.valueOf(requiredLevel))
                            .replace("%current_experience%", String.valueOf(playerLevel));
                    return ConditionResult.failure(message);
                }
            }
            // COST类型的经验条件在applyCosts中处理
            
            return ConditionResult.success();
        } catch (NumberFormatException e) {
            return ConditionResult.failure("无效的经验等级配置");
        }
    }
    
    /**
     * 检查权限条件
     */
    private ConditionResult checkPermissionCondition(Player player, GateCondition condition) {
        String permission = condition.getValue();
        boolean hasPermission = player.hasPermission(permission);
        
        if (condition.getCompareOperator() == GateCondition.CompareOperator.EQUAL && !hasPermission) {
            String message = configManager.getMessage("condition.permission-denied")
                    .replace("%message_need_permission%", permission);
            return ConditionResult.failure(message);
        } else if (condition.getCompareOperator() == GateCondition.CompareOperator.NOT_EQUAL && hasPermission) {
            String message = configManager.getMessage("condition.permission-denied")
                    .replace("%message_need_permission%", permission);
            return ConditionResult.failure(message);
        }
        
        return ConditionResult.success();
    }
    
    /**
     * 检查金钱条件
     */
    private ConditionResult checkMoneyCondition(Player player, GateCondition condition) {
        if (economy == null) {
            return ConditionResult.failure("经济系统未启用");
        }
        
        try {
            double requiredMoney = Double.parseDouble(condition.getValue());
            double playerMoney = economy.getBalance(player);
            
            if (condition.getJudgeType() == GateCondition.JudgeType.SET) {
                boolean result = compareValues(playerMoney, requiredMoney, condition.getCompareOperator());
                if (!result) {
                    String message = configManager.getMessage("condition.money-insufficient")
                            .replace("%message_need_money%", economy.format(requiredMoney))
                            .replace("%current_money%", economy.format(playerMoney));
                    return ConditionResult.failure(message);
                }
            } else if (condition.getJudgeType() == GateCondition.JudgeType.COST) {
                if (playerMoney < requiredMoney) {
                    String message = configManager.getMessage("condition.money-insufficient")
                            .replace("%message_need_money%", economy.format(requiredMoney))
                            .replace("%current_money%", economy.format(playerMoney));
                    return ConditionResult.failure(message);
                }
            }
            
            return ConditionResult.success();
        } catch (NumberFormatException e) {
            return ConditionResult.failure("无效的金钱数额配置");
        }
    }
    
    /**
     * 比较数值
     */
    private boolean compareValues(double playerValue, double requiredValue, GateCondition.CompareOperator operator) {
        switch (operator) {
            case EQUAL:
                return playerValue == requiredValue;
            case NOT_EQUAL:
                return playerValue != requiredValue;
            case GREATER:
                return playerValue > requiredValue;
            case GREATER_EQUAL:
                return playerValue >= requiredValue;
            case LESS:
                return playerValue < requiredValue;
            case LESS_EQUAL:
                return playerValue <= requiredValue;
            default:
                return false;
        }
    }
    
    /**
     * 应用传送费用（扣除经验、金钱等）
     * 
     * @param player 玩家
     * @param gate 传送门
     */
    public void applyCosts(Player player, Gate gate) {
        for (GateCondition condition : gate.getConditions().values()) {
            if (condition.getJudgeType() == GateCondition.JudgeType.COST) {
                applySingleCost(player, condition);
            }
        }
    }
    
    /**
     * 应用单个费用
     */
    private void applySingleCost(Player player, GateCondition condition) {
        try {
            switch (condition.getConditionType()) {
                case EXPERIENCE:
                    int expCost = Integer.parseInt(condition.getValue());
                    player.setLevel(Math.max(0, player.getLevel() - expCost));
                    if (configManager.isDebugEnabled()) {
                        plugin.getLogger().info("扣除玩家 " + player.getName() + " 经验等级: " + expCost);
                    }
                    break;
                case MONEY:
                    if (economy != null) {
                        double moneyCost = Double.parseDouble(condition.getValue());
                        // 使用转账服务处理金钱费用
                        boolean success = transferService.transferTeleportFee(player, moneyCost);
                        if (!success) {
                            plugin.getLogger().warning("扣除玩家 " + player.getName() + " 传送费用失败: " + economy.format(moneyCost));
                        } else if (configManager.isDebugEnabled()) {
                            plugin.getLogger().info("成功处理玩家 " + player.getName() + " 传送费用: " + economy.format(moneyCost));
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().log(Level.WARNING, "应用费用时出错: " + condition, e);
        }
    }
    
    /**
     * 检查是否有经济系统
     * 
     * @return 是否有经济系统
     */
    public boolean hasEconomy() {
        return economy != null;
    }
}
