package org.plugin.gatetools.util;

import org.bukkit.ChatColor;

/**
 * 消息工具类
 * 负责处理消息的颜色代码转换等功能
 * 
 * @author NSrank, Augment
 */
public class MessageUtil {
    
    /**
     * 将颜色代码转换为Minecraft颜色
     * 
     * @param message 包含颜色代码的消息
     * @return 转换后的消息
     */
    public static String colorize(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * 移除颜色代码
     * 
     * @param message 包含颜色代码的消息
     * @return 移除颜色代码后的消息
     */
    public static String stripColor(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.stripColor(colorize(message));
    }
    
    /**
     * 替换消息中的占位符
     * 
     * @param message 原始消息
     * @param placeholder 占位符
     * @param replacement 替换内容
     * @return 替换后的消息
     */
    public static String replacePlaceholder(String message, String placeholder, String replacement) {
        if (message == null || placeholder == null || replacement == null) {
            return message;
        }
        return message.replace(placeholder, replacement);
    }
    
    /**
     * 替换消息中的多个占位符
     * 
     * @param message 原始消息
     * @param placeholders 占位符数组（偶数索引为占位符，奇数索引为替换内容）
     * @return 替换后的消息
     */
    public static String replacePlaceholders(String message, String... placeholders) {
        if (message == null || placeholders == null || placeholders.length % 2 != 0) {
            return message;
        }
        
        String result = message;
        for (int i = 0; i < placeholders.length; i += 2) {
            result = replacePlaceholder(result, placeholders[i], placeholders[i + 1]);
        }
        return result;
    }
    
    /**
     * 格式化数字为字符串
     * 
     * @param number 数字
     * @return 格式化后的字符串
     */
    public static String formatNumber(double number) {
        if (number == (long) number) {
            return String.valueOf((long) number);
        } else {
            return String.format("%.2f", number);
        }
    }
    
    /**
     * 检查字符串是否为空或null
     * 
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 安全地获取字符串，如果为null则返回默认值
     * 
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 安全的字符串
     */
    public static String safe(String str, String defaultValue) {
        return str != null ? str : defaultValue;
    }
}
