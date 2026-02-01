package org.plugin.gatetools.util;

import org.bukkit.scheduler.BukkitTask;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.manager.GateManager;
import org.plugin.gatetools.service.TeleportService;
import org.plugin.gatetools.spatial.SpatialIndexManager;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 插件内存监控器
 * 监控插件自身的内存使用情况，而不是整个JVM
 *
 * @author NSrank, Augment
 */
public class MemoryMonitor {
    private final GateTools plugin;
    private final MemoryMXBean memoryBean;
    private final AtomicLong pluginMemoryUsed = new AtomicLong(0);
    private final AtomicLong maxPluginMemoryUsed = new AtomicLong(0);
    private final AtomicLong monitoringStartTime = new AtomicLong(System.currentTimeMillis());
    private BukkitTask monitorTask;
    private BukkitTask memoryCleanupTask;

    // 默认配置值
    private long memoryLeakThreshold = 50 * 1024 * 1024; // 50MB
    private int monitoringInterval = 300; // 5分钟
    private int leakCheckCycles = 6; // 30分钟
    private long memoryLimit = 100 * 1024 * 1024; // 100MB
    private boolean autoCleanupEnabled = true;

    private int monitorCycles = 0;
    private long baselineMemory = 0;
    
    public MemoryMonitor(GateTools plugin) {
        this.plugin = plugin;
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        loadConfiguration();
    }

    /**
     * 从配置文件加载内存监控配置
     */
    private void loadConfiguration() {
        this.memoryLeakThreshold = plugin.getConfigManager().getMemoryLeakThreshold();
        this.monitoringInterval = plugin.getConfigManager().getMemoryMonitorInterval();
        this.leakCheckCycles = plugin.getConfigManager().getMemoryLeakCheckCycles();
        this.memoryLimit = plugin.getConfigManager().getMemoryLimit();
        this.autoCleanupEnabled = plugin.getConfigManager().isMemoryAutoCleanupEnabled();
    }
    
    public void startMonitoring() {
        if (monitorTask != null) {
            monitorTask.cancel();
        }
        if (memoryCleanupTask != null) {
            memoryCleanupTask.cancel();
        }

        // 重新加载配置
        loadConfiguration();

        baselineMemory = calculatePluginMemoryUsage();
        monitoringStartTime.set(System.currentTimeMillis());

        // 启动内存监控任务
        monitorTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
            plugin, this::performMemoryCheck,
            monitoringInterval * 20L,
            monitoringInterval * 20L
        );

