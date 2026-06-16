package com.shadowfang.core.worldedit;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class WeTool {

    private static final Material WAND_MATERIAL = Material.WOODEN_SHOVEL;
    private static final int CUSTOM_MODEL_DATA = 2001;
    private static volatile NamespacedKey WAND_KEY;

    private static NamespacedKey key(Plugin plugin) {
        NamespacedKey k = WAND_KEY;
        if (k == null) {
            synchronized (WeTool.class) {
                k = WAND_KEY;
                if (k == null) {
                    k = WAND_KEY = new NamespacedKey(plugin, "we_wand");
                }
            }
        }
        return k;
    }

    public static ItemStack createWand(Plugin plugin) {
        ItemStack wand = new ItemStack(WAND_MATERIAL);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Path Wand");
            meta.setLore(List.of(
                    "§7Left-click: select §epos1",
                    "§7Right-click: select §epos2",
                    "§7/sr r copy §8- §7capture pattern",
                    "§7/sr r paste §8- §7place pattern",
                    "§7/sr r start §8- §7walk-paste mode",
                    "§7/sr r stop §8- §7exit walk-paste",
                    "§7/sr r undo §8- §7revert last action"
            ));
            meta.setCustomModelData(CUSTOM_MODEL_DATA);
            meta.getPersistentDataContainer().set(key(plugin), PersistentDataType.BYTE, (byte) 1);
            wand.setItemMeta(meta);
        }
        return wand;
    }

    public static boolean isWand(ItemStack item, Plugin plugin) {
        if (item == null || item.getType() != WAND_MATERIAL) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(key(plugin), PersistentDataType.BYTE);
    }
}
