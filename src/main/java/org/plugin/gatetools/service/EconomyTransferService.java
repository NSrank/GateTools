package org.plugin.gatetools.service;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.config.ConfigManager;
import org.plugin.gatetools.util.MessageUtil;

/**
 * 经济转账服务
 * 负责处理传送费用的转账功能
 * 
 * @author NSrank, Augment
 */
public class EconomyTransferService {
    private final GateTools plugin;
    private final ConfigManager configManager;
    private final Economy economy;
    
    public EconomyTransferService(GateTools plugin, ConfigManager configManager, Economy economy) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.economy = economy;
    }
    
    /**
     * 执行传送费用转账
     * 
     * @param payer 付款玩家
     * @param amount 转账金额
     * @return 转账是否成功
     */
    public boolean transferTeleportFee(OfflinePlayer payer, double amount) {
        if (economy == null) {
            plugin.getLogger().warning("经济系统未启用，无法执行转账");
            return false;
        }
        
        if (amount <= 0) {
            return true; // 金额为0或负数，无需转账
        }
        
        // 获取收款账户
        OfflinePlayer recipient = configManager.getRecipientPlayer();
        if (recipient == null) {
            // 没有配置收款账户，直接扣除费用（原有逻辑）
            if (configManager.isDebugEnabled()) {
                plugin.getLogger().info("未配置收款账户，直接扣除费用: " + economy.format(amount));
            }
            return economy.withdrawPlayer(payer, amount).transactionSuccess();
        }
        
        // 验证收款账户
        if (!configManager.isRecipientAccountValid()) {
            plugin.getLogger().warning("收款账户无效: " + configManager.getRecipientAccount());
            // 收款账户无效，直接扣除费用
            return economy.withdrawPlayer(payer, amount).transactionSuccess();
        }
        
        // 检查付款玩家余额
        if (!economy.has(payer, amount)) {
            return false; // 余额不足
        }
        
        // 执行转账
        return performTransfer(payer, recipient, amount);
    }
    
    /**
     * 执行实际的转账操作
     * 
     * @param payer 付款玩家
     * @param recipient 收款玩家
     * @param amount 转账金额
     * @return 转账是否成功
     */
    private boolean performTransfer(OfflinePlayer payer, OfflinePlayer recipient, double amount) {
        try {
            // 从付款玩家扣除金额
            var withdrawResult = economy.withdrawPlayer(payer, amount);
            if (!withdrawResult.transactionSuccess()) {
                plugin.getLogger().warning("从玩家 " + payer.getName() + " 扣除费用失败: " + withdrawResult.errorMessage);
                return false;
            }
            
            // 向收款玩家添加金额
            var depositResult = economy.depositPlayer(recipient, amount);
            if (!depositResult.transactionSuccess()) {
                // 转账失败，退还金额给付款玩家
                economy.depositPlayer(payer, amount);
                plugin.getLogger().warning("向收款账户 " + recipient.getName() + " 转账失败: " + depositResult.errorMessage);
                return false;
            }
            
            // 记录转账日志
            if (configManager.isTransferLoggingEnabled()) {
                plugin.getLogger().info(String.format(
                    "传送费用转账成功: %s -> %s, 金额: %s",
                    payer.getName(),
                    recipient.getName(),
                    economy.format(amount)
                ));
            }
            
            // 通知收款玩家（如果在线且启用通知）
            if (configManager.isRecipientNotificationEnabled() && recipient.isOnline()) {
                Player onlineRecipient = recipient.getPlayer();
                if (onlineRecipient != null) {
                    String message = String.format("&a收到传送费用: %s (来自: %s)", 
                        economy.format(amount), payer.getName());
                    onlineRecipient.sendMessage(MessageUtil.colorize(message));
                }
            }
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("转账过程中发生错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取收款账户信息
     * 
     * @return 收款账户信息字符串
     */
    public String getRecipientInfo() {
        OfflinePlayer recipient = configManager.getRecipientPlayer();
        if (recipient == null) {
            return "未配置收款账户";
        }
        
        String status = configManager.isRecipientAccountValid() ? "有效" : "无效";
        return String.format("收款账户: %s (%s)", recipient.getName(), status);
    }
}
