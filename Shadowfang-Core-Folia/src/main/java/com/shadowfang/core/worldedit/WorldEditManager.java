package com.shadowfang.core.worldedit;

import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class WorldEditManager {

    private final ShadowfangCorePlugin plugin;
    private final PathBuilder pathBuilder;
    private final UndoTracker undoTracker;

    private final Map<UUID, PosSelection> selections = new HashMap<>();
    private final Map<UUID, WePattern> clipboards = new HashMap<>();
    private final Set<UUID> walkPasteActive = new HashSet<>();
    private final Map<UUID, Location> lastPasteAnchor = new HashMap<>();

    public WorldEditManager(ShadowfangCorePlugin plugin) {
        this.plugin = plugin;
        this.pathBuilder = new PathBuilder(plugin);
        this.undoTracker = new UndoTracker(20);
    }

    public ShadowfangCorePlugin plugin() { return plugin; }

    public void setPos1(Player player, Location loc) {
        PosSelection sel = selections.computeIfAbsent(player.getUniqueId(), k -> new PosSelection());
        sel.pos1 = loc.clone();
        player.sendMessage("§aPosition 1 set to §e(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
    }

    public void setPos2(Player player, Location loc) {
        PosSelection sel = selections.computeIfAbsent(player.getUniqueId(), k -> new PosSelection());
        sel.pos2 = loc.clone();
        player.sendMessage("§aPosition 2 set to §e(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
    }

    public boolean hasSelection(Player player) {
        PosSelection sel = selections.get(player.getUniqueId());
        return sel != null && sel.isComplete();
    }

    public void copy(Player player) {
        UUID id = player.getUniqueId();
        PosSelection sel = selections.get(id);
        if (sel == null || !sel.isComplete()) {
            player.sendMessage("§cYou must set both positions first.");
            return;
        }

        Location pos1 = sel.pos1;
        Location pos2 = sel.pos2;

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int sx = maxX - minX + 1;
        int sy = maxY - minY + 1;
        int sz = maxZ - minZ + 1;

        Location origin = new Location(pos1.getWorld(), minX, minY, minZ);

        int copyRotations = PatternRotator.rotationsForFacing(
                PatternRotator.Facing.fromYaw(player.getLocation().getYaw()));

        player.sendMessage("§7Reading " + (sx * sy * sz) + " blocks...");

        pathBuilder.readRegion(pos1.getWorld(), minX, minY, minZ, maxX, maxY, maxZ, blocks -> {
            WePattern pattern = WePattern.fromCapture(blocks, sx, sy, sz, origin,
                    player.getLocation(), copyRotations);
            clipboards.put(id, pattern);
            player.sendMessage("§aPattern captured. §7" + pattern.solidBlocks().size()
                    + " solid, " + pattern.airBlocks().size() + " air. §7Size: "
                    + sx + "x" + sy + "x" + sz);
        });
    }

    public void paste(Player player, Runnable onComplete) {
        WePattern pattern = clipboards.get(player.getUniqueId());
        if (pattern == null) {
            player.sendMessage("§cNo pattern captured. Use copy first.");
            if (onComplete != null) onComplete.run();
            return;
        }

        Location playerLoc = player.getLocation();
        PatternRotator.Facing facing = PatternRotator.Facing.fromYaw(playerLoc.getYaw());

        doPaste(player, pattern, facing, playerLoc, null, onComplete, true);
    }

    public void walkPasteTick(Player player, Runnable onComplete) {
        WePattern pattern = clipboards.get(player.getUniqueId());
        if (pattern == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        Location playerLoc = player.getLocation();
        PatternRotator.Facing facing = PatternRotator.Facing.fromYaw(playerLoc.getYaw());

        Location previousAnchor = lastPasteAnchor.get(player.getUniqueId());
        doPaste(player, pattern, facing, playerLoc, previousAnchor, onComplete, true);
    }

    private void doPaste(Player player, WePattern pattern, PatternRotator.Facing facing,
                         Location playerLoc, Location previousAnchor, Runnable onComplete,
                         boolean updateAnchor) {
        UUID id = player.getUniqueId();
        int pasteRotations = PatternRotator.rotationsForFacing(facing);
        int delta = (pattern.copyRotations() - pasteRotations + 4) % 4;

        Map<Vec3i, Material> rotatedSolids = PatternRotator.rotateSolidBlocksBy(pattern.solidBlocks(), delta);
        List<Vec3i> rotatedAirs = PatternRotator.rotateAirBlocksBy(pattern.airBlocks(), delta);

        int segLen = PatternRotator.segmentLength(rotatedSolids, rotatedAirs, facing);

        Location pasteAnchor;
        if (previousAnchor != null) {
            Vec3i step = PatternRotator.stepOffset(facing, segLen);
            pasteAnchor = previousAnchor.clone().add(step.x(), step.y(), step.z());
        } else {
            Vec3i fwd = PatternRotator.forwardOffset(facing);
            pasteAnchor = playerLoc.clone().add(fwd.x(), fwd.y(), fwd.z());
        }

        pathBuilder.writeAndSnapshot(playerLoc.getWorld(), rotatedSolids, rotatedAirs, pasteAnchor,
                (snapshots, placed) -> {
                    undoTracker.push(id, snapshots.stream()
                            .filter(s -> !snapshotContains(placed, s))
                            .toList());
                    if (updateAnchor) {
                        lastPasteAnchor.put(id, pasteAnchor.clone());
                    }
                    player.sendMessage("§aPasted. §7" + placed.size() + " blocks modified.");
                    if (onComplete != null) onComplete.run();
                });
    }

    public void toggleWalkPaste(Player player) {
        UUID id = player.getUniqueId();
        if (walkPasteActive.contains(id)) {
            walkPasteActive.remove(id);
            lastPasteAnchor.remove(id);
            player.sendMessage("§cWalk-paste mode §4stopped§c.");
        } else {
            if (!clipboards.containsKey(id)) {
                player.sendMessage("§cNo pattern. Use copy first.");
                return;
            }
            walkPasteActive.add(id);
            lastPasteAnchor.remove(id);
            player.sendMessage("§aWalk-paste mode §2started§a. Hold your wand and walk.");
        }
    }

    public boolean isWalkPasteActive(Player player) {
        return walkPasteActive.contains(player.getUniqueId());
    }

    public UndoTracker getUndoTracker() {
        return undoTracker;
    }

    public PathBuilder getPathBuilder() {
        return pathBuilder;
    }

    public void clearPlayer(Player player) {
        UUID id = player.getUniqueId();
        selections.remove(id);
        clipboards.remove(id);
        walkPasteActive.remove(id);
        lastPasteAnchor.remove(id);
        undoTracker.clear(id);
    }

    public WePattern getClipboard(Player player) {
        return clipboards.get(player.getUniqueId());
    }

    private boolean snapshotContains(List<String> placed, UndoTracker.BlockSnapshot snap) {
        return placed.contains(snap.x() + "," + snap.y() + "," + snap.z());
    }

    private static class PosSelection {
        Location pos1;
        Location pos2;

        boolean isComplete() {
            return pos1 != null && pos2 != null && pos1.getWorld().equals(pos2.getWorld());
        }
    }
}
