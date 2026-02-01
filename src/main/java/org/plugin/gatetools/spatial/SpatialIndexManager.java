/*
 * MIT License
 * 
 * Copyright (c) 2024 NSrank, Augment
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.plugin.gatetools.spatial;

import org.bukkit.Location;
import org.bukkit.World;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.model.Gate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 空间索引管理器
 * 
 * 使用八叉树为每个世界管理传送门的空间索引，提供高效的空间查询
 * 
 * @author NSrank, Augment
 */
public class SpatialIndexManager {
    private final GateTools plugin;
    private final ConcurrentHashMap<String, Octree> worldOctrees = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Range3D, Gate> rangeToGate = new ConcurrentHashMap<>();
    
    // 八叉树配置
    private static final int MAX_DEPTH = 10;
    private static final int MAX_ITEMS = 10;
    private static final int WORLD_SIZE = 60000000; // Minecraft世界边界
    
    public SpatialIndexManager(GateTools plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 添加传送门到空间索引
     * 
     * @param gate 传送门
     */
    public void addGate(Gate gate) {
        String worldName = gate.getCorner1().getWorldName();
        
        // 确保世界的八叉树存在
        Octree octree = worldOctrees.computeIfAbsent(worldName, this::createOctreeForWorld);
        
        // 创建传送门的空间范围
        Range3D gateRange = createGateRange(gate);
        
        // 添加到八叉树和映射
        octree.insert(gateRange, gate);
        rangeToGate.put(gateRange, gate);
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("添加传送门到空间索引: " + gate.getConfigName() + " 范围: " + gateRange);
        }
    }
    
    /**
     * 从空间索引中移除传送门
     * 
     * @param gate 传送门
     */
    public void removeGate(Gate gate) {
        String worldName = gate.getCorner1().getWorldName();
        Octree octree = worldOctrees.get(worldName);
        
        if (octree != null) {
            Range3D gateRange = createGateRange(gate);
            octree.remove(gateRange);
            rangeToGate.remove(gateRange);
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("从空间索引移除传送门: " + gate.getConfigName());
            }
        }
    }
    
    /**
     * 查询指定位置的传送门
     * 
     * @param location 位置
     * @return 包含该位置的传送门列表
     */
    public List<Gate> queryGates(Location location) {
        String worldName = location.getWorld().getName();
        Octree octree = worldOctrees.get(worldName);
        
        if (octree == null) {
            return new ArrayList<>();
        }
        
        Point3D point = new Point3D(location);
        List<Range3D> ranges = octree.queryRanges(point);
        
        List<Gate> gates = new ArrayList<>();
        for (Range3D range : ranges) {
            Gate gate = rangeToGate.get(range);
            if (gate != null) {
                gates.add(gate);
            }
        }
        
        return gates;
    }
    
    /**
     * 查询指定位置的第一个传送门
     * 
     * @param location 位置
     * @return 包含该位置的第一个传送门，如果没有则返回null
     */
    public Gate queryFirstGate(Location location) {
        String worldName = location.getWorld().getName();
        Octree octree = worldOctrees.get(worldName);
        
        if (octree == null) {
            return null;
        }
        
        Point3D point = new Point3D(location);
        Range3D range = octree.firstRange(point);
        
        return range != null ? rangeToGate.get(range) : null;
    }
    
    /**
     * 重建指定世界的空间索引
     * 
     * @param worldName 世界名称
     * @param gates 该世界的所有传送门
     */
    public void rebuildWorldIndex(String worldName, List<Gate> gates) {
        // 关闭旧的八叉树
        Octree oldOctree = worldOctrees.get(worldName);
        if (oldOctree != null) {
            oldOctree.close();
        }
        
        // 创建新的八叉树
        Octree newOctree = createOctreeForWorld(worldName);
        worldOctrees.put(worldName, newOctree);
        
        // 清理旧的映射
        rangeToGate.entrySet().removeIf(entry -> {
            Gate gate = entry.getValue();
            return gate.getCorner1().getWorldName().equals(worldName);
        });
        
        // 添加所有传送门
        for (Gate gate : gates) {
            if (gate.getCorner1().getWorldName().equals(worldName)) {
                Range3D gateRange = createGateRange(gate);
                newOctree.insert(gateRange, gate);
                rangeToGate.put(gateRange, gate);
            }
        }
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            OctreeStats stats = newOctree.getStats();
            plugin.getLogger().info("重建世界 " + worldName + " 的空间索引: " + stats);
        }
    }
    
    /**
     * 获取空间索引统计信息
     */
    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("空间索引统计:\n");
        
        for (ConcurrentHashMap.Entry<String, Octree> entry : worldOctrees.entrySet()) {
            String worldName = entry.getKey();
            Octree octree = entry.getValue();
            OctreeStats stats = octree.getStats();
            
            sb.append("  世界 ").append(worldName).append(": ").append(stats).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 清空所有空间索引
     */
    public void clear() {
        for (Octree octree : worldOctrees.values()) {
            octree.close();
        }
        worldOctrees.clear();
        rangeToGate.clear();
    }
    
    /**
     * 为世界创建八叉树
     */
    private Octree createOctreeForWorld(String worldName) {
        // 创建覆盖整个世界的边界
        Range3D worldBoundary = new Range3D(
            -WORLD_SIZE, -256, -WORLD_SIZE,
            WORLD_SIZE, 320, WORLD_SIZE
        );
        
        return new Octree(worldBoundary, MAX_DEPTH, MAX_ITEMS);
    }
    
    /**
     * 为传送门创建空间范围
     */
    private Range3D createGateRange(Gate gate) {
        Point3D point1 = new Point3D(
            (int) gate.getCorner1().getX(),
            (int) gate.getCorner1().getY(),
            (int) gate.getCorner1().getZ()
        );
        Point3D point2 = new Point3D(
            (int) gate.getCorner2().getX(),
            (int) gate.getCorner2().getY(),
            (int) gate.getCorner2().getZ()
        );
        
        return new Range3D(point1, point2);
    }
}
