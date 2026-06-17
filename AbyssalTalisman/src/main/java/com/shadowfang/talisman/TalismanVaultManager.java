package com.shadowfang.talisman;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TalismanVaultManager {

    private final AbyssalTalismanPlugin plugin;
    private final File vaultDir;

    public TalismanVaultManager(AbyssalTalismanPlugin plugin) {
        this.plugin = plugin;
        this.vaultDir = new File(plugin.getDataFolder(), "vaults");
        if (!vaultDir.exists()) {
            vaultDir.mkdirs();
        }
    }

    private File getVaultFile(UUID uuid) {
        return new File(vaultDir, uuid.toString() + ".yml");
    }

    public List<ItemStack> getVaultContents(Player player) {
        File file = getVaultFile(player.getUniqueId());
        if (!file.exists()) return new ArrayList<>();

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        List<ItemStack> items = new ArrayList<>();
        List<String> serialized = cfg.getStringList("talismans");
        for (String s : serialized) {
            ItemStack item = TalismanItem.deserialize(s);
            if (item != null) items.add(item);
        }
        return items;
    }

    public void saveVault(Player player, List<ItemStack> items) {
        File file = getVaultFile(player.getUniqueId());
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        List<String> serialized = new ArrayList<>();
        for (ItemStack item : items) {
            if (TalismanItem.isTalisman(item)) {
                serialized.add(TalismanItem.serialize(item));
            }
        }
        cfg.set("talismans", serialized);
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save vault for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void addToVault(Player player, ItemStack talisman) {
        List<ItemStack> items = getVaultContents(player);
        items.add(talisman);
        saveVault(player, items);
    }

    public boolean removeFromVault(Player player, int slot, ItemStack talisman) {
        List<ItemStack> items = getVaultContents(player);
        if (slot < 0 || slot >= items.size()) return false;
        ItemStack inSlot = items.get(slot);
        if (!TalismanItem.isTalisman(inSlot)) return false;
        if (!TalismanItem.serialize(inSlot).equals(TalismanItem.serialize(talisman))) return false;
        items.remove(slot);
        saveVault(player, items);
        return true;
    }

    public int getVaultSize(Player player) {
        return getVaultContents(player).size();
    }

    public boolean hasTalismanInVault(Player player, TalismanType type) {
        List<ItemStack> items = getVaultContents(player);
        for (ItemStack item : items) {
            if (TalismanItem.isTalisman(item) && TalismanItem.getType(item) == type) {
                return true;
            }
        }
        return false;
    }
}
