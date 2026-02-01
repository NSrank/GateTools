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

package org.plugin.gatetools;

import org.bukkit.plugin.java.JavaPlugin;
import org.plugin.gatetools.command.GateToolsCommand;
import org.plugin.gatetools.config.ConfigManager;
import org.plugin.gatetools.listener.PlayerMovementListener;
import org.plugin.gatetools.manager.GateManager;
import org.plugin.gatetools.service.ConditionService;
import org.plugin.gatetools.service.TeleportService;
import org.plugin.gatetools.util.MemoryMonitor;

/**
 * GateTools主类
 * 传送门插件的核心类
 *
 * @author NSrank, Augment
 */
public final class GateTools extends JavaPlugin {

    private ConfigManager configManager;
    private GateManager gateManager;
    private ConditionService conditionService;
    private TeleportService teleportService;
    private MemoryMonitor memoryMonitor;

    @Override
    public void onEnable() {
        // 初始化管理器
        this.configManager = new ConfigManager(this);
        this.gateManager = new GateManager(this, configManager);
        this.conditionService = new ConditionService(this, configManager);
        this.teleportService = new TeleportService(this, configManager, conditionService);
        this.memoryMonitor = new MemoryMonitor(this);

        // 注册命令
        GateToolsCommand commandExecutor = new GateToolsCommand(this);
        getCommand("gatetools").setExecutor(commandExecutor);
        getCommand("gatetools").setTabCompleter(commandExecutor);

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(
            new PlayerMovementListener(this, gateManager, teleportService), this);

        // 启动内存监控
        memoryMonitor.startMonitoring();

        getLogger().info("===================================");
        getLogger().info("GateTools v1.1 已加载");
        getLogger().info("作者: NSrank & Augment");
        getLogger().info("===================================");
    }

    @Override
    public void onDisable() {
        // 停止内存监控
        if (memoryMonitor != null) {
            memoryMonitor.stopMonitoring();
        }

        // 保存数据
        if (gateManager != null) {
            gateManager.saveGates();
        }

        getLogger().info("GateTools 插件已禁用！");
    }

    // Getters
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GateManager getGateManager() {
        return gateManager;
    }

    public ConditionService getConditionService() {
        return conditionService;
    }

    public TeleportService getTeleportService() {
        return teleportService;
    }

    public MemoryMonitor getMemoryMonitor() {
        return memoryMonitor;
    }
}
