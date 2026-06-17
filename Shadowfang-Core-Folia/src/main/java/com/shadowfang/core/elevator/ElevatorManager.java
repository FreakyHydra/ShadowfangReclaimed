package com.shadowfang.core.elevator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ElevatorManager {

    private final ShadowfangCorePlugin plugin;
    private final Map<String, ElevatorGroup> groups = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, List<ElevatorGroup.ElevatorFloor>> pendingDestinations = new ConcurrentHashMap<>();
    private final Map<UUID, PendingFloor> pendingFloorNames = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Path dataFile;

    private static final long COOLDOWN_MS = 1000;

    public ElevatorManager(ShadowfangCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        Path dataDir = plugin.getDataFolder().toPath();
        try { Files.createDirectories(dataDir); } catch (IOException e) {}
        dataFile = dataDir.resolve("elevators.json");

        if (Files.exists(dataFile)) {
            try (Reader reader = Files.newBufferedReader(dataFile)) {
                ElevatorData data = gson.fromJson(reader, ElevatorData.class);
                if (data != null && data.groups != null) {
                    for (var e : data.groups.entrySet()) {
                        groups.put(e.getKey(), e.getValue());
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load elevators: " + e.getMessage());
            }
        }
    }

    public void save() {
        if (dataFile == null) return;
        Map<String, ElevatorGroup> toSave = new HashMap<>(groups);
        try (Writer writer = Files.newBufferedWriter(dataFile)) {
            gson.toJson(new ElevatorData(toSave), writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save elevators: " + e.getMessage());
        }
    }

    public boolean createGroup(String name) {
        if (groups.containsKey(name.toLowerCase())) return false;
        groups.put(name.toLowerCase(), new ElevatorGroup(name));
        save();
        return true;
    }

    public boolean removeGroup(String name) {
        boolean removed = groups.remove(name.toLowerCase()) != null;
        if (removed) save();
        return removed;
    }

    public ElevatorGroup getGroup(String name) {
        return groups.get(name.toLowerCase());
    }

    public Collection<ElevatorGroup> getAllGroups() {
        return groups.values();
    }

    public boolean addFloor(String groupName, String world, int x, int y, int z) {
        ElevatorGroup group = groups.get(groupName.toLowerCase());
        if (group == null) return false;
        group.addFloor(new ElevatorGroup.ElevatorFloor(world, x, y, z));
        save();
        return true;
    }

    public boolean addFloor(String groupName, String world, int x, int y, int z, String displayName) {
        ElevatorGroup group = groups.get(groupName.toLowerCase());
        if (group == null) return false;
        group.addFloor(new ElevatorGroup.ElevatorFloor(world, x, y, z, displayName));
        save();
        return true;
    }

    public boolean removeFloor(String groupName, int index) {
        ElevatorGroup group = groups.get(groupName.toLowerCase());
        if (group == null) return false;
        boolean removed = group.removeFloor(index);
        if (removed) save();
        return removed;
    }

    public boolean removeFloorAt(String world, int x, int y, int z) {
        for (ElevatorGroup group : groups.values()) {
            if (group.removeFloor(world, x, y, z)) {
                save();
                return true;
            }
        }
        return false;
    }

    public String findGroupAt(String world, int x, int y, int z) {
        for (var e : groups.entrySet()) {
            for (ElevatorGroup.ElevatorFloor floor : e.getValue().getFloors()) {
                if (floor.getWorld().equals(world) && floor.getX() == x && floor.getY() == y && floor.getZ() == z) {
                    return e.getKey();
                }
            }
        }
        return null;
    }

    public void activateTeleporter(Player player) {
        UUID id = player.getUniqueId();

        if (cooldowns.containsKey(id)) {
            long last = cooldowns.get(id);
            if (System.currentTimeMillis() - last < COOLDOWN_MS) {
                return;
            }
        }

        Location loc = player.getLocation();
        String world = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY() - 1;
        int z = loc.getBlockZ();

        String groupKey = findGroupAt(world, x, y, z);
        if (groupKey == null) return;

        ElevatorGroup group = groups.get(groupKey);
        if (group == null || group.getFloorCount() < 2) {
            player.sendMessage("§cNot enough floors in this teleporter group.");
            return;
        }

        List<ElevatorGroup.ElevatorFloor> allFloors = group.getFloors();
        List<ElevatorGroup.ElevatorFloor> destinations = new ArrayList<>();
        for (ElevatorGroup.ElevatorFloor floor : allFloors) {
            if (!floor.getWorld().equals(world) || floor.getX() != x || floor.getY() != y || floor.getZ() != z) {
                destinations.add(floor);
            }
        }

        if (destinations.isEmpty()) {
            player.sendMessage("§cNo other floors available.");
            return;
        }

        ElevatorGroup.ElevatorFloor floorAbove = null;
        ElevatorGroup.ElevatorFloor floorBelow = null;
        List<ElevatorGroup.ElevatorFloor> lateralFloors = new ArrayList<>();
        int tolerance = 1;

        for (ElevatorGroup.ElevatorFloor floor : destinations) {
            boolean sameX = Math.abs(floor.getX() - x) <= tolerance;
            boolean sameZ = Math.abs(floor.getZ() - z) <= tolerance;

            if (sameX && sameZ) {
                if (floor.getY() > y) {
                    if (floorAbove == null || floor.getY() < floorAbove.getY()) {
                        floorAbove = floor;
                    }
                } else if (floor.getY() < y) {
                    if (floorBelow == null || floor.getY() > floorBelow.getY()) {
                        floorBelow = floor;
                    }
                }
            } else {
                lateralFloors.add(floor);
            }
        }

        boolean hasVertical = (floorAbove != null || floorBelow != null);
        float pitch = player.getLocation().getPitch();
        boolean lookingUp = pitch < -30;
        boolean lookingDown = pitch > 30;

        if (!hasVertical) {
            if (destinations.size() == 1) {
                teleportTo(player, destinations.get(0));
            } else {
                pendingDestinations.put(id, destinations);
                openElevatorMenu(player, destinations, group.getName());
            }
            return;
        }

        if (floorAbove != null && floorBelow != null) {
            if (lookingUp) {
                teleportTo(player, floorAbove);
            } else if (lookingDown) {
                teleportTo(player, floorBelow);
            } else {
                List<ElevatorGroup.ElevatorFloor> combined = new ArrayList<>();
                combined.add(floorAbove);
                combined.add(floorBelow);
                combined.addAll(lateralFloors);
                pendingDestinations.put(id, combined);
                openElevatorMenu(player, combined, group.getName());
            }
        } else if (floorAbove != null) {
            teleportTo(player, floorAbove);
        } else {
            teleportTo(player, floorBelow);
        }
    }

    private void openElevatorMenu(Player player, List<ElevatorGroup.ElevatorFloor> destinations, String groupName) {
        ElevatorMenu.registerMenu(player.getUniqueId());
        Inventory inv = ElevatorMenu.createMenu(player, destinations, groupName);
        player.openInventory(inv);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void handleMenuClick(Player player, int slot) {
        UUID id = player.getUniqueId();
        List<ElevatorGroup.ElevatorFloor> destinations = pendingDestinations.get(id);
        if (destinations == null) return;
        if (slot < 0 || slot >= destinations.size()) return;

        pendingDestinations.remove(id);
        player.closeInventory();
        teleportTo(player, destinations.get(slot));
    }

    private void teleportTo(Player player, ElevatorGroup.ElevatorFloor destination) {
        Location from = player.getLocation();
        Location to = destination.toLocation();
        if (to == null) {
            player.sendMessage("§cDestination world not loaded.");
            return;
        }

        to.setYaw(from.getYaw());
        to.setPitch(from.getPitch());

        spawnDepartureEffects(player);
        player.teleportAsync(to).thenAccept(success -> {
            if (success) {
                spawnArrivalEffects(player);
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 4, false, false));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.playSound(to, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        });

        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void spawnDepartureEffects(Player player) {
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 0.5, 0), 20, 0.3, 0.5, 0.3, 0.05);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 0.5, 0), 15, 0.2, 0.3, 0.2, 0.02);
    }

    private void spawnArrivalEffects(Player player) {
        Location loc = player.getLocation();
        player.getWorld().spawnParticle(Particle.END_ROD, loc.add(0, 0.5, 0), 20, 0.3, 0.5, 0.3, 0.05);
        player.getWorld().spawnParticle(Particle.PORTAL, loc.add(0, 0.5, 0), 15, 0.2, 0.3, 0.2, 0.02);
    }

    public boolean handleChatSelection(Player player, String message) {
        return false;
    }

    public void clearPendingDestination(UUID id) {
        pendingDestinations.remove(id);
    }

    public void clearPendingFloorName(UUID id) {
        pendingFloorNames.remove(id);
    }

    private static class ElevatorData {
        Map<String, ElevatorGroup> groups;
        ElevatorData(Map<String, ElevatorGroup> groups) {
            this.groups = groups;
        }
    }

    private record PendingFloor(String groupName, String world, int x, int y, int z) {}

    public void promptFloorNaming(Player player, String groupName, String world, int x, int y, int z) {
        pendingFloorNames.put(player.getUniqueId(), new PendingFloor(groupName, world, x, y, z));
        player.sendMessage("§6§lFloor Name §7- Type a name for this floor, or §e\"skip\"§7 for no name.");
    }

    public boolean handleFloorNaming(Player player, String message) {
        PendingFloor pending = pendingFloorNames.remove(player.getUniqueId());
        if (pending == null) return false;

        String name = message.trim();
        if (name.equalsIgnoreCase("skip") || name.isEmpty()) {
            addFloor(pending.groupName(), pending.world(), pending.x(), pending.y(), pending.z());
            player.sendMessage("§aFloor added at §f" + pending.x() + "," + pending.y() + "," + pending.z() + " §awithout a name.");
        } else {
            addFloor(pending.groupName(), pending.world(), pending.x(), pending.y(), pending.z(), name);
            player.sendMessage("§aFloor added as §e\"" + name + "\" §aat §f" + pending.x() + "," + pending.y() + "," + pending.z() + "§a.");
        }
        return true;
    }

    public boolean renameFloor(String groupName, int index, String newName) {
        ElevatorGroup group = groups.get(groupName.toLowerCase());
        if (group == null) return false;
        ElevatorGroup.ElevatorFloor floor = group.getFloor(index);
        if (floor == null) return false;
        floor.setDisplayName(newName);
        save();
        return true;
    }
}
