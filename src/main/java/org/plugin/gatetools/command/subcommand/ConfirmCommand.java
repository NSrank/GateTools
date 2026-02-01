package org.plugin.gatetools.command.subcommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.command.SubCommand;
import org.plugin.gatetools.util.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 确认删除命令
 * 用法: /gatetools confirm
 * 
 * @author NSrank, Augment
 */
public class ConfirmCommand implements SubCommand {
    private final GateTools plugin;
    
    public ConfirmCommand(GateTools plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "confirm";
    }
    
    @Override
    public String getDescription() {
        return "确认删除操作";
    }
    
    @Override
    public String getUsage() {
        return "/gatetools confirm";
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("yes");
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.colorize("&c此命令只能由玩家执行"));
            return;
        }
        
        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();
        
        if (!plugin.getGateManager().hasDeleteConfirmation(playerId)) {
            String message = plugin.getConfigManager().getMessage("delete-confirm-expired");
            sender.sendMessage(MessageUtil.colorize(message));
            return;
        }
        
        // 这个命令只是标记确认状态，实际删除在DeleteCommand中处理
        sender.sendMessage(MessageUtil.colorize("&a确认状态已记录，请重新执行删除命令"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
