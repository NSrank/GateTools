package org.plugin.gatetools.config;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.util.MessageUtil;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
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

    // 内存管理配置方法
    public long getMemoryLimit() {
        return config.getLong("memory.limit", 100) * 1024 * 1024; // 转换为字节
    }

    public long getMemoryLeakThreshold() {
        return config.getLong("memory.leak-threshold", 50) * 1024 * 1024; // 转换为字节
    }

    public int getMemoryMonitorInterval() {
        return config.getInt("memory.monitor-interval", 300); // 秒
    }

    public int getMemoryLeakCheckCycles() {
        return config.getInt("memory.leak-check-cycles", 6);
    }

    public boolean isMemoryAutoCleanupEnabled() {
        return config.getBoolean("memory.auto-cleanup", true);
    }

    public int getMemoryCleanupInterval() {
        return config.getInt("memory.cleanup-interval", 10); // 分钟
    }

    // 经济系统配置方法
    public String getRecipientAccount() {
        return config.getString("economy.recipient-account", "");
    }

    public boolean isTransferLoggingEnabled() {
        return config.getBoolean("economy.transfer-logging", true);
    }

    public boolean isRecipientNotificationEnabled() {
        return config.getBoolean("economy.notify-recipient", true);
    }

    /**
     * 解析收款账户，支持UUID和玩家名自动识别
     *
     * @return 收款账户的OfflinePlayer对象，如果未配置或无效则返回null
     */
    public OfflinePlayer getRecipientPlayer() {
        String account = getRecipientAccount();
        if (account == null || account.trim().isEmpty()) {
            return null;
        }

        account = account.trim();

        try {
            // 尝试解析为UUID
            if (account.length() == 36 && account.contains("-")) {
                UUID uuid = UUID.fromString(account);
                return Bukkit.getOfflinePlayer(uuid);
            }

            // 尝试解析为没有连字符的UUID
            if (account.length() == 32 && !account.contains("-")) {
                String formattedUuid = account.substring(0, 8) + "-" +
                                     account.substring(8, 12) + "-" +
                                     account.substring(12, 16) + "-" +
                                     account.substring(16, 20) + "-" +
                                     account.substring(20, 32);
                UUID uuid = UUID.fromString(formattedUuid);
                return Bukkit.getOfflinePlayer(uuid);
            }

            // 作为玩家名处理
            return Bukkit.getOfflinePlayer(account);

        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("无效的收款账户配置: " + account + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * 验证收款账户是否有效
     *
     * @return 如果收款账户有效则返回true
     */
    public boolean isRecipientAccountValid() {
        OfflinePlayer recipient = getRecipientPlayer();
        return recipient != null && (recipient.hasPlayedBefore() || recipient.isOnline());
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
