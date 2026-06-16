package com.shadowfang.core.worldedit;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WePattern {

    private final Map<Vec3i, Material> solidBlocks;
    private final List<Vec3i> airBlocks;
    private final int sizeX, sizeY, sizeZ;
    private final Vec3i originOffset;
    private final int copyRotations;

    public WePattern(Map<Vec3i, Material> solidBlocks, List<Vec3i> airBlocks,
                     int sizeX, int sizeY, int sizeZ, Vec3i originOffset, int copyRotations) {
        this.solidBlocks = solidBlocks;
        this.airBlocks = airBlocks;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.originOffset = originOffset;
        this.copyRotations = copyRotations;
    }

    public static WePattern fromCapture(Map<Vec3i, Material> allBlocks, int sizeX, int sizeY, int sizeZ,
                                        Location origin, Location anchor, int copyRotations) {
        Map<Vec3i, Material> solids = new LinkedHashMap<>();
        List<Vec3i> airs = new ArrayList<>();
        Vec3i originOff = new Vec3i(
                anchor.getBlockX() - origin.getBlockX(),
                anchor.getBlockY() - origin.getBlockY(),
                anchor.getBlockZ() - origin.getBlockZ()
        );
        for (var e : allBlocks.entrySet()) {
            if (e.getValue().isAir()) {
                airs.add(e.getKey());
            } else {
                solids.put(e.getKey(), e.getValue());
            }
        }
        return new WePattern(solids, airs, sizeX, sizeY, sizeZ, originOff, copyRotations);
    }

    public Map<Vec3i, Material> solidBlocks() { return solidBlocks; }
    public List<Vec3i> airBlocks() { return airBlocks; }
    public int sizeX() { return sizeX; }
    public int sizeY() { return sizeY; }
    public int sizeZ() { return sizeZ; }
    public Vec3i originOffset() { return originOffset; }
    public int copyRotations() { return copyRotations; }
    public int totalBlocks() { return solidBlocks.size() + airBlocks.size(); }
}
