package org.plugin.gatetools.command.subcommand;

import org.bukkit.command.CommandSender;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.command.SubCommand;
import org.plugin.gatetools.util.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 帮助命令
 * 用法: /gatetools help
 * 
 * @author NSrank, Augment
 */
public class HelpCommand implements SubCommand {
    private final GateTools plugin;
    private final Map<String, SubCommand> subCommands;
    
    public HelpCommand(GateTools plugin, Map<String, SubCommand> subCommands) {
        this.plugin = plugin;
        this.subCommands = subCommands;
    }
    
    @Override
    public String getName() {
        return "help";
    }
    
    @Override
    public String getDescription() {
        return "显示帮助信息";
    }
    
    @Override
    public String getUsage() {
        return "/gatetools help";
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("?");
    }
    
    @Override
    public String getPermission() {
        return "gatetools.command";
    }
    
    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(MessageUtil.colorize("&6=== GateTools 帮助 ==="));
        sender.sendMessage(MessageUtil.colorize("&e作者: &fNSrank, Augment"));
        sender.sendMessage(MessageUtil.colorize("&e版本: &f1.0"));
        sender.sendMessage("");
        
        // 显示可用命令
        for (SubCommand subCommand : subCommands.values()) {
            // 跳过内部命令和别名
            if (subCommand.getName().contains("-") || 
                !subCommand.getName().equals(subCommand.getName().toLowerCase()) ||
                !subCommand.hasPermission(sender)) {
                continue;
            }
            
            // 跳过已显示的命令（避免重复）
            String commandName = subCommand.getName();
            boolean isMainCommand = true;
            for (SubCommand other : subCommands.values()) {
                if (other != subCommand && other.getName().equals(commandName)) {
                    isMainCommand = false;
                    break;
                }
            }
            
            if (isMainCommand && !commandName.equals("help")) {
                sender.sendMessage(MessageUtil.colorize(
                    "&e" + subCommand.getUsage() + " &7- " + subCommand.getDescription()));
            }
        }
        
        sender.sendMessage("");
        sender.sendMessage(MessageUtil.colorize("&7提示: 使用 &e/gatetools <命令> &7查看具体用法"));
        
        // 显示配置示例
        sender.sendMessage("");
        sender.sendMessage(MessageUtil.colorize("&6=== 配置示例 ==="));
        sender.sendMessage(MessageUtil.colorize("&7创建传送门:"));
        sender.sendMessage(MessageUtil.colorize("&e/gatetools set world,100,64,200 world,110,74,210 gate1 传送门1"));
        sender.sendMessage(MessageUtil.colorize("&7设置传送目标:"));
        sender.sendMessage(MessageUtil.colorize("&e/gatetools edit gate1 teleport set world,300,64,400"));
        sender.sendMessage(MessageUtil.colorize("&7设置权限要求:"));
        sender.sendMessage(MessageUtil.colorize("&e/gatetools edit gate1 permission set = teleport.gate1"));
        sender.sendMessage(MessageUtil.colorize("&7设置金钱费用:"));
        sender.sendMessage(MessageUtil.colorize("&e/gatetools edit gate1 money cost 100"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
