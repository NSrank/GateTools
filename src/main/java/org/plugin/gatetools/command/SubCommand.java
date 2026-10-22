package org.plugin.gatetools.command;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * 子命令接口
 * 定义子命令的基本结构
 * 
 * @author NSrank, Augment
 */
public interface SubCommand {
    
    /**
     * 获取命令名称
     * 
     * @return 命令名称
     */
    String getName();
    
    /**
     * 获取命令描述
     * 
     * @return 命令描述
     */
    String getDescription();
    
    /**
     * 获取命令用法
     * 
     * @return 命令用法
     */
    String getUsage();
    
    /**
     * 获取命令别名
     * 
     * @return 命令别名列表
     */
    List<String> getAliases();
    
    /**
     * 获取所需权限
     * 
     * @return 权限节点
     */
    String getPermission();
    
    /**
     * 检查发送者是否有权限执行此命令
     * 
     * @param sender 命令发送者
     * @return 是否有权限
     */
    boolean hasPermission(CommandSender sender);
    
    /**
     * 执行命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     */
    void execute(CommandSender sender, String[] args);
    
    /**
     * Tab补全
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 补全列表
     */
    List<String> onTabComplete(CommandSender sender, String[] args);
}
