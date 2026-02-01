package org.plugin.gatetools.command.subcommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.command.SubCommand;
import org.plugin.gatetools.model.Location3D;
import org.plugin.gatetools.util.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 创建传送门命令
 * 用法: /gatetools set <角点1> <角点2> <配置名> <显示名>
 * 
 * @author NSrank, Augment
 */
public class SetCommand implements SubCommand {
    private final GateTools plugin;
    
    public SetCommand(GateTools plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "set";
    }
    
    @Override
    public String getDescription() {
        return "创建传送区域";
    }
    
    @Override
    public String getUsage() {
        return "/gatetools set <角点1> <角点2> <配置名> <显示名>";
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("create", "add");
    }
    
    @Override
    public String getPermission() {
        return "gatetools.command.set";
    }
    
    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(MessageUtil.colorize("&c用法: " + getUsage()));
            return;
        }
        
        try {
            // 解析角点坐标
            Location3D corner1 = Location3D.fromString(args[0]);
            Location3D corner2 = Location3D.fromString(args[1]);
            
            // 检查两个角点是否在同一个世界
            if (!corner1.getWorldName().equals(corner2.getWorldName())) {
                sender.sendMessage(MessageUtil.colorize("&c两个角点必须在同一个世界中"));
                return;
            }
            
            String configName = args[2];
            String displayName = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            
            // 检查配置名是否已存在
            if (plugin.getGateManager().getGate(configName) != null) {
                String message = plugin.getConfigManager().getMessage("gate-already-exists")
                        .replace("%gate_name%", configName);
                sender.sendMessage(MessageUtil.colorize(message));
                return;
            }
            
            // 创建传送门
            boolean success = plugin.getGateManager().createGate(configName, displayName, corner1, corner2);
            
            if (success) {
                String message = plugin.getConfigManager().getMessage("gate-created")
                        .replace("%gate_name%", displayName);
                sender.sendMessage(MessageUtil.colorize(message));
                
                if (plugin.getConfigManager().isDebugEnabled()) {
                    plugin.getLogger().info("玩家 " + sender.getName() + " 创建了传送门: " + configName);
                }
            } else {
                String message = plugin.getConfigManager().getMessage("max-gates-reached")
                        .replace("%max_gates%", String.valueOf(plugin.getConfigManager().getMaxGates()));
                sender.sendMessage(MessageUtil.colorize(message));
            }
            
        } catch (IllegalArgumentException e) {
            String message = plugin.getConfigManager().getMessage("invalid-location");
            sender.sendMessage(MessageUtil.colorize(message));
            
            // 提供格式示例
            sender.sendMessage(MessageUtil.colorize("&e示例: world,100,64,200"));
        } catch (Exception e) {
            sender.sendMessage(MessageUtil.colorize("&c创建传送门时发生错误: " + e.getMessage()));
            plugin.getLogger().warning("创建传送门时出错: " + e.getMessage());
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1 || args.length == 2) {
            // 角点坐标补全
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String currentLocation = String.format("%s,%.0f,%.0f,%.0f",
                        player.getWorld().getName(),
                        player.getLocation().getX(),
                        player.getLocation().getY(),
                        player.getLocation().getZ());
                completions.add(currentLocation);
            }
            completions.add("world,100,64,200");
        } else if (args.length == 3) {
            // 配置名补全
            completions.add("gate1");
            completions.add("spawn_gate");
        } else if (args.length == 4) {
            // 显示名补全
            completions.add("传送门1");
            completions.add("出生点传送门");
        }
        
        return completions;
    }
}
