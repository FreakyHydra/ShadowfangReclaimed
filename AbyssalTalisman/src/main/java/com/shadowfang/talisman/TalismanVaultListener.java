package com.shadowfang.talisman;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TalismanVaultListener implements Listener {

    private static final String VAULT_TITLE = "§5§lTalisman Vault";
    private static final int VAULT_SIZE = 27;

    private final AbyssalTalismanPlugin plugin;
    private final Map<UUID, List<ItemStack>> openVaults = new HashMap<>();

    public TalismanVaultListener(AbyssalTalismanPlugin plugin) {
        this.plugin = plugin;
    }

    public void openVault(Player player) {
        List<ItemStack> items = plugin.getVaultManager().getVaultContents(player);
        Inventory vault = Bukkit.createInventory(null, VAULT_SIZE, VAULT_TITLE);

        for (int i = 0; i < VAULT_SIZE; i++) {
            if (i < items.size()) {
                vault.setItem(i, items.get(i));
            } else {
                vault.setItem(i, createFiller());
            }
        }

        openVaults.put(player.getUniqueId(), new ArrayList<>(items));
        player.openInventory(vault);
    }

    private ItemStack createFiller() {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName("§8§l×");
        filler.setItemMeta(meta);
        return filler;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVaultClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(VAULT_TITLE)) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        List<ItemStack> currentItems = openVaults.getOrDefault(player.getUniqueId(), new ArrayList<>());

        if (slot < 0 || slot >= VAULT_SIZE) return;

        if (clicked != null && clicked.getType() != Material.AIR && clicked.getType() != Material.BLACK_STAINED_GLASS_PANE) {
            if (TalismanItem.isTalisman(clicked)) {
                if (TalismanItem.isTalisman(offHand) && offHand.getType() != Material.AIR) {
                    player.sendMessage("§cClear your off-hand first.");
                    return;
                }

                List<ItemStack> vaultItems = plugin.getVaultManager().getVaultContents(player);
                if (slot < vaultItems.size()) {
                    ItemStack talisman = vaultItems.get(slot);
                    plugin.getVaultManager().removeFromVault(player, slot, talisman);
                    player.getInventory().setItemInOffHand(talisman);
                    TalismanItem.refreshLore(talisman);
                    player.sendMessage("§aTalisman equipped.");
                    refreshVault(player);
                    return;
                }
            }
            return;
        }

        if (TalismanItem.isTalisman(cursor)) {
            if (currentItems.size() < VAULT_SIZE) {
                plugin.getVaultManager().addToVault(player, cursor.clone());
                event.setCursor(new ItemStack(Material.AIR));
                player.sendMessage("§aTalisman stored in vault.");
                refreshVault(player);
            } else {
                player.sendMessage("§cVault is full.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVaultDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(VAULT_TITLE)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onVaultClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(VAULT_TITLE)) return;
        openVaults.remove(player.getUniqueId());
    }

    private void refreshVault(Player player) {
        List<ItemStack> items = plugin.getVaultManager().getVaultContents(player);
        Inventory vault = player.getOpenInventory().getTopInventory();
        if (vault.getHolder() == null) return;

        for (int i = 0; i < VAULT_SIZE; i++) {
            if (i < items.size()) {
                vault.setItem(i, items.get(i));
            } else {
                vault.setItem(i, createFiller());
            }
        }
        openVaults.put(player.getUniqueId(), new ArrayList<>(items));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().toString().contains("LEFT")) return;
        Player player = event.getPlayer();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (!TalismanItem.isTalisman(offHand)) return;
        if (!player.isSneaking()) return;

        event.setCancelled(true);

        TalismanMode current = TalismanItem.getMode(offHand);
        TalismanMode next = current.next();
        TalismanItem.setMode(offHand, next);
        TalismanItem.refreshLore(offHand);

        String[] powerNames = TalismanItem.getPowerNames(TalismanItem.getType(offHand));
        String modeName = powerNames[next.index];

        player.sendMessage("§6§l" + modeName + " §emode activated.");
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.2f);
    }
}
