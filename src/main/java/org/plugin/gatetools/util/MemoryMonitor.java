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

public class MemoryMonitor {
    private final GateTools plugin;
    private final MemoryMXBean memoryBean;
    private final AtomicLong lastHeapUsed = new AtomicLong(0);
    private final AtomicLong maxHeapUsed = new AtomicLong(0);
    private final AtomicLong monitoringStartTime = new AtomicLong(System.currentTimeMillis());
    private BukkitTask monitorTask;
    
    private static final long MEMORY_LEAK_THRESHOLD = 50 * 1024 * 1024; // 50MB
    private static final int MONITORING_INTERVAL = 300; // 5 minutes
    private static final int LEAK_CHECK_CYCLES = 6; // 30 minutes
    
    private int monitorCycles = 0;
    private long baselineMemory = 0;
    
    public MemoryMonitor(GateTools plugin) {
        this.plugin = plugin;
        this.memoryBean = ManagementFactory.getMemoryMXBean();
    }
    
    public void startMonitoring() {
        if (monitorTask != null) {
            monitorTask.cancel();
        }
        
        baselineMemory = getCurrentHeapUsage();
        monitoringStartTime.set(System.currentTimeMillis());
        
        monitorTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
            plugin, this::performMemoryCheck, 
            MONITORING_INTERVAL * 20L,
            MONITORING_INTERVAL * 20L
        );
        
        plugin.getLogger().info("Memory monitoring started, baseline: " + formatBytes(baselineMemory));
    }
    
    public void stopMonitoring() {
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
        plugin.getLogger().info("Memory monitoring stopped");
    }
    
    private void performMemoryCheck() {
        try {
            long currentHeap = getCurrentHeapUsage();
            lastHeapUsed.set(currentHeap);
            maxHeapUsed.updateAndGet(max -> Math.max(max, currentHeap));
            monitorCycles++;
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info(String.format(
                    "Memory check [cycle %d]: current=%s, max=%s, baseline=%s",
                    monitorCycles,
                    formatBytes(currentHeap),
                    formatBytes(maxHeapUsed.get()),
                    formatBytes(baselineMemory)
                ));
            }
            
            if (monitorCycles >= LEAK_CHECK_CYCLES) {
                checkForMemoryLeak(currentHeap);
            }
            
            checkMemoryStatistics();
            
        } catch (Exception e) {
            plugin.getLogger().warning("Memory check error: " + e.getMessage());
        }
    }
    
    private void checkForMemoryLeak(long currentHeap) {
        long memoryIncrease = currentHeap - baselineMemory;
        
        if (memoryIncrease > MEMORY_LEAK_THRESHOLD) {
            plugin.getLogger().warning(String.format(
                "Potential memory leak detected! Memory increase: %s (threshold: %s)",
                formatBytes(memoryIncrease),
                formatBytes(MEMORY_LEAK_THRESHOLD)
            ));
            
            logDetailedMemoryStats();
            System.gc();
            baselineMemory = getCurrentHeapUsage();
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
        long currentHeap = getCurrentHeapUsage();
        long runTime = (System.currentTimeMillis() - monitoringStartTime.get()) / 60000;
        
        return String.format(
            "Memory report: current=%s, max=%s, baseline=%s, runtime=%d min, cycles=%d",
            formatBytes(currentHeap),
            formatBytes(maxHeapUsed.get()),
            formatBytes(baselineMemory),
            runTime,
            monitorCycles
        );
    }
}
