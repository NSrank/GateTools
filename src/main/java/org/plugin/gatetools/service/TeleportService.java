package org.plugin.gatetools.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.config.ConfigManager;
import org.plugin.gatetools.model.Gate;
import org.plugin.gatetools.model.Location3D;
import org.plugin.gatetools.util.MessageUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 传送服务类
 * 负责处理传送确认、传送执行等功能
 * 
 * @author NSrank, Augment
 */
public class TeleportService {
    private final GateTools plugin;
    private final ConfigManager configManager;
    private final ConditionService conditionService;
    private final Map<UUID, BukkitTask> teleportTasks;
    private final Map<UUID, String> playerConfirmingGates;
    
    public TeleportService(GateTools plugin, ConfigManager configManager, ConditionService conditionService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.conditionService = conditionService;
        this.teleportTasks = new ConcurrentHashMap<>();
        this.playerConfirmingGates = new ConcurrentHashMap<>();
    }
    
    /**
     * 显示传送确认界面
     * 
     * @param player 玩家
     * @param gate 传送门
     */
    public void showTeleportConfirmation(Player player, Gate gate) {
        UUID playerId = player.getUniqueId();
        
        // 检查玩家是否已经在确认状态
        if (playerConfirmingGates.containsKey(playerId)) {
            return;
        }
        
        // 标记玩家进入确认状态
        playerConfirmingGates.put(playerId, gate.getConfigName());
        gate.playerStartConfirming(playerId);
        
        // 发送确认消息
        String confirmMessage = configManager.getMessage("teleport-confirm")
                .replace("%gate_name%", gate.getDisplayName());
        player.sendMessage(MessageUtil.colorize(confirmMessage));
        
        // 创建交互式按钮
        Component yesButton = Component.text(MessageUtil.stripColor(configManager.getYesButton()))
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text(MessageUtil.stripColor(configManager.getYesHover()))))
                .clickEvent(ClickEvent.runCommand("/gatetools confirm-teleport " + gate.getConfigName()));
        
        Component noButton = Component.text(MessageUtil.stripColor(configManager.getNoButton()))
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text(MessageUtil.stripColor(configManager.getNoHover()))))
                .clickEvent(ClickEvent.runCommand("/gatetools cancel-teleport " + gate.getConfigName()));
        
        Component buttons = yesButton.append(Component.text(" ")).append(noButton);
        player.sendMessage(buttons);
        
        if (configManager.isDebugEnabled()) {
            plugin.getLogger().info("向玩家 " + player.getName() + " 显示传送确认: " + gate.getDisplayName());
        }
    }
    
    /**
     * 确认传送
     * 
     * @param player 玩家
     * @param gateName 传送门名称
     * @return 是否成功开始传送
     */
    public boolean confirmTeleport(Player player, String gateName) {
        UUID playerId = player.getUniqueId();
        
        // 检查玩家是否在确认状态
        String confirmingGate = playerConfirmingGates.get(playerId);
        if (confirmingGate == null || !confirmingGate.equals(gateName)) {
            return false;
        }
        
        Gate gate = plugin.getGateManager().getGate(gateName);
        if (gate == null) {
            return false;
        }
        
        // 检查传送条件
        ConditionService.ConditionResult result = conditionService.checkConditions(player, gate);
        if (!result.isSuccess()) {
            player.sendMessage(MessageUtil.colorize(result.getErrorMessage()));
            cancelTeleport(playerId);
            return false;
        }
        
        // 清除确认状态
        playerConfirmingGates.remove(playerId);
        gate.playerStopConfirming(playerId);
        
        // 开始传送倒计时
        startTeleport(player, gate);
        return true;
    }
    
    /**
     * 取消传送
     * 
     * @param playerId 玩家UUID
     */
    public void cancelTeleport(UUID playerId) {
        // 取消传送任务
        BukkitTask task = teleportTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
        
        // 清除确认状态
        String gateName = playerConfirmingGates.remove(playerId);
        if (gateName != null) {
            Gate gate = plugin.getGateManager().getGate(gateName);
            if (gate != null) {
                gate.playerStopConfirming(playerId);
            }
        }
    }
    
    /**
     * 开始传送倒计时
     * 
     * @param player 玩家
     * @param gate 传送门
     */
    private void startTeleport(Player player, Gate gate) {
        UUID playerId = player.getUniqueId();
        
        // 发送准备消息
        String preparingMessage = configManager.getMessage("teleport-preparing");
        player.sendMessage(MessageUtil.colorize(preparingMessage));
        
        // 创建传送任务
        int delay = configManager.getTeleportDelay();
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            executeTeleport(player, gate);
        }, delay);
        
        teleportTasks.put(playerId, task);
        
        if (configManager.isDebugEnabled()) {
            plugin.getLogger().info("开始传送倒计时: " + player.getName() + " -> " + gate.getDisplayName());
        }
    }
    
    /**
     * 执行传送
     * 
     * @param player 玩家
     * @param gate 传送门
     */
    private void executeTeleport(Player player, Gate gate) {
        UUID playerId = player.getUniqueId();
        teleportTasks.remove(playerId);
        
        // 再次检查条件（防止在等待期间条件发生变化）
        ConditionService.ConditionResult result = conditionService.checkConditions(player, gate);
        if (!result.isSuccess()) {
            player.sendMessage(MessageUtil.colorize(result.getErrorMessage()));
            return;
        }
        
        // 获取传送目标
        Location3D targetLocation = gate.getTeleportLocation();
        if (targetLocation == null) {
            String failedMessage = configManager.getMessage("teleport-failed");
            player.sendMessage(MessageUtil.colorize(failedMessage));
            return;
        }
        
        Location bukkitLocation = targetLocation.toBukkitLocation();
        if (bukkitLocation == null) {
            String failedMessage = configManager.getMessage("teleport-failed");
            player.sendMessage(MessageUtil.colorize(failedMessage));
            return;
        }
        
        // 扣除费用
        conditionService.applyCosts(player, gate);
        
        // 执行传送
        player.teleport(bukkitLocation);
        
        // 发送成功消息
        String successMessage = configManager.getMessage("teleport-success");
        player.sendMessage(MessageUtil.colorize(successMessage));
        
        if (configManager.isDebugEnabled()) {
            plugin.getLogger().info("传送完成: " + player.getName() + " -> " + targetLocation);
        }
    }
    
    /**
     * 检查玩家是否在确认状态
     * 
     * @param playerId 玩家UUID
     * @return 是否在确认状态
     */
    public boolean isPlayerConfirming(UUID playerId) {
        return playerConfirmingGates.containsKey(playerId);
    }
    
    /**
     * 获取玩家正在确认的传送门名称
     * 
     * @param playerId 玩家UUID
     * @return 传送门名称，如果不在确认状态则返回null
     */
    public String getConfirmingGate(UUID playerId) {
        return playerConfirmingGates.get(playerId);
    }
}
