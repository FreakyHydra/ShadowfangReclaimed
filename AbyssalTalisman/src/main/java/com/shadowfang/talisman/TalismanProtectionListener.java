package com.shadowfang.talisman;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TalismanProtectionListener implements Listener {

    private final AbyssalTalismanPlugin plugin;
    private final Map<UUID, List<ItemStack>> pendingReturns = new HashMap<>();

    public TalismanProtectionListener(AbyssalTalismanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (TalismanItem.isTalisman(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cThe talisman refuses to leave your hand.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        List<ItemStack> toKeep = new ArrayList<>();
        for (Iterator<ItemStack> it = event.getDrops().iterator(); it.hasNext(); ) {
            ItemStack drop = it.next();
            if (TalismanItem.isTalisman(drop)) {
                toKeep.add(drop.clone());
                it.remove();
            }
        }
        if (!toKeep.isEmpty()) {
            pendingReturns.put(event.getPlayer().getUniqueId(), toKeep);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        List<ItemStack> items = pendingReturns.remove(event.getPlayer().getUniqueId());
        if (items != null && !items.isEmpty()) {
            for (ItemStack item : items) {
                event.getPlayer().getInventory().addItem(item);
            }
            event.getPlayer().sendMessage("§aYour talisman returns to you...");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        boolean talismanOnCursor = TalismanItem.isTalisman(cursor);
        boolean talismanClicked = TalismanItem.isTalisman(current);

        if (!talismanOnCursor && !talismanClicked) return;

        InventoryType type = event.getInventory().getType();

        if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
            return;
        }

        if (type == InventoryType.CRAFTING) {
            event.setCancelled(true);
            if (talismanClicked) player.sendMessage("§cThe Abyssal Talisman cannot be used in crafting.");
            return;
        }

        if (type == InventoryType.ANVIL || type == InventoryType.GRINDSTONE || type == InventoryType.SMITHING) {
            event.setCancelled(true);
            player.sendMessage("§cThe Abyssal Talisman resists modification.");
            return;
        }

        if (type == InventoryType.FURNACE || type == InventoryType.BLAST_FURNACE || type == InventoryType.SMOKER) {
            if (event.getRawSlot() == 1) {
                event.setCancelled(true);
                player.sendMessage("§cThe Abyssal Talisman cannot be used as fuel.");
            }
            return;
        }

        if (type == InventoryType.BREWING) {
            if (event.getRawSlot() == 4) {
                event.setCancelled(true);
                player.sendMessage("§cThe Abyssal Talisman cannot fuel a brewing stand.");
            }
            return;
        }

        if (type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST || type == InventoryType.BARREL) {
            event.setCancelled(true);
            player.sendMessage("§cTalismans cannot be stored in containers. Use §6/talisman vault§c.");
            return;
        }

        if (type == InventoryType.DISPENSER || type == InventoryType.DROPPER) {
            event.setCancelled(true);
            player.sendMessage("§cThe talisman refuses to enter that.");
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack dragged = event.getOldCursor();
        if (!TalismanItem.isTalisman(dragged)) return;

        InventoryType type = event.getInventory().getType();

        if (type == InventoryType.CRAFTING || type == InventoryType.ANVIL ||
            type == InventoryType.GRINDSTONE || type == InventoryType.SMITHING ||
            type == InventoryType.FURNACE || type == InventoryType.BLAST_FURNACE ||
            type == InventoryType.SMOKER || type == InventoryType.BREWING ||
            type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST ||
            type == InventoryType.BARREL || type == InventoryType.DISPENSER ||
            type == InventoryType.DROPPER) {
            event.setCancelled(true);
            player.sendMessage("§cThe talisman resists.");
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (event.getInventory().getResult() == null) return;
        if (TalismanItem.isTalisman(event.getInventory().getResult())) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!TalismanItem.isTalisman(hand)) {
            hand = player.getInventory().getItemInOffHand();
        }
        if (TalismanItem.isTalisman(hand)) {
            if (event.getRightClicked().getType().toString().contains("VILLAGER") ||
                event.getRightClicked().getType().toString().contains("TRADER")) {
                event.setCancelled(true);
                player.sendMessage("§cThe talisman cannot be used in trade.");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (!TalismanItem.isTalisman(offHand)) return;

        if (event.getClickedBlock() != null) {
            InventoryType blockType = event.getClickedBlock().getType().toString().contains("CHEST") ||
                                       event.getClickedBlock().getType().toString().contains("BARREL") ? InventoryType.CHEST :
                                     event.getClickedBlock().getType() == org.bukkit.Material.BREWING_STAND ? InventoryType.BREWING :
                                     event.getClickedBlock().getType() == org.bukkit.Material.FURNACE ? InventoryType.FURNACE :
                                     null;
            if (blockType != null) {
                event.setCancelled(true);
                player.sendMessage("§cTalismans cannot be used with containers. Use §6/talisman vault§c.");
            }
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem().getItemStack();
        if (TalismanItem.isTalisman(item)) {
            event.setCancelled(true);
        }
    }
}
