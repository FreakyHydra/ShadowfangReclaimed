package com.shadowfang.talisman;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class TalismanItem {

    public static final String NAMESPACE = "shadowfang";
    public static final NamespacedKey TALISMAN_KEY = new NamespacedKey(NAMESPACE, "talisman");
    public static final NamespacedKey TALISMAN_TYPE_KEY = new NamespacedKey(NAMESPACE, "talisman_type");
    public static final NamespacedKey TALISMAN_MODE_KEY = new NamespacedKey(NAMESPACE, "talisman_mode");
    public static final NamespacedKey TALISMAN_CURSED_KEY = new NamespacedKey(NAMESPACE, "talisman_cursed");

    public static boolean isTalisman(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(TALISMAN_KEY, PersistentDataType.BYTE);
    }

    public static TalismanType getType(ItemStack item) {
        if (!isTalisman(item)) return null;
        ItemMeta meta = item.getItemMeta();
        String name = meta.getPersistentDataContainer().get(TALISMAN_TYPE_KEY, PersistentDataType.STRING);
        if (name == null) return null;
        try {
            return TalismanType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static TalismanMode getMode(ItemStack item) {
        if (!isTalisman(item)) return TalismanMode.PRIMARY;
        ItemMeta meta = item.getItemMeta();
        Byte b = meta.getPersistentDataContainer().get(TALISMAN_MODE_KEY, PersistentDataType.BYTE);
        if (b == null) return TalismanMode.PRIMARY;
        return TalismanMode.fromIndex(b.intValue());
    }

    public static boolean isCursed(ItemStack item) {
        if (!isTalisman(item)) return false;
        ItemMeta meta = item.getItemMeta();
        Byte b = meta.getPersistentDataContainer().get(TALISMAN_CURSED_KEY, PersistentDataType.BYTE);
        return b != null && b == 1;
    }

    public static void setMode(ItemStack item, TalismanMode mode) {
        if (!isTalisman(item)) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(TALISMAN_MODE_KEY, PersistentDataType.BYTE, (byte) mode.index);
        item.setItemMeta(meta);
    }

    public static ItemStack createTalisman(TalismanType type, boolean cursed) {
        ItemStack item = new ItemStack(type.baseMaterial);
        ItemMeta meta = item.getItemMeta();

        String prefix = cursed ? "§4§l" : type.color + "§l";
        meta.setDisplayName(prefix + type.displayName + " Talisman");

        TalismanMode currentMode = TalismanMode.PRIMARY;
        List<String> lore = new ArrayList<>();
        lore.add("§8" + type.subtitle);
        lore.add("");
        appendModeLore(lore, type, currentMode, cursed);
        lore.add("");
        lore.add("§7§oEvery gift demands a price");

        meta.setLore(lore);
        meta.addEnchant(Enchantment.LURE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        meta.getPersistentDataContainer().set(TALISMAN_KEY, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(TALISMAN_TYPE_KEY, PersistentDataType.STRING, type.name());
        meta.getPersistentDataContainer().set(TALISMAN_MODE_KEY, PersistentDataType.BYTE, (byte) currentMode.index);
        meta.getPersistentDataContainer().set(TALISMAN_CURSED_KEY, PersistentDataType.BYTE, (byte) (cursed ? 1 : 0));

        item.setItemMeta(meta);
        return item;
    }

    public static void appendModeLore(List<String> lore, TalismanType type, TalismanMode mode, boolean cursed) {
        String[] powerNames = getPowerNames(type);
        String[] powerDescs = getPowerDescriptions(type);
        String[] curseDescs = getCurseDescriptions(type);

        for (int i = 0; i < 3; i++) {
            TalismanMode m = TalismanMode.fromIndex(i);
            boolean isActive = m == mode;
            String prefix = isActive ? "§e▸ " : "§7  ";
            String line = prefix + powerNames[i] + " §8— " + (isActive ? getActiveCurseText(type, m, cursed) : "§7" + curseDescs[i]);
            lore.add(line);
        }
        lore.add("§8Hold off-hand §7· §8Sneak+Right-Click to switch");
    }

    public static String getActiveCurseText(TalismanType type, TalismanMode mode, boolean cursed) {
        String[][] curses = getCurseTextMatrix(type);
        String base = curses[mode.index][0];
        if (cursed && curses[mode.index].length > 1) {
            return base + " §8(§4§oamplified§8)";
        }
        return base;
    }

    public static String[] getPowerNames(TalismanType type) {
        return switch (type) {
            case ABYSSAL -> new String[]{"Vein", "Excavate", "Prospect"};
            case HOLLOW -> new String[]{"Fog", "Void", "Descent"};
            case CRIMSON -> new String[]{"Siphon", "Rend", "Fervor"};
            case VERDANT -> new String[]{"Sprout", "Canopy", "Bloom"};
            case FROSTBOUND -> new String[]{"Chill", "Glacier", "Aurora"};
        };
    }

    public static String[] getPowerDescriptions(TalismanType type) {
        return switch (type) {
            case ABYSSAL -> new String[]{
                "Chain-mine connected ores/trees (30b)",
                "Mine 3×3 soft blocks from clicked face",
                "Highlight ores through stone (12b, 45s CD)"
            };
            case HOLLOW -> new String[]{
                "Mobs can't detect you beyond 8b (10s)",
                "+50% damage, 3b knockback void pulse",
                "Slow-fall 8s, negates fall damage"
            };
            case CRIMSON -> new String[]{
                "Melee kills restore 2 hearts",
                "Attacks apply Bleeding (1dmg/2s × 6s)",
                "+2 attack damage (10s, 30s CD)"
            };
            case VERDANT -> new String[]{
                "Bone-meal 3×3 grass/flowers",
                "5×5 leaf canopy, negates sun (8s)",
                "Advance crops in 5b radius (60s CD)"
            };
            case FROSTBOUND -> new String[]{
                "Freeze water 3b radius (10s)",
                "Place 3×5 ice wall ahead (15s)",
                "Highlight mobs 20b through walls (5s)"
            };
        };
    }

    public static String[] getCurseDescriptions(TalismanType type) {
        return switch (type) {
            case ABYSSAL -> new String[]{
                "Hunger III 4s after vein break",
                "Mining Fatigue II 3s after use",
                "Night Vision suppressed while held"
            };
            case HOLLOW -> new String[]{
                "No name tags 15s after Fog",
                "1.5 hearts void recoil damage",
                "30% slower 10s after landing"
            };
            case CRIMSON -> new String[]{
                "Can't eat 20s after siphon kill",
                "0.5 hearts self-damage per Rend",
                "-2 max hearts 30s after Fervor ends"
            };
            case VERDANT -> new String[]{
                "Poison I 4s — spores irritate skin",
                "Slowness I while canopy active",
                "-1 hunger per Bloom trigger"
            };
            case FROSTBOUND -> new String[]{
                "Fire damage ×1.5 while ice lasts",
                "Movement halved 5s after glacier",
                "YOU are highlighted 10s after pulse"
            };
        };
    }

    public static String[][] getCurseTextMatrix(TalismanType type) {
        return switch (type) {
            case ABYSSAL -> new String[][]{
                {"Hunger III 4s", "Hunger IV 6s"},
                {"Mining Fatigue II 3s", "Mining Fatigue III 5s"},
                {"NV suppressed", "Blindness flicker 2s"}
            };
            case HOLLOW -> new String[][]{
                {"No name tags 15s", "No name tags 25s"},
                {"1.5 hearts void recoil", "2.5 hearts void recoil"},
                {"30% slower 10s", "50% slower 10s"}
            };
            case CRIMSON -> new String[][]{
                {"No food 20s", "No food 40s"},
                {"0.5 hearts self-dmg", "1 heart self-dmg"},
                {"-2 max hearts 30s", "-4 max hearts 30s"}
            };
            case VERDANT -> new String[][]{
                {"Poison I 4s", "Poison II 4s"},
                {"Slowness I", "Slowness II"},
                {"-1 hunger", "-2 hunger"}
            };
            case FROSTBOUND -> new String[][]{
                {"Fire ×1.5", "Fire ×2.0"},
                {"Movement halved 5s", "75% slower 5s"},
                {"Highlighted 10s", "Highlighted 15s"}
            };
        };
    }

    public static void refreshLore(ItemStack item) {
        if (!isTalisman(item)) return;
        TalismanType type = getType(item);
        TalismanMode mode = getMode(item);
        boolean cursed = isCursed(item);

        ItemMeta meta = item.getItemMeta();
        String prefix = cursed ? "§4§l" : type.color + "§l";
        meta.setDisplayName(prefix + type.displayName + " Talisman");

        List<String> lore = new ArrayList<>();
        lore.add("§8" + type.subtitle);
        lore.add("");
        appendModeLore(lore, type, mode, cursed);
        lore.add("");
        lore.add("§7§oEvery gift demands a price");

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static String serialize(ItemStack item) {
        TalismanType type = getType(item);
        TalismanMode mode = getMode(item);
        boolean cursed = isCursed(item);
        if (type == null) return null;
        return type.name() + ":" + mode.index + ":" + (cursed ? "1" : "0");
    }

    public static ItemStack deserialize(String data) {
        if (data == null || data.isEmpty()) return null;
        String[] parts = data.split(":");
        if (parts.length != 3) return null;
        try {
            TalismanType type = TalismanType.valueOf(parts[0]);
            int modeIndex = Integer.parseInt(parts[1]);
            boolean cursed = parts[2].equals("1");
            ItemStack item = createTalisman(type, cursed);
            setMode(item, TalismanMode.fromIndex(modeIndex));
            refreshLore(item);
            return item;
        } catch (Exception e) {
            return null;
        }
    }
}
