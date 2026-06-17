package com.shadowfang.core.elevator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class ElevatorGroup {

    private String name;
    private java.util.List<ElevatorFloor> floors;

    public ElevatorGroup(String name) {
        this.name = name;
        this.floors = new java.util.ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public java.util.List<ElevatorFloor> getFloors() {
        return floors;
    }

    public void addFloor(ElevatorFloor floor) {
        floors.add(floor);
    }

    public boolean removeFloor(int index) {
        if (index >= 0 && index < floors.size()) {
            floors.remove(index);
            return true;
        }
        return false;
    }

    public boolean removeFloor(String world, int x, int y, int z) {
        return floors.removeIf(f ->
            f.getWorld().equals(world) && f.getX() == x && f.getY() == y && f.getZ() == z
        );
    }

    public ElevatorFloor getFloor(int index) {
        if (index >= 0 && index < floors.size()) {
            return floors.get(index);
        }
        return null;
    }

    public int getFloorCount() {
        return floors.size();
    }

    public static class ElevatorFloor {
        private String world;
        private int x, y, z;
        private String displayName;

        public ElevatorFloor(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public ElevatorFloor(String world, int x, int y, int z, String displayName) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.displayName = displayName;
        }

        public String getWorld() { return world; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String name) { this.displayName = name; }

        public Location toLocation() {
            World w = Bukkit.getWorld(world);
            if (w == null) return null;
            return new Location(w, x, y + 1, z);
        }

        public String toCoordString() {
            return world + ":" + x + "," + y + "," + z;
        }
    }
}
