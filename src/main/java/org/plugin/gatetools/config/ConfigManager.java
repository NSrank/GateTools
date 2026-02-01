package org.plugin.gatetools.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.util.MessageUtil;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * 配置文件管理器
 * 负责管理config.yml和data.yml文件
 * 
 * @author NSrank, Augment
 */
public class ConfigManager {
    private final GateTools plugin;
    private FileConfiguration config;
    private FileConfiguration dataConfig;
    private File dataFile;
    private MessageManager messageManager;
    
    public ConfigManager(GateTools plugin) {
        this.plugin = plugin;
        loadConfigs();
        this.messageManager = new MessageManager(plugin);
    }
    
    /**
     * 加载配置文件
     */
    public void loadConfigs() {
        // 保存默认配置文件
        plugin.saveDefaultConfig();
        
        // 加载主配置文件
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // 创建并加载数据文件
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "无法创建数据文件", e);
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    /**
     * 重载配置文件
     */
    public void reloadConfigs() {
        loadConfigs();
        if (messageManager != null) {
            messageManager.reload();
        }
    }
    
    /**
     * 保存数据文件
     */
    public void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "无法保存数据文件", e);
        }
    }
    
    /**
     * 异步保存数据文件
     */
    public void saveDataAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveData);
    }
    
    // 配置获取方法
    public int getMaxGates() {
        return config.getInt("settings.max-gates", 50);
    }
    
    public int getDeleteConfirmTimeout() {
        return config.getInt("settings.delete-confirm-timeout", 30);
    }
    
    public int getTeleportDelay() {
        return config.getInt("settings.teleport-delay", 60);
    }
    
    public int getAutoSaveInterval() {
        return config.getInt("settings.auto-save-interval", 5);
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("settings.debug", false);
    }
    
    // 消息获取方法
    public String getMessage(String key) {
        if (messageManager != null) {
            return messageManager.getMessage(key);
        }
        // 回退到旧方法
        String prefix = config.getString("messages.prefix", "&8[&6GateTools&8] &r");
        String message = config.getString("messages." + key, "&c消息未找到: " + key);
        return prefix + message;
    }

    public String getMessageWithoutPrefix(String key) {
        if (messageManager != null) {
            return messageManager.getMessageWithoutPrefix(key);
        }
        // 回退到旧方法
        return config.getString("messages." + key, "&c消息未找到: " + key);
    }
    
    // 确认界面配置 - 从MessageManager获取
    public String getYesButton() {
        if (messageManager != null) {
            return messageManager.getMessageWithoutPrefix("confirmation.yes-button");
        }
        return "&a[是]";
    }

    public String getNoButton() {
        if (messageManager != null) {
            return messageManager.getMessageWithoutPrefix("confirmation.no-button");
        }
        return "&c[否]";
    }

    public String getYesHover() {
        if (messageManager != null) {
            return messageManager.getMessageWithoutPrefix("confirmation.yes-hover");
        }
        return "&a点击确认传送";
    }

    public String getNoHover() {
        if (messageManager != null) {
            return messageManager.getMessageWithoutPrefix("confirmation.no-hover");
        }
        return "&c点击取消传送";
    }

    public String getYesCommand() {
        if (messageManager != null) {
            return messageManager.getMessageWithoutPrefix("confirmation.yes-command");
        }
        return "/gatetools confirm-teleport %gate_name%";
    }

    public String getNoCommand() {
        if (messageManager != null) {
            return messageManager.getMessageWithoutPrefix("confirmation.no-command");
        }
        return "/gatetools cancel-teleport %gate_name%";
    }
    
    // Getters
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getDataConfig() {
        return dataConfig;
    }
    
    public File getDataFile() {
        return dataFile;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    /**
     * 将颜色代码转换为Minecraft颜色
     *
     * @param message 包含颜色代码的消息
     * @return 转换后的消息
     */
    public String colorize(String message) {
        return MessageUtil.colorize(message);
    }
}
