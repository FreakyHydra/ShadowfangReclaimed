package com.shadowfang.core.elevator;

import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.UUID;

public class ElevatorListener implements Listener {

    private final ElevatorManager manager;
    private final ShadowfangCorePlugin plugin;

    public ElevatorListener(ElevatorManager manager, ShadowfangCorePlugin plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!ElevatorWand.isWand(event.getItem())) return;

        event.setCancelled(true);

        if (event.getClickedBlock() == null) return;

        String groupName = ElevatorWand.getGroupName(event.getItem());
        if (groupName == null) {
            event.getPlayer().sendMessage("§cWand is not properly configured. Use /sr elevator assign again.");
            return;
        }

        var block = event.getClickedBlock();
        String world = block.getWorld().getName();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (manager.addFloor(groupName, world, x, y, z)) {
                event.getPlayer().sendMessage("§aFloor added to §e" + groupName + " §aat §f" + x + "," + y + "," + z + "§a.");
            } else {
                event.getPlayer().sendMessage("§cFailed to add floor.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (ElevatorWand.isWand(event.getPlayer().getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            return;
        }

        var block = event.getBlock();
        String world = block.getWorld().getName();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        if (manager.removeFloorAt(world, x, y, z)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§aFloor removed from elevator.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        manager.activateTeleporter(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (manager.handleChatSelection(event.getPlayer(), event.getMessage())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.clearPendingDestination(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        manager.clearPendingDestination(event.getPlayer().getUniqueId());
    }
}
