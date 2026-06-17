package com.shadowfang.core.elevator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ElevatorMenu {

    private static final Set<UUID> openMenus = new HashSet<>();
    private static final TextColor GOLD = TextColor.color(0xFFD700);
    private static final TextColor DIM = TextColor.color(0x888888);

    public static Component getMenuTitle(String groupName) {
        return Component.text("\u2740 Teleporter: ").color(GOLD)
            .append(Component.text(groupName).color(NamedTextColor.WHITE));
    }

    public static boolean isElevatorMenu(UUID playerId) {
        return openMenus.contains(playerId);
    }

    public static void registerMenu(UUID playerId) {
        openMenus.add(playerId);
    }

    public static void unregisterMenu(UUID playerId) {
        openMenus.remove(playerId);
    }

    public static Inventory createMenu(Player player, List<ElevatorGroup.ElevatorFloor> floors, String groupName, int currentFloorIndex) {
        int size = Math.min(54, ((floors.size() / 9) + 1) * 9);
        if (size < 9) size = 9;

        Inventory inv = Bukkit.createInventory(null, size, getMenuTitle(groupName));

        for (int i = 0; i < floors.size(); i++) {
            ElevatorGroup.ElevatorFloor floor = floors.get(i);
            boolean isCurrentFloor = (i == currentFloorIndex);

            ItemStack item = new ItemStack(isCurrentFloor ? Material.BARRIER : Material.PAPER);
            ItemMeta meta = item.getItemMeta();

            String displayName = floor.getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = "Floor " + (i + 1);
            }

            if (isCurrentFloor) {
                meta.displayName(Component.text(displayName + " (Current)").color(NamedTextColor.GRAY));
            } else {
                meta.displayName(Component.text(displayName).color(NamedTextColor.YELLOW));
            }

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(floor.getWorld() + ": " + floor.getX() + ", " + floor.getY() + ", " + floor.getZ()).color(DIM));
            if (!isCurrentFloor) {
                lore.add(Component.text("Click to teleport").color(NamedTextColor.GREEN));
            }
            meta.lore(lore);

            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        return inv;
    }
}
