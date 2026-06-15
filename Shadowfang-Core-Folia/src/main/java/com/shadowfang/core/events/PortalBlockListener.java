package com.shadowfang.core.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class PortalBlockListener implements Listener {

    @EventHandler
    public void onPortalUse(PlayerPortalEvent event) {
        String dim = event.getFrom().getWorld().getName();
        if (dim.contains("bitterroot")) {
            event.getPlayer().sendMessage("§cPortals are useless here.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String dim = player.getWorld().getName();
        
        if (dim.contains("bitterroot") && event.getItem() != null && event.getClickedBlock() != null) {
            
            // Prevent igniting portals with flint and steel
            if (event.getItem().getType() == Material.FLINT_AND_STEEL) {
                if (event.getClickedBlock().getType() == Material.OBSIDIAN) {
                    player.sendMessage("§cPortals cannot be ignited in Bitterroot.");
                    event.setCancelled(true);
                }
            }
            
            // Prevent placing eyes of ender in end portal frames
            if (event.getItem().getType() == Material.ENDER_EYE) {
                if (event.getClickedBlock().getType() == Material.END_PORTAL_FRAME) {
                    player.sendMessage("§cThe End is sealed away from Bitterroot.");
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        String dim = event.getPlayer().getWorld().getName();
        if (dim.contains("bitterroot")) {
            if (event.getBlockPlaced().getType() == Material.NETHER_PORTAL || event.getBlockPlaced().getType() == Material.END_PORTAL) {
                event.getPlayer().sendMessage("§cPortals are useless here.");
                event.setCancelled(true);
            }
        }
    }
}
