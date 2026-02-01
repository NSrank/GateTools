package org.plugin.gatetools.command.subcommand;

import org.bukkit.command.CommandSender;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.command.SubCommand;
import org.plugin.gatetools.model.Gate;
import org.plugin.gatetools.model.GateCondition;
import org.plugin.gatetools.model.Location3D;
import org.plugin.gatetools.util.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 编辑传送门命令
 * 用法: /gatetools edit <配置名> <配置项> <判断器> [值]
 * 
 * @author NSrank, Augment
 */
public class EditCommand implements SubCommand {
    private final GateTools plugin;
    
    public EditCommand(GateTools plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "edit";
    }
    
    @Override
    public String getDescription() {
        return "编辑传送区域";
    }
    
    @Override
    public String getUsage() {
        return "/gatetools edit <配置名> <配置项> <判断器> [值]";
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("modify", "update");
    }
    
    @Override
    public String getPermission() {
        return "gatetools.command.edit";
    }
    
    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MessageUtil.colorize("&c用法: " + getUsage()));
            sender.sendMessage(MessageUtil.colorize("&e配置项: experience, permission, money, teleport"));
            sender.sendMessage(MessageUtil.colorize("&e判断器: set, cost (teleport配置项直接接坐标)"));
            return;
        }

        String gateName = args[0];
        Gate gate = plugin.getGateManager().getGate(gateName);

        if (gate == null) {
            String message = plugin.getConfigManager().getMessage("error.gate-not-found")
                    .replace("%gate_name%", gateName);
            sender.sendMessage(MessageUtil.colorize(message));
            return;
        }

        String conditionTypeStr = args[1];
        GateCondition.ConditionType conditionType = GateCondition.ConditionType.fromKey(conditionTypeStr);

        if (conditionType == null) {
            sender.sendMessage(MessageUtil.colorize("&c无效的配置项: " + conditionTypeStr));
            return;
        }

        try {
            GateCondition condition = null;

            if (conditionType == GateCondition.ConditionType.TELEPORT) {
                // 处理传送目标 - 直接接坐标，不需要判断器
                if (args.length < 3) {
                    sender.sendMessage(MessageUtil.colorize("&c用法: /gatetools edit <配置名> teleport <世界,x,y,z>"));
                    sender.sendMessage(MessageUtil.colorize("&e示例: /gatetools edit gate1 teleport world,100,64,200"));
                    return;
                }

                String locationStr = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                Location3D location = Location3D.fromString(locationStr);
                condition = GateCondition.createTeleportCondition(location);

            } else {
                // 其他配置项需要判断器
                if (args.length < 3) {
                    sender.sendMessage(MessageUtil.colorize("&c用法: /gatetools edit <配置名> <配置项> <判断器> [值]"));
                    sender.sendMessage(MessageUtil.colorize("&e判断器: set, cost"));
                    return;
                }

                String judgeTypeStr = args[2];
                GateCondition.JudgeType judgeType = GateCondition.JudgeType.fromKey(judgeTypeStr);

                if (judgeType == null) {
                    sender.sendMessage(MessageUtil.colorize("&c无效的判断器: " + judgeTypeStr));
                    return;
                }

                // 检查判断器和配置项的兼容性
                if (judgeType == GateCondition.JudgeType.COST &&
                    conditionType != GateCondition.ConditionType.EXPERIENCE &&
                    conditionType != GateCondition.ConditionType.MONEY) {
                    sender.sendMessage(MessageUtil.colorize("&c只有经验和金钱配置项支持cost判断器"));
                    return;
                }

                if (judgeType == GateCondition.JudgeType.COST) {
                    // 处理费用类型
                    if (args.length < 4) {
                        sender.sendMessage(MessageUtil.colorize("&c请提供费用值"));
                        return;
                    }
                    String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    condition = GateCondition.createCostCondition(conditionType, value);

                } else {
                    // 处理set类型，需要解析比较操作符
                    if (args.length < 5) {
                        sender.sendMessage(MessageUtil.colorize("&c用法: /gatetools edit <配置名> <配置项> set <操作符> <值>"));
                        sender.sendMessage(MessageUtil.colorize("&e操作符: =, !, >, >=, <, <="));
                        return;
                    }

                    String operatorStr = args[3];
                    String actualValue = String.join(" ", Arrays.copyOfRange(args, 4, args.length));

                    GateCondition.CompareOperator operator = GateCondition.CompareOperator.fromSymbol(operatorStr);
                    if (operator == null) {
                        sender.sendMessage(MessageUtil.colorize("&c无效的操作符: " + operatorStr));
                        return;
                    }

                    // 检查操作符和配置项的兼容性
                    if ((operator == GateCondition.CompareOperator.GREATER ||
                         operator == GateCondition.CompareOperator.GREATER_EQUAL ||
                         operator == GateCondition.CompareOperator.LESS ||
                         operator == GateCondition.CompareOperator.LESS_EQUAL) &&
                        conditionType != GateCondition.ConditionType.EXPERIENCE &&
                        conditionType != GateCondition.ConditionType.MONEY) {
                        sender.sendMessage(MessageUtil.colorize("&c数值比较操作符只能用于经验和金钱配置项"));
                        return;
                    }

                    condition = GateCondition.createSetCondition(conditionType, operator, actualValue);
                }
            }

            // 添加条件到传送门
            gate.addCondition(condition);
            plugin.getGateManager().saveGatesAsync();

            String message = plugin.getConfigManager().getMessage("success.gate-edited")
                    .replace("%gate_name%", gate.getDisplayName());
            sender.sendMessage(MessageUtil.colorize(message));

            // 显示设置的具体信息
            if (conditionType == GateCondition.ConditionType.TELEPORT) {
                sender.sendMessage(MessageUtil.colorize("&a已设置传送目标: &f" + condition.getValue()));
            } else {
                sender.sendMessage(MessageUtil.colorize("&a已设置条件: &f" + condition.toString()));
            }

            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("玩家 " + sender.getName() + " 编辑了传送门: " + gateName + " - " + condition);
            }

        } catch (IllegalArgumentException e) {
            String message = plugin.getConfigManager().getMessage("error.invalid-condition");
            sender.sendMessage(MessageUtil.colorize(message));
            sender.sendMessage(MessageUtil.colorize("&c错误: " + e.getMessage()));
        } catch (Exception e) {
            sender.sendMessage(MessageUtil.colorize("&c编辑传送门时发生错误: " + e.getMessage()));
            plugin.getLogger().warning("编辑传送门时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 传送门名称补全
            for (String gateName : plugin.getGateManager().getGateNames()) {
                if (gateName.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(gateName);
                }
            }
        } else if (args.length == 2) {
            // 配置项补全
            String[] conditionTypes = {"experience", "permission", "money", "teleport"};
            for (String type : conditionTypes) {
                if (type.startsWith(args[1].toLowerCase())) {
                    completions.add(type);
                }
            }
        } else if (args.length == 3) {
            // 根据配置项类型提供不同的补全
            String conditionType = args[1].toLowerCase();

            if ("teleport".equals(conditionType)) {
                // teleport配置项直接提供坐标格式提示
                if (sender instanceof org.bukkit.entity.Player) {
                    org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
                    org.bukkit.Location loc = player.getLocation();
                    String suggestion = loc.getWorld().getName() + "," +
                                      (int)loc.getX() + "," +
                                      (int)loc.getY() + "," +
                                      (int)loc.getZ();
                    completions.add(suggestion);
                } else {
                    completions.add("world,x,y,z");
                }
            } else {
                // 其他配置项提供判断器补全
                String[] judgeTypes = {"set", "cost"};
                for (String type : judgeTypes) {
                    if (type.startsWith(args[2].toLowerCase())) {
                        completions.add(type);
                    }
                }
            }
        } else if (args.length == 4 && !"teleport".equals(args[1].toLowerCase()) && "set".equals(args[2])) {
            // 操作符补全（仅非teleport配置项）
            String[] operators = {"=", "!", ">", ">=", "<", "<="};
            for (String op : operators) {
                if (op.startsWith(args[3])) {
                    completions.add(op);
                }
            }
        } else if (args.length == 5 && !"teleport".equals(args[1].toLowerCase()) && "set".equals(args[2])) {
            // 值补全提示
            String conditionType = args[1].toLowerCase();
            switch (conditionType) {
                case "permission":
                    completions.add("teleport.example");
                    break;
                case "experience":
                    completions.add("10");
                    completions.add("20");
                    break;
                case "money":
                    completions.add("100");
                    completions.add("500");
                    break;
            }
        } else if (args.length == 4 && !"teleport".equals(args[1].toLowerCase()) && "cost".equals(args[2])) {
            // cost类型的值补全
            String conditionType = args[1].toLowerCase();
            if ("experience".equals(conditionType)) {
                completions.add("5");
                completions.add("10");
            } else if ("money".equals(conditionType)) {
                completions.add("50");
                completions.add("100");
            }
        }

        return completions;
    }
}
