package com.shadowfang.rosetta;

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Folia detection and compatibility utilities.
 * Uses direct Folia API calls (no reflection) since Rosetta Stone depends on folia-api.
 */
public final class FoliaCompat {

    private static final boolean FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;
    }

    private FoliaCompat() {}

    public static boolean isFolia() {
        return FOLIA;
    }

    public static boolean isGlobalTickThread() {
        if (!FOLIA) {
            return Thread.currentThread().getName().equals("Server thread");
        }
        // On Folia, the global tick thread runs tasks dispatched via GlobalRegionScheduler.
        // Check the thread name — Folia names region threads "Folia Region Scheduler Thread #N"
        // and the global tick thread does not match that pattern.
        String name = Thread.currentThread().getName();
        return !name.contains("Region Scheduler");
    }

    private static GlobalRegionScheduler getGlobalScheduler() {
        return Bukkit.getServer().getGlobalRegionScheduler();
    }

    private static RegionScheduler getRegionScheduler() {
        return Bukkit.getServer().getRegionScheduler();
    }

    public static void scheduleSyncRepeating(org.bukkit.plugin.Plugin plugin, Runnable task, long delay, long period) {
        if (!FOLIA) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, delay, period);
            return;
        }
        getGlobalScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period);
    }

    public static void runGlobal(org.bukkit.plugin.Plugin plugin, Runnable task) {
        if (!FOLIA) {
            task.run();
            return;
        }
        getGlobalScheduler().run(plugin, scheduledTask -> task.run());
    }

    public static void runOnRegion(org.bukkit.plugin.Plugin plugin, World world, int chunkX, int chunkZ, Runnable task) {
        if (!FOLIA) {
            task.run();
            return;
        }
        getRegionScheduler().run(plugin, world, chunkX, chunkZ, scheduledTask -> task.run());
    }

    public static void runAsync(org.bukkit.plugin.Plugin plugin, Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }
}
