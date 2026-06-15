package com.shadowfang.core.events;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.shadowfang.core.verse.VerseManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PortalLinkListener implements Listener {

    private final ShadowfangCorePlugin plugin;

    public PortalLinkListener(ShadowfangCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) return;

        String fromWorld = event.getFrom().getWorld().getName();
        String baseName = getBaseWorldName(fromWorld);
        String targetWorldName = switch (event.getCause()) {
            case NETHER_PORTAL -> baseName + "_nether";
            case END_PORTAL -> baseName + "_the_end";
            default -> null;
        };

        if (targetWorldName == null) return;

        World targetWorld = plugin.getServer().getWorld(targetWorldName);
        if (targetWorld == null) {
            VerseManager.WorldType type = event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
                ? VerseManager.WorldType.NETHER : VerseManager.WorldType.END;
            event.setCancelled(true);
            event.getPlayer().sendMessage("§eThe " + targetWorldName + " dimension is not yet generated. Creating...");
            String finalTargetName = targetWorldName;
            VerseManager.getInstance().createWorld(targetWorldName, type).thenAccept(world -> {
                if (world != null) {
                    event.getPlayer().teleportAsync(world.getSpawnLocation());
                    event.getPlayer().sendMessage("§aPortal linked to " + world.getName() + "!");
                } else {
                    event.getPlayer().sendMessage("§cFailed to create linked dimension.");
                }
            });
            return;
        }

        Location targetLoc = targetWorld.getSpawnLocation();
        event.setTo(targetLoc);
        event.getPlayer().sendMessage("§7Crossing into " + targetWorldName + "...");
    }

    private String getBaseWorldName(String worldName) {
        if (worldName.endsWith("_nether")) {
            return worldName.substring(0, worldName.length() - "_nether".length());
        }
        if (worldName.endsWith("_the_end")) {
            return worldName.substring(0, worldName.length() - "_the_end".length());
        }
        return worldName;
    }
}