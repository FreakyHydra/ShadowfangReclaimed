package com.shadowfang.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldChangeListener implements Listener {

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        // Per-world inventory has been removed in favor of server-wide inventory.
        // This listener remains as a hook for future cross-world logic.
    }
}
