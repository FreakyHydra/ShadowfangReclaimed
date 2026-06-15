package com.shadowfang.core.events;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.shadowfang.core.faction.Faction;
import com.shadowfang.core.faction.FactionManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class FactionBellListener implements Listener {

    @EventHandler
    public void onBellPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.BELL) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        NamespacedKey tagKey = new NamespacedKey(ShadowfangCorePlugin.getInstance(), "isFactionBell");
        boolean isFactionBell = item.hasItemMeta()
            && item.getItemMeta().getPersistentDataContainer().has(tagKey, PersistentDataType.BYTE);

        FactionManager manager = ShadowfangCorePlugin.getInstance().getFactionManager();
        Faction playerFaction = manager.getPlayerFaction(player.getUniqueId());

        if (playerFaction == null || !isFactionBell) {
            return;
        }

        if (playerFaction.getBellLocation() != null) {
            player.sendMessage(ChatColor.YELLOW + "Your pack already claims its territory. Disband to start anew.");
            return;
        }

        String dim = player.getWorld().getName();
        int chunkX = block.getChunk().getX();
        int chunkZ = block.getChunk().getZ();

        if (manager.isWithinSpawnRadius(block.getLocation(), 1000)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Cannot claim here! Too close to world spawn (1,000 block minimum).");
            return;
        }

        if (!manager.canClaimArea(dim, chunkX, chunkZ)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Cannot claim here! Too close to another faction's territory.");
            return;
        }

        manager.claimBellArea(playerFaction, dim, chunkX, chunkZ);
        playerFaction.setBellLocation(block.getLocation());
        if (playerFaction.getSpawnLocation() == null) {
            playerFaction.setSpawnLocation(block.getLocation().add(0.5, 1, 0.5));
        }
        manager.save();

        player.sendMessage(ChatColor.GREEN + "Your pack's territory has been claimed!");
        player.sendMessage(ChatColor.GREEN + "A 3x3 chunk territory is now yours.");
        player.sendMessage(ChatColor.YELLOW + "Use /f setspawn to set your pack's spawn point.");
    }
}
