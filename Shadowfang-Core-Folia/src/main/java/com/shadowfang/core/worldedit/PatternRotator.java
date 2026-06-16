package com.shadowfang.core.worldedit;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PatternRotator {

    public enum Facing {
        SOUTH, WEST, NORTH, EAST;

        public static Facing fromYaw(float yaw) {
            float y = ((yaw % 360) + 360) % 360;
            if (y >= 315 || y < 45) return SOUTH;
            if (y < 135) return WEST;
            if (y < 225) return NORTH;
            return EAST;
        }

        public static Facing fromLocation(Location from, Location to) {
            double dx = to.getX() - from.getX();
            double dz = to.getZ() - from.getZ();
            if (dx == 0 && dz == 0) return SOUTH;
            double angle = Math.toDegrees(Math.atan2(dx, dz));
            return fromYaw((float) angle);
        }
    }

    public static int rotationsForFacing(Facing facing) {
        return switch (facing) {
            case SOUTH -> 0;
            case WEST -> 1;
            case NORTH -> 2;
            case EAST -> 3;
        };
    }

    private static Vec3i rotateOffset(Vec3i offset, int rotations) {
        int rx = offset.x();
        int rz = offset.z();
        for (int i = 0; i < rotations; i++) {
            int oldRx = rx;
            rx = rz;
            rz = -oldRx;
        }
        return new Vec3i(rx, offset.y(), rz);
    }

    public static Map<Vec3i, Material> rotateSolidBlocks(
            Map<Vec3i, Material> blocks, Facing facing) {
        return rotateSolidBlocksBy(blocks, rotationsForFacing(facing));
    }

    public static Map<Vec3i, Material> rotateSolidBlocksBy(
            Map<Vec3i, Material> blocks, int rotations) {
        if (rotations == 0) return new LinkedHashMap<>(blocks);
        Map<Vec3i, Material> result = new LinkedHashMap<>();
        for (var e : blocks.entrySet()) {
            result.put(rotateOffset(e.getKey(), rotations), e.getValue());
        }
        return result;
    }

    public static List<Vec3i> rotateAirBlocks(List<Vec3i> offsets, Facing facing) {
        return rotateAirBlocksBy(offsets, rotationsForFacing(facing));
    }

    public static List<Vec3i> rotateAirBlocksBy(List<Vec3i> offsets, int rotations) {
        if (rotations == 0) return new ArrayList<>(offsets);
        List<Vec3i> result = new ArrayList<>(offsets.size());
        for (Vec3i off : offsets) {
            result.add(rotateOffset(off, rotations));
        }
        return result;
    }

    public static int segmentLength(Map<Vec3i, Material> solids, List<Vec3i> airs, Facing facing) {
        int maxFwd = 0;
        for (Vec3i off : solids.keySet()) {
            int fwd = forwardProjection(off, facing);
            if (fwd > maxFwd) maxFwd = fwd;
        }
        for (Vec3i off : airs) {
            int fwd = forwardProjection(off, facing);
            if (fwd > maxFwd) maxFwd = fwd;
        }
        return maxFwd + 1;
    }

    private static int forwardProjection(Vec3i off, Facing facing) {
        return switch (facing) {
            case SOUTH -> off.z();
            case WEST -> -off.x();
            case NORTH -> -off.z();
            case EAST -> off.x();
        };
    }

    public static Vec3i forwardOffset(Facing facing) {
        return switch (facing) {
            case SOUTH -> new Vec3i(0, 0, 2);
            case WEST -> new Vec3i(-2, 0, 0);
            case NORTH -> new Vec3i(0, 0, -2);
            case EAST -> new Vec3i(2, 0, 0);
        };
    }

    public static Vec3i stepOffset(Facing facing, int length) {
        return switch (facing) {
            case SOUTH -> new Vec3i(0, 0, length);
            case WEST -> new Vec3i(-length, 0, 0);
            case NORTH -> new Vec3i(0, 0, -length);
            case EAST -> new Vec3i(length, 0, 0);
        };
    }

    public static int rotatedSizeX(int sx, int sz, Facing facing) {
        return switch (facing) {
            case SOUTH, NORTH -> sx;
            case WEST, EAST -> sz;
        };
    }

    public static int rotatedSizeZ(int sx, int sz, Facing facing) {
        return switch (facing) {
            case SOUTH, NORTH -> sz;
            case WEST, EAST -> sx;
        };
    }
}
