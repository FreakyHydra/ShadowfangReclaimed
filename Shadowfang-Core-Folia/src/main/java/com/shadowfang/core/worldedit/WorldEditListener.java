package com.shadowfang.core.worldedit;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class WorldEditListener implements Listener {

    private final WorldEditManager manager;
    private final WePasteTask pasteTask;

    public WorldEditListener(WorldEditManager manager) {
        this.manager = manager;
        this.pasteTask = new WePasteTask(manager,
                com.shadowfang.core.ShadowfangCorePlugin.getInstance());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!WeTool.isWand(event.getItem(), manager.plugin())) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Location loc = (event.getClickedBlock() != null)
                ? event.getClickedBlock().getLocation()
                : player.getLocation();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            manager.setPos1(player, loc);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            manager.setPos2(player, loc);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!WeTool.isWand(event.getPlayer().getInventory().getItemInMainHand(), manager.plugin())) return;
        pasteTask.tryPaste(event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (WeTool.isWand(event.getPlayer().getInventory().getItemInMainHand(), manager.plugin())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.clearPlayer(event.getPlayer());
    }
}
