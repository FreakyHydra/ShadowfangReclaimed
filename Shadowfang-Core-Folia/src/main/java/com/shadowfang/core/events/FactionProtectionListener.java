package com.shadowfang.core.events;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.shadowfang.core.faction.Faction;
import com.shadowfang.core.faction.FactionManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class FactionProtectionListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().hasPermission("shadowfang.admin")) return;

        Player player = event.getPlayer();
        FactionManager manager = ShadowfangCorePlugin.getInstance().getFactionManager();
        
        String dim = player.getWorld().getName();
        int chunkX = event.getBlock().getChunk().getX();
        int chunkZ = event.getBlock().getChunk().getZ();
        Block block = event.getBlock();

        Faction chunkFaction = manager.getFactionAt(dim, chunkX, chunkZ);
        if (chunkFaction != null) {
            Faction playerFaction = manager.getPlayerFaction(player.getUniqueId());
            if (playerFaction == null || !playerFaction.getId().equals(chunkFaction.getId())) {
                player.sendMessage("§cThis Hunting Ground belongs to " + chunkFaction.getName());
                event.setCancelled(true);
                return;
            }

            if (block.getType() == Material.BELL) {
                event.setDropItems(false);
                NamespacedKey tagKey = new NamespacedKey(ShadowfangCorePlugin.getInstance(), "isFactionBell");
                ItemStack bellDrop = new ItemStack(Material.BELL);
                ItemMeta meta = bellDrop.getItemMeta();
                meta.getPersistentDataContainer().set(tagKey, PersistentDataType.BYTE, (byte) 1);
                meta.setCustomModelData(1001);
                bellDrop.setItemMeta(meta);
                block.getWorld().dropItemNaturally(block.getLocation(), bellDrop);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().hasPermission("shadowfang.admin")) return;

        Player player = event.getPlayer();
        FactionManager manager = ShadowfangCorePlugin.getInstance().getFactionManager();
        
        String dim = player.getWorld().getName();
        int chunkX = event.getBlock().getChunk().getX();
        int chunkZ = event.getBlock().getChunk().getZ();

        Faction chunkFaction = manager.getFactionAt(dim, chunkX, chunkZ);
        if (chunkFaction != null) {
            Faction playerFaction = manager.getPlayerFaction(player.getUniqueId());
            if (playerFaction == null || !playerFaction.getId().equals(chunkFaction.getId())) {
                player.sendMessage("§cThis Hunting Ground belongs to " + chunkFaction.getName());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer().hasPermission("shadowfang.admin")) return;
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        FactionManager manager = ShadowfangCorePlugin.getInstance().getFactionManager();
        
        String dim = player.getWorld().getName();
        int chunkX = block.getChunk().getX();
        int chunkZ = block.getChunk().getZ();

        Faction chunkFaction = manager.getFactionAt(dim, chunkX, chunkZ);
        if (chunkFaction != null) {
            Faction playerFaction = manager.getPlayerFaction(player.getUniqueId());
            if (playerFaction == null || !playerFaction.getId().equals(chunkFaction.getId())) {
                if (block.getType() == Material.BELL) {
                    player.sendMessage("§cThis Faction Bell belongs to " + chunkFaction.getName());
                } else {
                    player.sendMessage("§cYou cannot interact in " + chunkFaction.getName() + "'s territory!");
                }
                event.setCancelled(true);
            }
        }
    }
}
