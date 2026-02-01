package org.plugin.gatetools.command.subcommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.command.SubCommand;
import org.plugin.gatetools.util.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 取消传送命令（内部使用）
 * 用法: /gatetools cancel-teleport <传送门名称>
 * 
 * @author NSrank, Augment
 */
public class CancelTeleportCommand implements SubCommand {
    private final GateTools plugin;
    
    public CancelTeleportCommand(GateTools plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "cancel-teleport";
    }
    
    @Override
    public String getDescription() {
        return "取消传送（内部命令）";
    }
    
    @Override
    public String getUsage() {
        return "/gatetools cancel-teleport <传送门名称>";
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList();
    }
    
    @Override
    public String getPermission() {
        return "gatetools.use";
    }
    
    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        
        Player player = (Player) sender;
        
        plugin.getTeleportService().cancelTeleport(player.getUniqueId());
        
        String message = plugin.getConfigManager().getMessage("teleport-cancelled");
        player.sendMessage(MessageUtil.colorize(message));
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("玩家 " + player.getName() + " 取消了传送");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
