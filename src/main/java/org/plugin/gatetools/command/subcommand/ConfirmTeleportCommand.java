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
 * 确认传送命令（内部使用）
 * 用法: /gatetools confirm-teleport <传送门名称>
 * 
 * @author NSrank, Augment
 */
public class ConfirmTeleportCommand implements SubCommand {
    private final GateTools plugin;
    
    public ConfirmTeleportCommand(GateTools plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "confirm-teleport";
    }
    
    @Override
    public String getDescription() {
        return "确认传送（内部命令）";
    }
    
    @Override
    public String getUsage() {
        return "/gatetools confirm-teleport <传送门名称>";
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
        
        if (args.length < 1) {
            return;
        }
        
        Player player = (Player) sender;
        String gateName = args[0];
        
        boolean success = plugin.getTeleportService().confirmTeleport(player, gateName);
        
        if (!success) {
            // 传送失败的消息已在TeleportService中处理
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("玩家 " + player.getName() + " 传送确认失败: " + gateName);
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
