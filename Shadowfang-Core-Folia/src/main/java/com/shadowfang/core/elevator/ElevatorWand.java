package com.shadowfang.core.elevator;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ElevatorWand {

    private static final int CUSTOM_MODEL_DATA = 2001;
    private static final String WAND_TYPE_KEY = "wand_type";
    private static final String ELEVATOR_WAND_TYPE = "elevator_wand";
    private static final String ELEVATOR_GROUP_KEY = "elevator_group";

    public static ItemStack createWand(String groupName) {
        ItemStack wand = new ItemStack(Material.WOODEN_SHOVEL);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName("§6Teleport Wand");
        meta.setLore(java.util.Arrays.asList(
            "§7Right-click a block to assign it",
            "§7as a teleporter floor.",
            "§7Left-click a floor to remove it.",
            "",
            "§7Group: §e" + groupName
        ));
        meta.getPersistentDataContainer().set(
            new NamespacedKey(getPlugin(), WAND_TYPE_KEY),
            PersistentDataType.STRING,
            ELEVATOR_WAND_TYPE
        );
        meta.getPersistentDataContainer().set(
            new NamespacedKey(getPlugin(), ELEVATOR_GROUP_KEY),
            PersistentDataType.STRING,
            groupName.toLowerCase()
        );
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        wand.setItemMeta(meta);
        return wand;
    }

    public static boolean isWand(ItemStack item) {
        if (item == null || item.getType() != Material.WOODEN_SHOVEL) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return false;
        if (meta.getCustomModelData() != CUSTOM_MODEL_DATA) return false;
        String type = meta.getPersistentDataContainer().get(
            new NamespacedKey(getPlugin(), WAND_TYPE_KEY),
            PersistentDataType.STRING
        );
        return ELEVATOR_WAND_TYPE.equals(type);
    }

    public static String getGroupName(ItemStack item) {
        if (!isWand(item)) return null;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(
            new NamespacedKey(getPlugin(), ELEVATOR_GROUP_KEY),
            PersistentDataType.STRING
        );
    }

    public static void updateWandGroup(ItemStack item, String groupName) {
        if (!isWand(item)) return;
        ItemMeta meta = item.getItemMeta();
        java.util.List<String> lore = meta.getLore();
        if (lore != null && lore.size() >= 5) {
            lore.set(lore.size() - 1, "§7Group: §e" + groupName);
            meta.setLore(lore);
        }
        meta.getPersistentDataContainer().set(
            new NamespacedKey(getPlugin(), ELEVATOR_GROUP_KEY),
            PersistentDataType.STRING,
            groupName.toLowerCase()
        );
        item.setItemMeta(meta);
    }

    private static com.shadowfang.core.ShadowfangCorePlugin getPlugin() {
        return com.shadowfang.core.ShadowfangCorePlugin.getInstance();
    }
}
