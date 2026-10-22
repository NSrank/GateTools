package org.plugin.gatetools.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.command.subcommand.*;

import java.util.*;

/**
 * GateTools主命令处理器
 * 负责分发子命令到对应的处理器
 * 
 * @author NSrank, Augment
 */
public class GateToolsCommand implements CommandExecutor, TabCompleter {
    private final GateTools plugin;
    private final Map<String, SubCommand> subCommands;
    
    public GateToolsCommand(GateTools plugin) {
        this.plugin = plugin;
        this.subCommands = new HashMap<>();
        
        // 注册子命令
        registerSubCommand(new SetCommand(plugin));
        registerSubCommand(new EditCommand(plugin));
        registerSubCommand(new DeleteCommand(plugin));
        registerSubCommand(new ReloadCommand(plugin));
        registerSubCommand(new SaveCommand(plugin));
        registerSubCommand(new ConfirmCommand(plugin));
        registerSubCommand(new ConfirmTeleportCommand(plugin));
        registerSubCommand(new CancelTeleportCommand(plugin));
        registerSubCommand(new HelpCommand(plugin, subCommands));
    }
    
    /**
     * 注册子命令
     * 
     * @param subCommand 子命令
     */
    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
        for (String alias : subCommand.getAliases()) {
            subCommands.put(alias.toLowerCase(), subCommand);
        }
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        // 检查基础权限
        if (!sender.hasPermission("gatetools.command")) {
            String message = plugin.getConfigManager().getMessage("error.no-permission");
            sender.sendMessage(plugin.getConfigManager().colorize(message));
            return true;
        }
        
        // 如果没有参数，显示帮助
        if (args.length == 0) {
            SubCommand helpCommand = subCommands.get("help");
            if (helpCommand != null) {
                helpCommand.execute(sender, args);
            }
            return true;
        }
        
        // 查找并执行子命令
        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);
        
        if (subCommand == null) {
            String message = plugin.getConfigManager().getMessage("error.invalid-command");
            if (message.contains("消息未找到") || message.contains("Message not found")) {
                message = "&c未知的子命令: " + args[0];
            } else {
                message = message.replace("%command%", args[0]);
            }
            sender.sendMessage(plugin.getConfigManager().colorize(message));
            return true;
        }

        // 检查子命令权限
        if (!subCommand.hasPermission(sender)) {
            String message = plugin.getConfigManager().getMessage("error.no-permission");
            sender.sendMessage(plugin.getConfigManager().colorize(message));
            return true;
        }
        
        // 执行子命令
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, subArgs);
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                               @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一个参数：子命令补全
            String input = args[0].toLowerCase();
            for (String subCommandName : subCommands.keySet()) {
                SubCommand subCommand = subCommands.get(subCommandName);
                if (subCommand.hasPermission(sender) && subCommandName.startsWith(input)) {
                    // 只添加主命令名，不添加别名
                    if (subCommandName.equals(subCommand.getName().toLowerCase())) {
                        completions.add(subCommand.getName());
                    }
                }
            }
        } else if (args.length > 1) {
            // 子命令的参数补全
            String subCommandName = args[0].toLowerCase();
            SubCommand subCommand = subCommands.get(subCommandName);
            
            if (subCommand != null && subCommand.hasPermission(sender)) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                List<String> subCompletions = subCommand.onTabComplete(sender, subArgs);
                if (subCompletions != null) {
                    completions.addAll(subCompletions);
                }
            }
        }
        
        Collections.sort(completions);
        return completions;
    }
}
