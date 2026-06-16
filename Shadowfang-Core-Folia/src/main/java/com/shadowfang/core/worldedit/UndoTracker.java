package com.shadowfang.core.worldedit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;

public class UndoTracker {

    public record BlockSnapshot(World world, int x, int y, int z, Material material) {}

    private final Map<UUID, Deque<List<BlockSnapshot>>> history = new HashMap<>();
    private final int maxHistory;

    public UndoTracker(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public void push(UUID playerId, List<BlockSnapshot> snapshots) {
        if (snapshots.isEmpty()) return;
        Deque<List<BlockSnapshot>> stack = history.computeIfAbsent(playerId, k -> new ArrayDeque<>());
        stack.push(snapshots);
        while (stack.size() > maxHistory) {
            stack.removeLast();
        }
    }

    public List<BlockSnapshot> pop(UUID playerId) {
        Deque<List<BlockSnapshot>> stack = history.get(playerId);
        if (stack == null || stack.isEmpty()) return List.of();
        return stack.pop();
    }

    public boolean hasUndo(UUID playerId) {
        Deque<List<BlockSnapshot>> stack = history.get(playerId);
        return stack != null && !stack.isEmpty();
    }

    public void clear(UUID playerId) {
        history.remove(playerId);
    }

    public static List<BlockSnapshot> snapshotBlocks(World world, List<Vec3i> positions, Location anchor) {
        List<BlockSnapshot> snaps = new ArrayList<>();
        for (Vec3i pos : positions) {
            int wx = anchor.getBlockX() + pos.x();
            int wy = anchor.getBlockY() + pos.y();
            int wz = anchor.getBlockZ() + pos.z();
            if (wy < world.getMinHeight() || wy >= world.getMaxHeight()) continue;
            snaps.add(new BlockSnapshot(world, wx, wy, wz, world.getBlockAt(wx, wy, wz).getType()));
        }
        return snaps;
    }
}
