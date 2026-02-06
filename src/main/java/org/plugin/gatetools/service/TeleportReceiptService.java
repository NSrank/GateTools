package org.plugin.gatetools.service;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.plugin.gatetools.GateTools;
import org.plugin.gatetools.model.Gate;
import org.plugin.gatetools.util.MessageUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 传送凭证服务
 * 负责生成传送完成后的凭证物品
 * 
 * @author NSrank, Augment
 */
public class TeleportReceiptService {
    private final GateTools plugin;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    
    public TeleportReceiptService(GateTools plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 为玩家生成传送凭证
     * 
     * @param player 传送的玩家
     * @param gate 使用的传送门
     * @param cost 传送花费的金额
     */
    public void giveReceiptToPlayer(Player player, Gate gate, double cost) {
        if (!gate.isLogEnabled()) {
            return; // 传送门未启用日志功能
        }
        
        ItemStack receipt = createReceipt(player, gate, cost);
        
        // 尝试将凭证添加到玩家背包
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(receipt);
            player.sendMessage(MessageUtil.colorize("&a您已获得传送凭证！"));
        } else {
            // 背包满了，掉落在玩家脚下
            player.getWorld().dropItemNaturally(player.getLocation(), receipt);
            player.sendMessage(MessageUtil.colorize("&e背包已满，传送凭证已掉落在您脚下！"));
        }
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("为玩家 " + player.getName() + " 生成传送凭证: " + gate.getDisplayName());
        }
    }
    
    /**
     * 创建传送凭证物品
     * 
     * @param player 传送的玩家
     * @param gate 使用的传送门
     * @param cost 传送花费的金额
     * @return 传送凭证物品
     */
    private ItemStack createReceipt(Player player, Gate gate, double cost) {
        ItemStack receipt = new ItemStack(Material.PAPER);
        ItemMeta meta = receipt.getItemMeta();
        
        if (meta != null) {
            // 设置物品名称
            meta.setDisplayName(MessageUtil.colorize("&6&l[GateTools传送凭证]"));
            
            // 设置物品描述
            String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
            String costText = cost > 0 ?
                (plugin.getConditionService().hasEconomy() ? String.format("%.2f", cost) : String.valueOf(cost)) :
                "免费";
            
            List<String> lore = Arrays.asList(
                MessageUtil.colorize("&7传送操作者: &f" + player.getName()),
                MessageUtil.colorize("&7传送门: &f" + gate.getDisplayName()),
                MessageUtil.colorize("&7传送时间: &f" + currentTime),
                MessageUtil.colorize("&7传送花费: &f" + costText),
                "",
                MessageUtil.colorize("&8此凭证证明您已完成传送操作"),
                MessageUtil.colorize("&8请妥善保管，不可复制")
            );
            meta.setLore(lore);
            
            // 添加附魔以确保独特性，但隐藏所有额外信息
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            meta.addItemFlags(
                org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS,
                org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES,
                org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE,
                org.bukkit.inventory.ItemFlag.HIDE_DESTROYS,
                org.bukkit.inventory.ItemFlag.HIDE_PLACED_ON
            );
            
            // 设置高修复花费以防止复制（使用Repairable接口）
            if (meta instanceof org.bukkit.inventory.meta.Repairable) {
                ((org.bukkit.inventory.meta.Repairable) meta).setRepairCost(999999);
            }
            
            receipt.setItemMeta(meta);
        }
        
        return receipt;
    }
    
    /**
     * 检查物品是否为有效的传送凭证
     * 
     * @param item 要检查的物品
     * @return 是否为有效的传送凭证
     */
    public boolean isValidReceipt(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        String displayName = meta.displayName() != null ?
            net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(meta.displayName()) :
            (meta.hasDisplayName() ? meta.getDisplayName() : "");

        boolean hasCorrectRepairCost = true;
        if (meta instanceof org.bukkit.inventory.meta.Repairable) {
            hasCorrectRepairCost = ((org.bukkit.inventory.meta.Repairable) meta).getRepairCost() == 999999;
        }

        return displayName.contains("[GateTools传送凭证]") &&
               meta.hasEnchant(Enchantment.PROTECTION_ENVIRONMENTAL) &&
               hasCorrectRepairCost;
    }
}
