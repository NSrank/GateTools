package org.plugin.gatetools.command.subcommand;

import org.bukkit.command.CommandSender;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.command.SubCommand;
import org.plugin.gatetools.util.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 重载配置命令
 * 用法: /gatetools reload
 * 
 * @author NSrank, Augment
 */
public class ReloadCommand implements SubCommand {
    private final GateTools plugin;
    
    public ReloadCommand(GateTools plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "reload";
    }
    
    @Override
    public String getDescription() {
        return "重载配置文件";
    }
    
    @Override
    public String getUsage() {
        return "/gatetools reload";
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("rl");
    }
    
    @Override
    public String getPermission() {
        return "gatetools.command.reload";
    }
    
    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            // 重载配置文件
            plugin.getConfigManager().reloadConfigs();
            
            // 重载传送门数据
            plugin.getGateManager().loadGates();
            
            String message = plugin.getConfigManager().getMessage("success.config-reloaded");
            sender.sendMessage(MessageUtil.colorize(message));
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("玩家 " + sender.getName() + " 重载了配置文件");
            }
            
        } catch (Exception e) {
            sender.sendMessage(MessageUtil.colorize("&c重载配置时发生错误: " + e.getMessage()));
            plugin.getLogger().warning("重载配置时出错: " + e.getMessage());
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
