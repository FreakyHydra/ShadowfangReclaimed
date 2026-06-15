package com.shadowfang.core.verse;

import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Shadowfang's world management API.
 * Wraps the Worlds plugin internally but exposes a clean, simple interface.
 * All operations are async and Folia-safe.
 */
public class VerseManager {

    private static VerseManager instance;
    private final ShadowfangCorePlugin plugin;
    private final Map<String, String> displayNames = new HashMap<>();

    public enum WorldType {
        NORMAL, NETHER, END, VOID, FLAT
    }

    public VerseManager(ShadowfangCorePlugin plugin) {
        instance = this;
        this.plugin = plugin;
    }

    public static VerseManager getInstance() {
        return instance;
    }

    public void registerWorld(String worldName, String displayName) {
        displayNames.put(worldName, displayName);
    }

    public void unregisterWorld(String worldName) {
        displayNames.remove(worldName);
    }

    public String getDisplayName(String worldName) {
        return displayNames.getOrDefault(worldName, worldName);
    }

    public Map<String, String> getDisplayNames() {
        return new HashMap<>(displayNames);
    }

    public boolean isWorldLoaded(String name) {
        return plugin.getServer().getWorld(name) != null;
    }

    public World getWorld(String name) {
        return plugin.getServer().getWorld(name);
    }

    public CompletableFuture<World> createWorld(String name, WorldType type) {
        return createWorld(name, type, null);
    }

    /**
     * Create a new world via the Worlds plugin engine.
     */
    public CompletableFuture<World> createWorld(String name, WorldType type, Long seed) {
        CompletableFuture<World> future = new CompletableFuture<>();

        World existing = plugin.getServer().getWorld(name);
        if (existing != null) {
            future.complete(existing);
            return future;
        }

        if (!org.bukkit.Bukkit.getPluginManager().isPluginEnabled("Worlds")) {
            plugin.getLogger().severe("[Verse] Worlds plugin not available. Cannot create world '" + name + "'.");
            future.completeExceptionally(new IllegalStateException("Worlds plugin not available"));
            return future;
        }

        try {
            plugin.getLogger().info("[Verse] Creating world '" + name + "' (type: " + type + ")...");

            net.kyori.adventure.key.Key worldKey = net.kyori.adventure.key.Key.key("shadowfang", name);
            net.thenextlvl.worlds.Level.Builder builder = net.thenextlvl.worlds.Level.builder(worldKey);

            switch (type) {
                case NETHER -> builder.dimension(net.thenextlvl.worlds.Dimension.THE_NETHER);
                case END -> builder.dimension(net.thenextlvl.worlds.Dimension.THE_END);
                default -> builder.dimension(net.thenextlvl.worlds.Dimension.OVERWORLD);
            }

            switch (type) {
                case VOID -> builder.generatorType(
                    net.thenextlvl.worlds.generator.GeneratorType.FLAT.with(
                        net.thenextlvl.worlds.preset.Preset.THE_VOID));
                case FLAT -> builder.generatorType(
                    net.thenextlvl.worlds.generator.GeneratorType.FLAT);
                default -> builder.generatorType(
                    net.thenextlvl.worlds.generator.GeneratorType.NORMAL);
            }

            if (seed != null) {
                builder.seed(seed);
            }

            net.thenextlvl.worlds.Level level = builder.build();

            level.create().thenAccept(world -> {
                if (world != null) {
                    plugin.getLogger().info("[Verse] World '" + name + "' created successfully.");
                    registerWorld(world.getName(), capitalize(name));
                    // Also register under the simple name if Worlds plugin uses a namespace
                    if (!world.getName().equals(name)) {
                        registerWorld(name, capitalize(name));
                    }
                    future.complete(world);
                } else {
                    plugin.getLogger().severe("[Verse] World '" + name + "' creation returned null.");
                    future.completeExceptionally(new RuntimeException("World creation returned null"));
                }
            }).exceptionally(ex -> {
                plugin.getLogger().log(Level.SEVERE, "[Verse] Failed to create world '" + name + "'", ex);
                future.completeExceptionally(ex);
                return null;
            });

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[Verse] Exception creating world '" + name + "'", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    public CompletableFuture<Boolean> unloadWorld(String name, boolean save) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        World world = plugin.getServer().getWorld(name);
        if (world == null) {
            future.complete(false);
            return future;
        }

        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            boolean unloaded = plugin.getServer().unloadWorld(world, save);
            if (unloaded) {
                unregisterWorld(name);
                plugin.getLogger().info("[Verse] World '" + name + "' unloaded.");
            }
            future.complete(unloaded);
        });

        return future;
    }

    public CompletableFuture<Boolean> teleportToWorld(Player player, String worldName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            future.complete(false);
            return future;
        }

        player.teleportAsync(world.getSpawnLocation()).thenAccept(future::complete)
            .exceptionally(ex -> {
                future.complete(false);
                return null;
            });

        return future;
    }

    public String getWorldList() {
        StringBuilder sb = new StringBuilder();
        for (World world : plugin.getServer().getWorlds()) {
            String display = getDisplayName(world.getName());
            int players = world.getPlayers().size();
            if (sb.length() > 0) sb.append(", ");
            sb.append(display).append(" (").append(players).append(")");
        }
        return sb.toString();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
