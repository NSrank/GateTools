package org.plugin.gatetools.command.subcommand;

import org.bukkit.command.CommandSender;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.command.SubCommand;
import org.plugin.gatetools.util.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 保存数据命令
 * 用法: /gatetools save
 * 
 * @author NSrank, Augment
 */
public class SaveCommand implements SubCommand {
    private final GateTools plugin;
    
    public SaveCommand(GateTools plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "save";
    }
    
    @Override
    public String getDescription() {
        return "保存数据";
    }
    
    @Override
    public String getUsage() {
        return "/gatetools save";
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList();
    }
    
    @Override
    public String getPermission() {
        return "gatetools.command.save";
    }
    
    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            // 异步保存数据
            plugin.getGateManager().saveGatesAsync();
            
            String message = plugin.getConfigManager().getMessage("success.data-saved");
            sender.sendMessage(MessageUtil.colorize(message));
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("玩家 " + sender.getName() + " 手动保存了数据");
            }
            
        } catch (Exception e) {
            sender.sendMessage(MessageUtil.colorize("&c保存数据时发生错误: " + e.getMessage()));
            plugin.getLogger().warning("保存数据时出错: " + e.getMessage());
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
