package org.plugin.gatetools.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.config.ConfigManager;
import org.plugin.gatetools.model.Gate;
import org.plugin.gatetools.model.GateCondition;
import org.plugin.gatetools.model.Location3D;
import org.plugin.gatetools.spatial.SpatialIndexManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * 传送门管理器
 * 负责管理所有传送门的创建、编辑、删除和数据持久化
 * 
 * @author NSrank, Augment
 */
public class GateManager {
    private final GateTools plugin;
    private final ConfigManager configManager;
    private final Map<String, Gate> gates;
    private final Map<UUID, Long> deleteConfirmations;
    private final SpatialIndexManager spatialIndexManager;

    public GateManager(GateTools plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.gates = new ConcurrentHashMap<>();
        this.deleteConfirmations = new ConcurrentHashMap<>();
        this.spatialIndexManager = new SpatialIndexManager(plugin);

        loadGates();
        startAutoSaveTask();
    }
    
    /**
     * 从数据文件加载传送门
     */
    public void loadGates() {
        gates.clear();
        FileConfiguration dataConfig = configManager.getDataConfig();
        ConfigurationSection gatesSection = dataConfig.getConfigurationSection("gates");
        
        if (gatesSection == null) {
            return;
        }
        
        for (String gateName : gatesSection.getKeys(false)) {
            try {
                ConfigurationSection gateSection = gatesSection.getConfigurationSection(gateName);
                if (gateSection == null) continue;
                
                String displayName = gateSection.getString("display-name", gateName);
                String corner1Str = gateSection.getString("corner1");
                String corner2Str = gateSection.getString("corner2");
                
                if (corner1Str == null || corner2Str == null) {
                    plugin.getLogger().warning("传送门 " + gateName + " 缺少角点坐标，跳过加载");
                    continue;
                }
                
                Location3D corner1 = Location3D.fromString(corner1Str);
                Location3D corner2 = Location3D.fromString(corner2Str);
                Gate gate = new Gate(gateName, displayName, corner1, corner2);
                
                // 加载条件
                ConfigurationSection conditionsSection = gateSection.getConfigurationSection("conditions");
                if (conditionsSection != null) {
                    for (String conditionKey : conditionsSection.getKeys(false)) {
                        try {
                            GateCondition.ConditionType conditionType = GateCondition.ConditionType.fromKey(conditionKey);
                            if (conditionType == null) continue;
                            
                            ConfigurationSection conditionSection = conditionsSection.getConfigurationSection(conditionKey);
                            if (conditionSection == null) continue;
                            
                            String judgeTypeStr = conditionSection.getString("judge-type");
                            String value = conditionSection.getString("value");
                            String operatorStr = conditionSection.getString("operator");
                            
                            GateCondition.JudgeType judgeType = GateCondition.JudgeType.fromKey(judgeTypeStr);
                            GateCondition.CompareOperator operator = null;
                            if (operatorStr != null) {
                                operator = GateCondition.CompareOperator.fromSymbol(operatorStr);
                            }
                            
                            if (judgeType != null && value != null) {
                                GateCondition condition = new GateCondition(conditionType, judgeType, operator, value);
                                gate.addCondition(condition);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.WARNING, "加载传送门 " + gateName + " 的条件时出错", e);
                        }
                    }
                }
                
                gates.put(gateName, gate);
                if (configManager.isDebugEnabled()) {
                    plugin.getLogger().info("已加载传送门: " + gateName);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "加载传送门 " + gateName + " 时出错", e);
            }
        }

        // 重建空间索引
        spatialIndexManager.clear();
        for (Gate gate : gates.values()) {
            spatialIndexManager.addGate(gate);
        }

        plugin.getLogger().info("已加载 " + gates.size() + " 个传送门");
        if (configManager.isDebugEnabled()) {
            plugin.getLogger().info(spatialIndexManager.getStats());
        }
    }
    
    /**
     * 保存传送门到数据文件
     */
    public void saveGates() {
        FileConfiguration dataConfig = configManager.getDataConfig();
        
        // 清除现有数据
        dataConfig.set("gates", null);
        
        for (Gate gate : gates.values()) {
            String path = "gates." + gate.getConfigName();
            dataConfig.set(path + ".display-name", gate.getDisplayName());
            dataConfig.set(path + ".corner1", gate.getCorner1().toString());
            dataConfig.set(path + ".corner2", gate.getCorner2().toString());
            
            // 保存条件
            Map<GateCondition.ConditionType, GateCondition> conditions = gate.getConditions();
            for (Map.Entry<GateCondition.ConditionType, GateCondition> entry : conditions.entrySet()) {
                GateCondition condition = entry.getValue();
                String conditionPath = path + ".conditions." + condition.getConditionType().getKey();
                
                dataConfig.set(conditionPath + ".judge-type", condition.getJudgeType().getKey());
                dataConfig.set(conditionPath + ".value", condition.getValue());
                if (condition.getCompareOperator() != null) {
                    dataConfig.set(conditionPath + ".operator", condition.getCompareOperator().getSymbol());
                }
            }
        }
        
        configManager.saveData();
        if (configManager.isDebugEnabled()) {
            plugin.getLogger().info("已保存 " + gates.size() + " 个传送门");
        }
    }
    
    /**
     * 异步保存传送门
     */
    public void saveGatesAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveGates);
    }
    
    /**
     * 启动自动保存任务
     */
    private void startAutoSaveTask() {
        int interval = configManager.getAutoSaveInterval() * 60 * 20; // 转换为tick
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::saveGates, interval, interval);
    }
    
    // 传送门操作方法
    public boolean createGate(String configName, String displayName, Location3D corner1, Location3D corner2) {
        if (gates.containsKey(configName)) {
            return false;
        }
        if (gates.size() >= configManager.getMaxGates()) {
            return false;
        }
        
        Gate gate = new Gate(configName, displayName, corner1, corner2);
        gates.put(configName, gate);
        spatialIndexManager.addGate(gate);
        saveGatesAsync();
        return true;
    }
    
    public Gate getGate(String configName) {
        return gates.get(configName);
    }
    
    public boolean deleteGate(String configName) {
        Gate removed = gates.remove(configName);
        if (removed != null) {
            spatialIndexManager.removeGate(removed);
            saveGatesAsync();
            return true;
        }
        return false;
    }
    
    public Set<String> getGateNames() {
        return gates.keySet();
    }
    
    public Map<String, Gate> getAllGates() {
        return new HashMap<>(gates);
    }
    
    // 删除确认相关方法
    public void addDeleteConfirmation(UUID playerId) {
        deleteConfirmations.put(playerId, System.currentTimeMillis());
    }
    
    public boolean hasDeleteConfirmation(UUID playerId) {
        Long timestamp = deleteConfirmations.get(playerId);
        if (timestamp == null) {
            return false;
        }
        
        long timeout = configManager.getDeleteConfirmTimeout() * 1000L;
        if (System.currentTimeMillis() - timestamp > timeout) {
            deleteConfirmations.remove(playerId);
            return false;
        }
        
        return true;
    }
    
    public void removeDeleteConfirmation(UUID playerId) {
        deleteConfirmations.remove(playerId);
    }

    /**
     * 获取空间索引管理器
     */
    public SpatialIndexManager getSpatialIndexManager() {
        return spatialIndexManager;
    }
}
