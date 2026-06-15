package com.shadowfang.core.faction;

import org.bukkit.Location;

import java.util.*;

public class Faction {
    private final UUID id;
    private String name;
    private String description;
    private double hoardBalance;
    private final Map<UUID, FactionRank> members;
    private final Set<Integer> unlockedLoreFragments;
    private transient Location bellLocation;
    private transient Location spawnLocation;
    private String bellWorld;
    private double bellX;
    private double bellY;
    private double bellZ;
    private String spawnWorld;
    private double spawnX;
    private double spawnY;
    private double spawnZ;

    public Faction(UUID id, String name, UUID alpha) {
        this.id = id;
        this.name = name;
        this.description = "A new Howling Faction";
        this.hoardBalance = 0.0;
        this.members = new HashMap<>();
        this.members.put(alpha, FactionRank.ALPHA);
        this.unlockedLoreFragments = new HashSet<>();
    }

    public UUID getId() { return id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getHoardBalance() { return hoardBalance; }
    public void addHoardBalance(double amount) { this.hoardBalance += amount; }
    public void removeHoardBalance(double amount) { this.hoardBalance -= amount; }

    public Map<UUID, FactionRank> getMembers() { return Collections.unmodifiableMap(members); }
    
    public void addMember(UUID player, FactionRank rank) {
        members.put(player, rank);
    }
    
    public void removeMember(UUID player) {
        members.remove(player);
    }
    
    public FactionRank getRank(UUID player) {
        return members.get(player);
    }

    public void setRank(UUID player, FactionRank rank) {
        if (members.containsKey(player)) {
            members.put(player, rank);
        }
    }

    public boolean hasUnlockedLore(int chapter) {
        return unlockedLoreFragments.contains(chapter);
    }

    public void unlockLore(int chapter) {
        unlockedLoreFragments.add(chapter);
    }
    
    public Set<Integer> getUnlockedLoreFragments() {
        return Collections.unmodifiableSet(unlockedLoreFragments);
    }

    public UUID getAlpha() {
        for (Map.Entry<UUID, FactionRank> entry : members.entrySet()) {
            if (entry.getValue() == FactionRank.ALPHA) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Location getBellLocation() {
        if (bellLocation == null && bellWorld != null) {
            bellLocation = new Location(
                org.bukkit.Bukkit.getWorld(bellWorld),
                bellX, bellY, bellZ
            );
        }
        return bellLocation;
    }

    public void setBellLocation(Location location) {
        this.bellLocation = location;
        if (location != null) {
            this.bellWorld = location.getWorld().getName();
            this.bellX = location.getX();
            this.bellY = location.getY();
            this.bellZ = location.getZ();
        } else {
            this.bellWorld = null;
        }
    }

    public Location getSpawnLocation() {
        if (spawnLocation == null && spawnWorld != null) {
            spawnLocation = new Location(
                org.bukkit.Bukkit.getWorld(spawnWorld),
                spawnX, spawnY, spawnZ
            );
        }
        return spawnLocation;
    }

    public void setSpawnLocation(Location location) {
        this.spawnLocation = location;
        if (location != null) {
            this.spawnWorld = location.getWorld().getName();
            this.spawnX = location.getX();
            this.spawnY = location.getY();
            this.spawnZ = location.getZ();
        } else {
            this.spawnWorld = null;
        }
    }
}
