package org.plugin.gatetools.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.manager.GateManager;
import org.plugin.gatetools.model.Gate;
import org.plugin.gatetools.service.TeleportService;
import org.plugin.gatetools.spatial.SpatialIndexManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家移动监听器
 * 负责检测玩家进入和离开传送区域
 * 
 * @author NSrank, Augment
 */
public class PlayerMovementListener implements Listener {
    private final GateTools plugin;
    private final GateManager gateManager;
    private final TeleportService teleportService;
    
    public PlayerMovementListener(GateTools plugin, GateManager gateManager, TeleportService teleportService) {
        this.plugin = plugin;
        this.gateManager = gateManager;
        this.teleportService = teleportService;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // 检查玩家是否有基础使用权限
        if (!player.hasPermission("gatetools.use")) {
            return;
        }
        
        // 优化：只在玩家实际移动时检查（不包括视角转动）
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        // 使用空间索引查询当前位置的传送门
        SpatialIndexManager spatialIndexManager = gateManager.getSpatialIndexManager();
        List<Gate> currentGates = spatialIndexManager.queryGates(event.getTo());

        // 检查所有传送门的状态变化
        Map<String, Gate> allGates = gateManager.getAllGates();
        for (Gate gate : allGates.values()) {
            boolean wasInside = gate.isPlayerInside(playerId);
            boolean isInside = currentGates.contains(gate);

            if (!wasInside && isInside) {
                // 玩家进入传送区域
                gate.playerEnter(playerId);
                handlePlayerEnterGate(player, gate);

                if (plugin.getConfigManager().isDebugEnabled()) {
                    plugin.getLogger().info("玩家 " + player.getName() + " 进入传送门: " + gate.getConfigName());
                }
            } else if (wasInside && !isInside) {
                // 玩家离开传送区域
                gate.playerLeave(playerId);
                handlePlayerLeaveGate(player, gate);

                if (plugin.getConfigManager().isDebugEnabled()) {
                    plugin.getLogger().info("玩家 " + player.getName() + " 离开传送门: " + gate.getConfigName());
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // 清理玩家状态
        Map<String, Gate> gates = gateManager.getAllGates();
        for (Gate gate : gates.values()) {
            gate.playerLeave(playerId);
        }
        
        // 取消传送任务
        teleportService.cancelTeleport(playerId);
    }
    
    /**
     * 处理玩家进入传送区域
     * 
     * @param player 玩家
     * @param gate 传送门
     */
    private void handlePlayerEnterGate(Player player, Gate gate) {
        UUID playerId = player.getUniqueId();

        // 检查是否已经在确认状态（避免重复触发）
        if (gate.isPlayerConfirming(playerId)) {
            return;
        }

        // 检查传送门是否配置了传送目标
        if (gate.getTeleportLocation() == null) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("传送门 " + gate.getConfigName() + " 未配置传送目标");
            }
            return;
        }

        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("玩家 " + player.getName() + " 进入传送区域: " + gate.getDisplayName());
        }

        // 触发传送确认
        teleportService.showTeleportConfirmation(player, gate);
    }
    
    /**
     * 处理玩家离开传送区域
     * 
     * @param player 玩家
     * @param gate 传送门
     */
    private void handlePlayerLeaveGate(Player player, Gate gate) {
        UUID playerId = player.getUniqueId();

        // 取消传送确认和传送任务
        teleportService.cancelTeleport(playerId);

        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("玩家 " + player.getName() + " 离开传送区域: " + gate.getDisplayName());
        }
    }
}
