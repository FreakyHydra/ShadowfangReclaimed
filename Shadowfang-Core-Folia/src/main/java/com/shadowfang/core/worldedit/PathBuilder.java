package com.shadowfang.core.worldedit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PathBuilder {

    private final Plugin plugin;

    public PathBuilder(Plugin plugin) {
        this.plugin = plugin;
    }

    public void readRegion(World world, int x1, int y1, int z1, int x2, int y2, int z2,
                           ReadCallback callback) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);

        Map<Long, List<int[]>> byChunk = new LinkedHashMap<>();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int cx = x >> 4, cz = z >> 4;
                    long key = chunkKey(cx, cz);
                    byChunk.computeIfAbsent(key, k -> new ArrayList<>())
                            .add(new int[]{x, y, z, x - x1, y - y1, z - z1});
                }
            }
        }

        if (byChunk.isEmpty()) {
            callback.done(new ConcurrentHashMap<>());
            return;
        }

        Map<Vec3i, Material> result = new ConcurrentHashMap<>();
        AtomicInteger remaining = new AtomicInteger(byChunk.size());

        for (var e : byChunk.entrySet()) {
            long key = e.getKey();
            int cx = (int) (key >> 32);
            int cz = (int) key;
            List<int[]> blocks = e.getValue();

            Bukkit.getRegionScheduler().execute(plugin,
                    new Location(world, cx << 4, minY, cz << 4), () -> {
                        if (!world.isChunkLoaded(cx, cz)) {
                            if (remaining.decrementAndGet() == 0) {
                                callback.done(result);
                            }
                            return;
                        }
                        for (int[] b : blocks) {
                            Material m = world.getBlockAt(b[0], b[1], b[2]).getType();
                            result.put(new Vec3i(b[3], b[4], b[5]), m);
                        }
                        if (remaining.decrementAndGet() == 0) {
                            callback.done(result);
                        }
                    });
        }
    }

    public void writeAndSnapshot(World world, Map<Vec3i, Material> solidOffsets,
                                  List<Vec3i> airOffsets, Location anchor,
                                  WriteCallback callback) {
        Map<Long, List<WriteEntry>> byChunk = new LinkedHashMap<>();

        for (var e : solidOffsets.entrySet()) {
            Vec3i off = e.getKey();
            Material mat = e.getValue();
            int wx = anchor.getBlockX() + off.x();
            int wy = anchor.getBlockY() + off.y();
            int wz = anchor.getBlockZ() + off.z();
            if (wy < world.getMinHeight() || wy >= world.getMaxHeight()) continue;
            int cx = wx >> 4, cz = wz >> 4;
            long key = chunkKey(cx, cz);
            byChunk.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(new WriteEntry(wx, wy, wz, mat));
        }
        for (Vec3i off : airOffsets) {
            int wx = anchor.getBlockX() + off.x();
            int wy = anchor.getBlockY() + off.y();
            int wz = anchor.getBlockZ() + off.z();
            if (wy < world.getMinHeight() || wy >= world.getMaxHeight()) continue;
            int cx = wx >> 4, cz = wz >> 4;
            long key = chunkKey(cx, cz);
            byChunk.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(new WriteEntry(wx, wy, wz, Material.AIR));
        }

        if (byChunk.isEmpty()) {
            callback.done(List.of(), List.of());
            return;
        }

        List<UndoTracker.BlockSnapshot> allSnapshots = Collections.synchronizedList(new ArrayList<>());
        List<String> allPlaced = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger remaining = new AtomicInteger(byChunk.size());

        for (var e : byChunk.entrySet()) {
            long key = e.getKey();
            int cx = (int) (key >> 32);
            int cz = (int) key;
            List<WriteEntry> entries = e.getValue();

            Bukkit.getRegionScheduler().execute(plugin,
                    new Location(world, cx << 4, 64, cz << 4), () -> {
                        if (!world.isChunkLoaded(cx, cz)) {
                            if (remaining.decrementAndGet() == 0) {
                                callback.done(allSnapshots, allPlaced);
                            }
                            return;
                        }
                        for (WriteEntry we : entries) {
                            Material old = world.getBlockAt(we.x, we.y, we.z).getType();
                            allSnapshots.add(new UndoTracker.BlockSnapshot(world, we.x, we.y, we.z, old));
                            world.getBlockAt(we.x, we.y, we.z).setType(we.material, false);
                            allPlaced.add(we.x + "," + we.y + "," + we.z);
                        }
                        if (remaining.decrementAndGet() == 0) {
                            callback.done(allSnapshots, allPlaced);
                        }
                    });
        }
    }

    public void restoreBlocks(List<UndoTracker.BlockSnapshot> snapshots, Runnable onComplete) {
        Map<Long, List<UndoTracker.BlockSnapshot>> byChunk = new LinkedHashMap<>();
        for (UndoTracker.BlockSnapshot s : snapshots) {
            int cx = s.x() >> 4, cz = s.z() >> 4;
            long key = chunkKey(cx, cz);
            byChunk.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }

        if (byChunk.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        AtomicInteger remaining = new AtomicInteger(byChunk.size());

        for (var e : byChunk.entrySet()) {
            long key = e.getKey();
            int cx = (int) (key >> 32);
            int cz = (int) key;
            List<UndoTracker.BlockSnapshot> chunkSnaps = e.getValue();

            Bukkit.getRegionScheduler().execute(plugin,
                    new Location(chunkSnaps.get(0).world(), cx << 4, 64, cz << 4), () -> {
                        for (UndoTracker.BlockSnapshot s : chunkSnaps) {
                            if (s.y() < s.world().getMinHeight() || s.y() >= s.world().getMaxHeight()) continue;
                            s.world().getBlockAt(s.x(), s.y(), s.z()).setType(s.material(), false);
                        }
                        if (remaining.decrementAndGet() == 0 && onComplete != null) {
                            onComplete.run();
                        }
                    });
        }
    }

    private record WriteEntry(int x, int y, int z, Material material) {}

    private static long chunkKey(int cx, int cz) {
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    }

    @FunctionalInterface
    public interface ReadCallback {
        void done(Map<Vec3i, Material> blocks);
    }

    @FunctionalInterface
    public interface WriteCallback {
        void done(List<UndoTracker.BlockSnapshot> snapshots, List<String> placed);
    }
}
