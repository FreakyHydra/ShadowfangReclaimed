package com.shadowfang.core.elevator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shadowfang.core.ShadowfangCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
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

        String groupName = findGroupAt(world, x, y, z);
        if (groupName == null) return;

        ElevatorGroup group = groups.get(groupName);
        if (group == null || group.getFloorCount() < 2) {
            player.sendMessage("§cNot enough floors in this teleporter group.");
            return;
        }

        List<ElevatorGroup.ElevatorFloor> destinations = new ArrayList<>();
        for (ElevatorGroup.ElevatorFloor floor : group.getFloors()) {
            if (!floor.getWorld().equals(world) || floor.getX() != x || floor.getY() != y || floor.getZ() != z) {
                destinations.add(floor);
            }
        }

        if (destinations.isEmpty()) {
            player.sendMessage("§cNo other floors available.");
            return;
        }

        if (destinations.size() == 1) {
            teleportTo(player, destinations.get(0));
        } else {
            pendingDestinations.put(id, destinations);
            showDestinationMenu(player, destinations);
        }
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
        player.teleport(to);
        spawnArrivalEffects(player);

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 4, false, false));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.playSound(to, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

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

    private void showDestinationMenu(Player player, List<ElevatorGroup.ElevatorFloor> destinations) {
        StringBuilder sb = new StringBuilder();
        sb.append("§6§lTeleport Menu\n");
        sb.append("§7Choose a destination:\n");

        for (int i = 0; i < destinations.size(); i++) {
            ElevatorGroup.ElevatorFloor floor = destinations.get(i);
            String name = floor.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = "Floor " + (i + 1);
            }
            sb.append("§e").append(i + 1).append(". §f").append(name);
            sb.append(" §7(").append(floor.getWorld()).append(": ").append(floor.getX()).append(", ").append(floor.getY()).append(", ").append(floor.getZ()).append(")\n");
        }

        sb.append("§7Type the number in chat to select.");

        player.sendMessage(Component.text(sb.toString()));
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean handleChatSelection(Player player, String message) {
        UUID id = player.getUniqueId();
        List<ElevatorGroup.ElevatorFloor> destinations = pendingDestinations.get(id);
        if (destinations == null) return false;

        int index;
        try {
            index = Integer.parseInt(message.trim()) - 1;
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid selection. Type a number from the menu.");
            return true;
        }

        if (index < 0 || index >= destinations.size()) {
            player.sendMessage("§cInvalid floor number.");
            return true;
        }

        pendingDestinations.remove(id);
        teleportTo(player, destinations.get(index));
        return true;
    }

    public void clearPendingDestination(UUID id) {
        pendingDestinations.remove(id);
    }

    private static class ElevatorData {
        Map<String, ElevatorGroup> groups;
        ElevatorData(Map<String, ElevatorGroup> groups) {
            this.groups = groups;
        }
    }
}
