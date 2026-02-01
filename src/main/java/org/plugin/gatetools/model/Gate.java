package org.plugin.gatetools.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 传送门数据模型
 * 包含传送门的所有配置信息
 * 
 * @author NSrank, Augment
 */
public class Gate {
    private final String configName;
    private final String displayName;
    private final Location3D corner1;
    private final Location3D corner2;
    private final Map<GateCondition.ConditionType, GateCondition> conditions;
    private final Set<UUID> playersInside;
    private final Set<UUID> playersConfirming;
    
    /**
     * 构造函数
     * 
     * @param configName 配置名称
     * @param displayName 显示名称
     * @param corner1 区域角点1
     * @param corner2 区域角点2
     */
    public Gate(String configName, String displayName, Location3D corner1, Location3D corner2) {
        this.configName = configName;
        this.displayName = displayName;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.conditions = new HashMap<>();
        this.playersInside = new HashSet<>();
        this.playersConfirming = new HashSet<>();
    }
    
    /**
     * 检查玩家是否在传送区域内
     * 
     * @param player 玩家
     * @return 是否在区域内
     */
    public boolean isPlayerInside(Player player) {
        Location loc = player.getLocation();
        
        // 检查世界是否匹配
        if (!loc.getWorld().getName().equals(corner1.getWorldName()) ||
            !loc.getWorld().getName().equals(corner2.getWorldName())) {
            return false;
        }
        
        // 计算区域边界
        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());
        
        // 检查玩家位置是否在区域内
        return loc.getX() >= minX && loc.getX() <= maxX &&
               loc.getY() >= minY && loc.getY() <= maxY &&
               loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
    
    /**
     * 添加条件
     * 
     * @param condition 条件
     */
    public void addCondition(GateCondition condition) {
        conditions.put(condition.getConditionType(), condition);
    }
    
    /**
     * 移除条件
     * 
     * @param conditionType 条件类型
     */
    public void removeCondition(GateCondition.ConditionType conditionType) {
        conditions.remove(conditionType);
    }
    
    /**
     * 获取条件
     * 
     * @param conditionType 条件类型
     * @return 条件对象，如果不存在则返回null
     */
    public GateCondition getCondition(GateCondition.ConditionType conditionType) {
        return conditions.get(conditionType);
    }
    
    /**
     * 获取传送目标位置
     * 
     * @return 传送位置，如果未设置则返回null
     */
    public Location3D getTeleportLocation() {
        GateCondition teleportCondition = getCondition(GateCondition.ConditionType.TELEPORT);
        return teleportCondition != null ? teleportCondition.getTeleportLocation() : null;
    }
    
    /**
     * 玩家进入区域
     * 
     * @param playerId 玩家UUID
     */
    public void playerEnter(UUID playerId) {
        playersInside.add(playerId);
    }
    
    /**
     * 玩家离开区域
     * 
     * @param playerId 玩家UUID
     */
    public void playerLeave(UUID playerId) {
        playersInside.remove(playerId);
        playersConfirming.remove(playerId);
    }
    
    /**
     * 玩家开始确认传送
     * 
     * @param playerId 玩家UUID
     */
    public void playerStartConfirming(UUID playerId) {
        playersConfirming.add(playerId);
    }
    
    /**
     * 玩家停止确认传送
     * 
     * @param playerId 玩家UUID
     */
    public void playerStopConfirming(UUID playerId) {
        playersConfirming.remove(playerId);
    }
    
    /**
     * 检查玩家是否在确认状态
     * 
     * @param playerId 玩家UUID
     * @return 是否在确认状态
     */
    public boolean isPlayerConfirming(UUID playerId) {
        return playersConfirming.contains(playerId);
    }
    
    /**
     * 检查玩家是否在区域内（通过UUID）
     * 
     * @param playerId 玩家UUID
     * @return 是否在区域内
     */
    public boolean isPlayerInside(UUID playerId) {
        return playersInside.contains(playerId);
    }
    
    // Getters
    public String getConfigName() { return configName; }
    public String getDisplayName() { return displayName; }
    public Location3D getCorner1() { return corner1; }
    public Location3D getCorner2() { return corner2; }
    public Map<GateCondition.ConditionType, GateCondition> getConditions() { return new HashMap<>(conditions); }
    public Set<UUID> getPlayersInside() { return new HashSet<>(playersInside); }
    public Set<UUID> getPlayersConfirming() { return new HashSet<>(playersConfirming); }
    
    @Override
    public String toString() {
        return String.format("Gate{name='%s', display='%s', corner1=%s, corner2=%s, conditions=%d}", 
                           configName, displayName, corner1, corner2, conditions.size());
    }
}
