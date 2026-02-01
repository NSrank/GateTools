package org.plugin.gatetools.command.subcommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.command.SubCommand;
import org.plugin.gatetools.model.Gate;
import org.plugin.gatetools.util.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 删除传送门命令
 * 用法: /gatetools delete <配置名>
 * 
 * @author NSrank, Augment
 */
public class DeleteCommand implements SubCommand {
    private final GateTools plugin;
    
    public DeleteCommand(GateTools plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "delete";
    }
    
    @Override
    public String getDescription() {
        return "删除传送区域";
    }
    
    @Override
    public String getUsage() {
        return "/gatetools delete <配置名>";
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("remove", "del");
    }
    
    @Override
    public String getPermission() {
        return "gatetools.command.delete";
    }
    
    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MessageUtil.colorize("&c用法: " + getUsage()));
            return;
        }
        
        String gateName = args[0];
        Gate gate = plugin.getGateManager().getGate(gateName);
        
        if (gate == null) {
            String message = plugin.getConfigManager().getMessage("gate-not-found")
                    .replace("%gate_name%", gateName);
            sender.sendMessage(MessageUtil.colorize(message));
            return;
        }
        
        // 只有玩家才需要确认，控制台可以直接删除
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID playerId = player.getUniqueId();
            
            // 检查是否已经有确认记录
            if (plugin.getGateManager().hasDeleteConfirmation(playerId)) {
                // 执行删除
                boolean success = plugin.getGateManager().deleteGate(gateName);
                plugin.getGateManager().removeDeleteConfirmation(playerId);
                
                if (success) {
                    String message = plugin.getConfigManager().getMessage("gate-deleted")
                            .replace("%gate_name%", gate.getDisplayName());
                    sender.sendMessage(MessageUtil.colorize(message));
                    
                    if (plugin.getConfigManager().isDebugEnabled()) {
                        plugin.getLogger().info("玩家 " + sender.getName() + " 删除了传送门: " + gateName);
                    }
                } else {
                    sender.sendMessage(MessageUtil.colorize("&c删除传送门失败"));
                }
            } else {
                // 添加确认记录
                plugin.getGateManager().addDeleteConfirmation(playerId);
                
                String message = plugin.getConfigManager().getMessage("delete-confirm-required")
                        .replace("%timeout%", String.valueOf(plugin.getConfigManager().getDeleteConfirmTimeout()));
                sender.sendMessage(MessageUtil.colorize(message));
            }
        } else {
            // 控制台直接删除
            boolean success = plugin.getGateManager().deleteGate(gateName);
            
            if (success) {
                String message = plugin.getConfigManager().getMessage("gate-deleted")
                        .replace("%gate_name%", gate.getDisplayName());
                sender.sendMessage(MessageUtil.colorize(message));
                
                if (plugin.getConfigManager().isDebugEnabled()) {
                    plugin.getLogger().info("控制台删除了传送门: " + gateName);
                }
            } else {
                sender.sendMessage(MessageUtil.colorize("&c删除传送门失败"));
            }
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
        }
        
        return completions;
    }
}
