package com.shadowfang.rosetta;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Player utilities that are Folia-safe.
 */
public final class FoliaPlayer {

    private FoliaPlayer() {}

    /**
     * Teleport a player to a location, dispatching to the correct region thread on Folia.
     */
    public static void teleport(Player player, Location location, Plugin plugin) {
        if (!FoliaCompat.isFolia()) {
            player.teleport(location);
            return;
        }
        World world = location.getWorld();
        if (world == null) return;
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        FoliaCompat.runOnRegion(plugin, world, chunkX, chunkZ, () -> {
            if (player.isOnline()) {
                player.teleport(location);
            }
        });
    }

    /**
     * Schedule a task to run on the player's region thread.
     * On non-Folia, runs immediately on the main thread.
     */
    public static void runAtPlayer(Player player, Plugin plugin, Runnable task) {
        if (!FoliaCompat.isFolia()) {
            task.run();
            return;
        }
        Location loc = player.getLocation();
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;
        FoliaCompat.runOnRegion(plugin, loc.getWorld(), chunkX, chunkZ, task);
    }

    /**
     * Get the chunk coordinates for a player's current location.
     */
    public static int[] getPlayerChunk(Player player) {
        Location loc = player.getLocation();
        return new int[]{loc.getBlockX() >> 4, loc.getBlockZ() >> 4};
    }
}