        // 启动内存清理任务（如果启用）
        if (autoCleanupEnabled) {
            int cleanupInterval = plugin.getConfigManager().getMemoryCleanupInterval() * 60 * 20; // 转换为tick
            memoryCleanupTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin, this::performMemoryCleanup,
                cleanupInterval,
                cleanupInterval
            );
        }

        plugin.getLogger().info("插件内存监控已启动，基准内存: " + formatBytes(baselineMemory));
        if (autoCleanupEnabled) {
            plugin.getLogger().info("自动内存清理已启用，清理间隔: " + plugin.getConfigManager().getMemoryCleanupInterval() + " 分钟");
        }
    }
    
    public void stopMonitoring() {
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
        if (memoryCleanupTask != null) {
            memoryCleanupTask.cancel();
            memoryCleanupTask = null;
        }
        plugin.getLogger().info("插件内存监控已停止");
    }
    
    private void performMemoryCheck() {
        try {
            long currentPluginMemory = calculatePluginMemoryUsage();
            pluginMemoryUsed.set(currentPluginMemory);
            maxPluginMemoryUsed.updateAndGet(max -> Math.max(max, currentPluginMemory));
            monitorCycles++;

            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info(String.format(
                    "插件内存检查 [周期 %d]: 当前=%s, 最大=%s, 基准=%s",
                    monitorCycles,
                    formatBytes(currentPluginMemory),
                    formatBytes(maxPluginMemoryUsed.get()),
                    formatBytes(baselineMemory)
                ));
            }

            if (monitorCycles >= leakCheckCycles) {
                checkForMemoryLeak(currentPluginMemory);
            }

            // 检查内存限制
            if (currentPluginMemory > memoryLimit) {
                plugin.getLogger().warning(String.format(
                    "插件内存使用超过限制! 当前: %s, 限制: %s",
                    formatBytes(currentPluginMemory),
                    formatBytes(memoryLimit)
                ));
                if (autoCleanupEnabled) {
                    performMemoryCleanup();
                }
            }

            checkMemoryStatistics();

        } catch (Exception e) {
            plugin.getLogger().warning("内存检查错误: " + e.getMessage());
        }
    }
    
    private void checkForMemoryLeak(long currentPluginMemory) {
        long memoryIncrease = currentPluginMemory - baselineMemory;

        if (memoryIncrease > memoryLeakThreshold) {
            plugin.getLogger().warning(String.format(
                "检测到潜在内存泄漏! 内存增长: %s (阈值: %s)",
                formatBytes(memoryIncrease),
                formatBytes(memoryLeakThreshold)
            ));

            logDetailedMemoryStats();
            performMemoryCleanup();
            baselineMemory = calculatePluginMemoryUsage();
            monitorCycles = 0; // 重置周期计数
        }
    }
    
    private void checkMemoryStatistics() {
        try {
            GateManager gateManager = plugin.getGateManager();
            SpatialIndexManager spatialManager = gateManager.getSpatialIndexManager();
            int gateCount = gateManager.getAllGates().size();
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info(String.format(
                    "Plugin stats: gates=%d, spatial=%s",
                    gateCount,
                    spatialManager.getStats().replace("\n", ", ")
                ));
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Memory stats error: " + e.getMessage());
        }
    }
    
    private void logDetailedMemoryStats() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        plugin.getLogger().warning("=== Detailed Memory Stats ===");
        plugin.getLogger().warning(String.format(
            "Heap: used=%s, committed=%s, max=%s",
            formatBytes(heapUsage.getUsed()),
            formatBytes(heapUsage.getCommitted()),
            formatBytes(heapUsage.getMax())
        ));
        plugin.getLogger().warning(String.format(
            "Non-heap: used=%s, committed=%s",
            formatBytes(nonHeapUsage.getUsed()),
            formatBytes(nonHeapUsage.getCommitted())
        ));
    }
    
    private long getCurrentHeapUsage() {
        return memoryBean.getHeapMemoryUsage().getUsed();
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    public String getMemoryReport() {
        long currentPluginMemory = calculatePluginMemoryUsage();
        long runTime = (System.currentTimeMillis() - monitoringStartTime.get()) / 60000;

        return String.format(
            "插件内存报告: 当前=%s, 最大=%s, 基准=%s, 运行时间=%d 分钟, 周期=%d",
            formatBytes(currentPluginMemory),
            formatBytes(maxPluginMemoryUsed.get()),
            formatBytes(baselineMemory),
            runTime,
            monitorCycles
        );
    }

    /**
     * 计算插件内存使用量
     * 通过统计插件相关对象的内存占用来估算
     */
    private long calculatePluginMemoryUsage() {
        try {
            long memoryUsage = 0;

            // 统计传送门数据内存
            GateManager gateManager = plugin.getGateManager();
            if (gateManager != null) {
                int gateCount = gateManager.getAllGates().size();
                // 估算每个传送门约占用 2KB 内存
                memoryUsage += gateCount * 2048;

                // 统计空间索引内存
                SpatialIndexManager spatialManager = gateManager.getSpatialIndexManager();
                if (spatialManager != null) {
                    // 估算空间索引内存使用（基于传送门数量）
                    memoryUsage += gateCount * 1024; // 每个传送门的索引约占用 1KB
                }
            }

            // 统计传送服务内存
            TeleportService teleportService = plugin.getTeleportService();
            if (teleportService != null) {
                // 估算传送任务和确认状态的内存使用
                memoryUsage += 1024 * 10; // 基础内存占用约 10KB
            }

            // 添加基础插件内存占用
            memoryUsage += 1024 * 1024; // 基础占用约 1MB

            return memoryUsage;

        } catch (Exception e) {
            plugin.getLogger().warning("计算插件内存使用量时出错: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 执行内存清理
     */
    private void performMemoryCleanup() {
        try {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("开始执行内存清理...");
            }

            long beforeCleanup = calculatePluginMemoryUsage();

            // 清理空间索引缓存
            GateManager gateManager = plugin.getGateManager();
            if (gateManager != null) {
                SpatialIndexManager spatialManager = gateManager.getSpatialIndexManager();
                if (spatialManager != null) {
                    // 重建空间索引以清理碎片
                    spatialManager.clear();
                    for (var gate : gateManager.getAllGates().values()) {
                        spatialManager.addGate(gate);
                    }
                }
            }

            // 执行垃圾回收
            System.gc();

            // 等待一小段时间让GC完成
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long afterCleanup = calculatePluginMemoryUsage();
            long cleaned = beforeCleanup - afterCleanup;

            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info(String.format(
                    "内存清理完成: 清理前=%s, 清理后=%s, 释放=%s",
                    formatBytes(beforeCleanup),
                    formatBytes(afterCleanup),
                    formatBytes(cleaned)
                ));
            }

        } catch (Exception e) {
            plugin.getLogger().warning("内存清理时出错: " + e.getMessage());
        }
    }
}
