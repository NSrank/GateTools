package org.plugin.gatetools.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.model.GateCondition;
import org.plugin.gatetools.util.MessageUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final GateTools plugin;
    private FileConfiguration messagesConfig;
    private final Map<String, String> operatorTranslations;
    
    public MessageManager(GateTools plugin) {
        this.plugin = plugin;
        this.operatorTranslations = new HashMap<>();
        loadMessages();
        loadOperatorTranslations();
    }
    
    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                InputStream inputStream = plugin.getResource("messages.yml");
                if (inputStream != null) {
                    Files.copy(inputStream, messagesFile.toPath());
                    inputStream.close();
                    plugin.getLogger().info("Created messages.yml file from resources");
                } else {
                    plugin.getLogger().warning("messages.yml resource not found in plugin jar");
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Cannot create messages.yml file: " + e.getMessage());
            }
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        plugin.getLogger().info("Loaded message configuration file from: " + messagesFile.getAbsolutePath());

        // 验证配置是否正确加载
        if (messagesConfig.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("messages.yml appears to be empty or invalid!");
        } else {
            plugin.getLogger().info("Loaded " + messagesConfig.getKeys(true).size() + " message keys");
        }
    }
    
    private void loadOperatorTranslations() {
        operatorTranslations.clear();
        if (messagesConfig.isConfigurationSection("operators")) {
            for (String key : messagesConfig.getConfigurationSection("operators").getKeys(false)) {
                String translation = messagesConfig.getString("operators." + key, key);
                operatorTranslations.put(key, translation);
            }
        }
        
        operatorTranslations.putIfAbsent("=", "required");
        operatorTranslations.putIfAbsent("!=", "not required");
        operatorTranslations.putIfAbsent(">", "greater than");
        operatorTranslations.putIfAbsent(">=", "greater than or equal");
        operatorTranslations.putIfAbsent("<", "less than");
        operatorTranslations.putIfAbsent("<=", "less than or equal");
    }
    
    public String getMessage(String key) {
        String prefix = messagesConfig.getString("prefix", "&8[&6GateTools&8] &r");
        String message = getRawMessage(key);
        return MessageUtil.colorize(prefix + message);
    }
    
    public String getMessageWithoutPrefix(String key) {
        return MessageUtil.colorize(getRawMessage(key));
    }
    
    private String getRawMessage(String key) {
        // 首先尝试直接获取键值
        String message = messagesConfig.getString(key);
        if (message != null) {
            return message;
        }

        // 如果直接获取失败，尝试处理层级键名（如 error.no-permission）
        if (key.contains(".")) {
            String[] parts = key.split("\\.", 2);
            if (parts.length == 2) {
                String section = parts[0];
                String subKey = parts[1];
                if (messagesConfig.isConfigurationSection(section)) {
                    message = messagesConfig.getString(section + "." + subKey);
                    if (message != null) {
                        return message;
                    }
                }
            }
        }

        // 如果都找不到，返回默认错误消息
        return "&cMessage not found: " + key;
    }
    
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        return replacePlaceholders(message, placeholders);
    }
    
    public String getMessageWithoutPrefix(String key, Map<String, String> placeholders) {
        String message = getMessageWithoutPrefix(key);
        return replacePlaceholders(message, placeholders);
    }
    
    private String replacePlaceholders(String message, Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) {
            return message;
        }
        
        String result = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = "%" + entry.getKey() + "%";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }
    
    public String translateOperator(String operator) {
        return operatorTranslations.getOrDefault(operator, operator);
    }
    
    public String translateOperator(GateCondition.CompareOperator operator) {
        if (operator == null) return "";
        return translateOperator(operator.getSymbol());
    }
    
    public void reload() {
        loadMessages();
        loadOperatorTranslations();
    }

    /**
     * 调试方法：测试消息键是否存在
     */
    public boolean hasMessage(String key) {
        return messagesConfig.contains(key);
    }

    /**
     * 调试方法：获取所有可用的消息键
     */
    public java.util.Set<String> getAllMessageKeys() {
        return messagesConfig.getKeys(true);
    }
}
