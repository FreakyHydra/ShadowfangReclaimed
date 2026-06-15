package com.shadowfang.core.verse;

import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.Location;
import org.bukkit.World;

public class HubManager {

    private final ShadowfangCorePlugin plugin;

    public HubManager(ShadowfangCorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialises the hub world's spawn point.
     * Must be called inside a GlobalRegionScheduler task — never directly on onEnable().
     */
    public void initHub() {
        // On Folia, world operations must run on the region that owns the location.
        // We schedule on the GlobalRegionScheduler to be safe for spawn-setting.
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            World hub = resolveHubWorld();
            if (hub == null) {
                return;
            }

            hub.setSpawnLocation(new Location(hub, 0.5, -59, 0.5));
            plugin.getLogger().info("[HubManager] Hub world '" + hub.getName() +
                "' initialized — spawn set at (0.5, -59, 0.5)");
        });
    }

    /**
     * Returns the hub world's spawn location, or null if the hub is not loaded.
     */
    public Location getHubSpawn() {
        World hub = resolveHubWorld();
        return hub != null ? hub.getSpawnLocation() : null;
    }

    public Location getSpawnLocation(String worldName) {
        World world = plugin.getServer().getWorld(worldName);
        if (world != null) {
            return world.getSpawnLocation();
        }
        return null;
    }

    /**
     * Resolves the hub world by checking common names.
     * Uses plain folder names — Server.getWorld() matches by folder name only.
     */
    private World resolveHubWorld() {
        World hub = plugin.getServer().getWorld("world_hub");
        if (hub == null) hub = plugin.getServer().getWorld("hub");
        return hub;
    }
}
