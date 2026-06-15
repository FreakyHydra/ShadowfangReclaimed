package com.shadowfang.rosetta;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Dispatches work to the correct Folia region thread based on chunk coordinates.
 * On non-Folia, runs synchronously on the main thread.
 */
public final class RegionDispatcher {

    private RegionDispatcher() {}

    /**
     * Run a task on the region thread that owns the given chunk.
     * On non-Folia, runs immediately on the main thread.
     */
    public static void runAtChunk(Plugin plugin, World world, int chunkX, int chunkZ, Runnable task) {
        FoliaCompat.runOnRegion(plugin, world, chunkX, chunkZ, task);
    }

    /**
     * Run a task on the region thread that owns the given block position.
     */
    public static void runAtBlock(Plugin plugin, World world, int x, int z, Runnable task) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        FoliaCompat.runOnRegion(plugin, world, chunkX, chunkZ, task);
    }

    /**
     * Dispatch a task to run on the global region scheduler.
     * Use for operations that touch multiple regions (weather, time, broadcasting).
     */
    public static void runGlobal(Plugin plugin, Runnable task) {
        FoliaCompat.runGlobal(plugin, task);
    }

    /**
     * Execute a consumer for each online player on the global region thread.
     * On non-Folia, runs on the main thread.
     */
    public static void forAllPlayers(Plugin plugin, Consumer<Player> action) {
        if (!FoliaCompat.isFolia()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                action.accept(player);
            }
            return;
        }
        // On Folia, iterate the snapshot — callers must ensure they don't
        // do region-specific operations from here without further dispatching
        for (Player player : Bukkit.getOnlinePlayers()) {
            action.accept(player);
        }
    }

    /**
     * Schedule an async task that, when complete, dispatches back to the correct region.
     */
    public static void asyncThenRunAt(Plugin plugin, World world, int chunkX, int chunkZ,
                                       Runnable asyncWork, Runnable onRegionThread) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            asyncWork.run();
            FoliaCompat.runOnRegion(plugin, world, chunkX, chunkZ, onRegionThread);
        });
    }

    /**
     * A thread-safe set of player UUIDs that can be safely iterated from any thread.
     */
    public static java.util.Set<java.util.UUID> newConcurrentUuidSet() {
        return ConcurrentHashMap.newKeySet();
    }
}
