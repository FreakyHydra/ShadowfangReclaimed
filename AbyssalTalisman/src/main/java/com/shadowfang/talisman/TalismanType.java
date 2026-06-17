package com.shadowfang.talisman;

import org.bukkit.Material;

public enum TalismanType {
    ABYSSAL(Material.BLAZE_ROD, 5, 0.10, "Abyssal", "§6", "§8the deep calls back"),
    HOLLOW(Material.ECHO_SHARD, 5, 0.15, "Hollow", "§3", "§8what was taken cannot be returned"),
    CRIMSON(Material.GHAST_TEAR, 7, 0.12, "Crimson", "§c", "§cblood for blood, always"),
    VERDANT(Material.SPORE_BLOSSOM, 3, 0.08, "Verdant", "§a", "§athe garden gives, the garden takes"),
    FROSTBOUND(Material.BLUE_ICE, 5, 0.10, "Frostbound", "§b", "§8stillness is a kind of death");

    public final Material baseMaterial;
    public final int fragmentCost;
    public final double cursedChance;
    public final String displayName;
    public final String color;
    public final String subtitle;

    TalismanType(Material baseMaterial, int fragmentCost, double cursedChance, String displayName, String color, String subtitle) {
        this.baseMaterial = baseMaterial;
        this.fragmentCost = fragmentCost;
        this.cursedChance = cursedChance;
        this.displayName = displayName;
        this.color = color;
        this.subtitle = subtitle;
    }
}
