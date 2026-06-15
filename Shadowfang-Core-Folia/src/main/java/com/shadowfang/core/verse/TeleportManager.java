package com.shadowfang.core.verse;

import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TeleportManager implements CommandExecutor, TabCompleter {

    private static TeleportManager instance;
    private final ShadowfangCorePlugin plugin;
    private final Map<String, String> aliases = new HashMap<>();
    private static final Map<String, String> WORLD_DISPLAY_NAMES = new HashMap<>();
    private final File worldsFile;
    private YamlConfiguration worldsConfig;
    private static final double TRAVEL_COST = 10.0;

    public TeleportManager(ShadowfangCorePlugin plugin) {
        instance = this;
        this.plugin = plugin;
        aliases.put("forest", "world");
        aliases.put("plains", "world");
        aliases.put("overworld", "world");
        aliases.put("spawn", "world");
        aliases.put("bitterroot", "shadowfang:bitterroot");

        this.worldsFile = new File(plugin.getDataFolder(), "worlds.yml");
        loadWorldsConfig();
    }

    public static void registerWorld(String worldName, String displayName) {
        WORLD_DISPLAY_NAMES.put(worldName, displayName);
        VerseManager.getInstance().registerWorld(worldName, displayName);
    }

    public static void unregisterWorld(String worldName) {
        WORLD_DISPLAY_NAMES.remove(worldName);
        VerseManager.getInstance().unregisterWorld(worldName);
    }

    public static Map<String, String> getRegisteredWorlds() {
        return new HashMap<>(WORLD_DISPLAY_NAMES);
    }

    public static TeleportManager getInstance() {
        return instance;
    }

    private void loadWorldsConfig() {
        if (!worldsFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                worldsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create worlds.yml: " + e.getMessage());
            }
        }
        worldsConfig = YamlConfiguration.loadConfiguration(worldsFile);
    }

    public void saveWorldsConfig() {
        try {
            worldsConfig.save(worldsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save worlds.yml: " + e.getMessage());
        }
    }

    public void loadSavedWorlds() {
        ConfigurationSection section = worldsConfig.getConfigurationSection("worlds");
        if (section == null) return;

        for (String worldName : section.getKeys(false)) {
            String display = section.getString(worldName + ".display", worldName);
            String genType = section.getString(worldName + ".generator", "normal");

            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                registerWorld(world.getName(), display);
                if (!world.getName().equals(worldName)) {
                    registerWorld(worldName, display);
                }
                plugin.getLogger().info("[Verse] Loaded saved world: " + worldName + " (" + genType + ")");
                worldsConfig.set("worlds." + worldName + ".display", display);
                worldsConfig.set("worlds." + worldName + ".generator", genType.toLowerCase());
                saveWorldsConfig();
            }
        }
    }

    /**
     * Scan server root for world folders containing level.dat that aren't yet registered.
     * Only registers worlds that are currently loaded by the server.
     */
    public void autoDetectWorlds() {
        File serverRoot = plugin.getDataFolder().getParentFile();
        if (serverRoot == null || !serverRoot.isDirectory()) return;

        File[] files = serverRoot.listFiles();
        if (files == null) return;

        int imported = 0;
        for (File file : files) {
            if (!file.isDirectory()) continue;

            String name = file.getName();
            if (WORLD_DISPLAY_NAMES.containsKey(name)) continue;

            File levelDat = new File(file, "level.dat");
            if (!levelDat.exists()) continue;

            World world = plugin.getServer().getWorld(name);
            if (world == null) continue;

            String displayName = deriveDisplayName(name);
            registerWorld(name, displayName);
            worldsConfig.set("worlds." + name + ".display", displayName);
            worldsConfig.set("worlds." + name + ".generator", "normal");
            imported++;
            plugin.getLogger().info("[Verse] Auto-imported world: " + name + " (" + displayName + ")");
        }

        if (imported > 0) {
            saveWorldsConfig();
            plugin.getLogger().info("[Verse] Auto-imported " + imported + " world(s).");
        }
    }

    /**
     * Import a loaded world that isn't registered yet.
     */
    public boolean importWorld(String worldName, String displayName) {
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) return false;

        if (WORLD_DISPLAY_NAMES.containsKey(worldName)) return false;

        if (displayName == null || displayName.isBlank()) {
            displayName = deriveDisplayName(worldName);
        }

        registerWorld(worldName, displayName);
        worldsConfig.set("worlds." + worldName + ".display", displayName);
        worldsConfig.set("worlds." + worldName + ".generator", "normal");
        saveWorldsConfig();
        return true;
    }

    /**
     * Import all loaded worlds that aren't registered yet.
     */
    public int importAllWorlds() {
        int imported = 0;
        for (World world : plugin.getServer().getWorlds()) {
            String name = world.getName();
            if (WORLD_DISPLAY_NAMES.containsKey(name)) continue;

            String displayName = deriveDisplayName(name);
            registerWorld(name, displayName);
            worldsConfig.set("worlds." + name + ".display", displayName);
            worldsConfig.set("worlds." + name + ".generator", "normal");
            imported++;
        }
        if (imported > 0) {
            saveWorldsConfig();
        }
        return imported;
    }

    /**
     * Get world names that are loaded but NOT registered.
     */
    public List<String> getUnregisteredWorlds() {
        List<String> unregistered = new ArrayList<>();
        for (World world : plugin.getServer().getWorlds()) {
            if (!WORLD_DISPLAY_NAMES.containsKey(world.getName())) {
                unregistered.add(world.getName());
            }
        }
        return unregistered;
    }

    private static String deriveDisplayName(String folderName) {
        String name = folderName;
        int colon = name.indexOf(':');
        if (colon >= 0) name = name.substring(colon + 1);

        String[] parts = name.replace("_", " ").replace("-", " ").split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) sb.append(part.substring(1));
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    // ─────────────────────────────────────────────────────────────────
    // Command Handling
    // ─────────────────────────────────────────────────────────────────

    public boolean handleCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            showHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create" -> {
                if (sender instanceof Player player && !player.hasPermission("shadowfang.verse.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /sfv create <name> [normal|nether|end|void|flat]");
                    return true;
                }
                String name = args[1].toLowerCase();
                String type = args[2];

                VerseManager.WorldType worldType;
                try {
                    worldType = VerseManager.WorldType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid type: " + type);
                    sender.sendMessage(ChatColor.GRAY + "Types: normal, nether, end, void, flat");
                    return true;
                }

                sender.sendMessage(ChatColor.YELLOW + "Creating world '" + name + "'...");

                VerseManager.getInstance().createWorld(name, worldType).thenAccept(world -> {
                    if (world != null) {
                        sender.sendMessage(ChatColor.GREEN + "World '" + name + "' created successfully!");
                        registerWorld(world.getName(), capitalize(name));
                        if (!world.getName().equals(name)) {
                            registerWorld(name, capitalize(name));
                        }
                        worldsConfig.set("worlds." + name + ".display", capitalize(name));
                        worldsConfig.set("worlds." + name + ".generator", type.toLowerCase());
                        saveWorldsConfig();
                        if (sender instanceof Player player) {
                            player.teleportAsync(world.getSpawnLocation());
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Failed to create world '" + name + "'.");
                    }
                }).exceptionally(ex -> {
                    sender.sendMessage(ChatColor.RED + "Error creating world: " + ex.getMessage());
                    return null;
                });
                return true;
            }
            case "remove" -> {
                if (sender instanceof Player player && !player.hasPermission("shadowfang.verse.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /sfv remove <name>");
                    return true;
                }
                String name = stripNamespace(args[1].toLowerCase());
                if (name.equals("world") || name.equals("world_nether") || name.equals("world_the_end")) {
                    sender.sendMessage(ChatColor.RED + "Cannot remove core server worlds.");
                    return true;
                }

                sender.sendMessage(ChatColor.YELLOW + "Attempting to unload world '" + name + "'...");

                VerseManager.getInstance().unloadWorld(name, true).thenAccept(unloaded -> {
                    if (unloaded) {
                        sender.sendMessage(ChatColor.GREEN + "World '" + name + "' unloaded.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Failed to unload world '" + name + "'.");
                    }
                });
                return true;
            }
            case "hub" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                if (chargeTravel(player)) return true;
                teleportToHub(player);
            }
            case "warp", "travel" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /" + args[0] + " <name>");
                    player.sendMessage(ChatColor.GRAY + "Worlds: " + getWorldList());
                    return true;
                }
                if (chargeTravel(player)) return true;
                teleportToBiome(player, args[1]);
            }
            case "worlds", "list" -> listWorlds(sender);
            case "sign" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /sfv sign <name>");
                    player.sendMessage(ChatColor.GRAY + "Worlds: " + getWorldList());
                    return true;
                }
                placeBiomeSign(player, args[1]);
            }
            case "setspawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                handleSetSpawn(player);
            }
            case "spawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                if (chargeTravel(player)) return true;
                handleSpawn(player, args);
            }
            case "import" -> {
                if (sender instanceof Player player && !player.hasPermission("shadowfang.verse.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /sfv import <worldName> [displayName]");
                    return true;
                }
                String worldName = args[1];
                String displayName = args.length >= 3 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : null;

                World world = plugin.getServer().getWorld(worldName);
                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "World '" + worldName + "' is not loaded.");
                    sender.sendMessage(ChatColor.GRAY + "Loaded worlds: " + getWorldList());
                    return true;
                }

                if (WORLD_DISPLAY_NAMES.containsKey(worldName)) {
                    sender.sendMessage(ChatColor.YELLOW + "World '" + worldName + "' is already registered as '" + WORLD_DISPLAY_NAMES.get(worldName) + "'.");
                    return true;
                }

                if (importWorld(worldName, displayName)) {
                    String finalDisplay = WORLD_DISPLAY_NAMES.get(worldName);
                    sender.sendMessage(ChatColor.GREEN + "Imported world '" + worldName + "' as '" + finalDisplay + "'!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to import world '" + worldName + "'.");
                }
            }
            case "import-all" -> {
                if (sender instanceof Player player && !player.hasPermission("shadowfang.verse.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                int count = importAllWorlds();
                if (count == 0) {
                    sender.sendMessage(ChatColor.YELLOW + "No unregistered worlds found. All loaded worlds are already registered.");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Imported " + count + " world(s)!");
                }
            }
            default -> showHelp(sender);
        }
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§7§l━━━━━━━━━━ §c§lShadowfang Reclaimed §7§l━━━━━━━━━━");
        sender.sendMessage("§7/sfv create <name> <type> - Create a world");
        sender.sendMessage("§7/sfv remove <name> - Unload a world");
        sender.sendMessage("§7/sfv import <name> [display] - Import a loaded world");
        sender.sendMessage("§7/sfv import-all - Import all unregistered worlds");
        sender.sendMessage("§7/sfv warp <world> - Teleport to a world");
        sender.sendMessage("§7/travel <world> - Teleport to a world (alias)");
        sender.sendMessage("§7/sfv list - List all loaded worlds");
        sender.sendMessage("§7/sfv spawn [world] - Go to spawn");
        sender.sendMessage("§7/sfv setspawn - Set world spawn");
        sender.sendMessage("§7/sfv sign <world> - Place portal sign");
        sender.sendMessage("§7/sfv hub - Teleport to hub");
        sender.sendMessage("§7§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("sfv")) {
            return handleCommand(sender, args);
        } else {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = label.toLowerCase();
            System.arraycopy(args, 0, newArgs, 1, args.length);
            return handleCommand(sender, newArgs);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("shadowfang.verse.admin")) {
                completions.addAll(List.of("create", "remove", "import", "import-all", "warp", "travel", "list", "spawn", "setspawn", "sign", "hub"));
            } else {
                completions.addAll(List.of("warp", "travel", "list", "spawn", "hub"));
            }
        } else if (args.length == 2) {
            String s = args[0].toLowerCase();
            if (s.equals("warp") || s.equals("travel") || s.equals("remove") || s.equals("sign") || s.equals("spawn")) {
                completions.addAll(WORLD_DISPLAY_NAMES.keySet());
            } else if (s.equals("import")) {
                completions.addAll(getUnregisteredWorlds());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            completions.addAll(List.of("normal", "flat", "void", "nether", "end"));
        }

        String partial = args[args.length - 1].toLowerCase();
        completions.removeIf(c -> !c.toLowerCase().startsWith(partial));
        return completions;
    }

    // ─────────────────────────────────────────────────────────────────
    // Teleport Helpers
    // ─────────────────────────────────────────────────────────────────

    private void teleportToHub(Player player) {
        World hub = VerseManager.getInstance().getWorld("hub");
        if (hub == null) hub = VerseManager.getInstance().getWorld("world_hub");

        if (hub == null) {
            player.sendMessage(ChatColor.YELLOW + "Hub world is loading...");
            VerseManager.getInstance().createWorld("hub", VerseManager.WorldType.VOID).thenAccept(createdHub -> {
                if (createdHub != null) {
                    player.teleportAsync(createdHub.getSpawnLocation());
                    player.sendMessage(ChatColor.GREEN + "Teleported to Hub!");
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to load hub world.");
                }
            }).exceptionally(ex -> {
                player.sendMessage(ChatColor.RED + "Error loading hub: " + ex.getMessage());
                return null;
            });
            return;
        }

        player.teleportAsync(hub.getSpawnLocation());
        player.sendMessage(ChatColor.GREEN + "Teleported to Hub!");
    }

    private void teleportToBiome(Player player, String input) {
        String worldName = resolveWorldName(input);
        if (worldName == null) {
            player.sendMessage(ChatColor.RED + "Unknown world: " + input);
            player.sendMessage(ChatColor.GRAY + "Worlds: " + getWorldList());
            return;
        }

        World world = VerseManager.getInstance().getWorld(worldName);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "World '" + worldName + "' is not loaded.");
            return;
        }

        player.teleportAsync(world.getSpawnLocation());
        String displayName = WORLD_DISPLAY_NAMES.getOrDefault(worldName, worldName);
        player.sendMessage(ChatColor.GREEN + "Teleported to " + displayName + "!");
    }

    private void listWorlds(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Loaded Worlds ===");
        for (Map.Entry<String, String> entry : WORLD_DISPLAY_NAMES.entrySet()) {
            World world = VerseManager.getInstance().getWorld(entry.getKey());
            if (world != null) {
                int players = world.getPlayers().size();
                sender.sendMessage(ChatColor.YELLOW + entry.getValue()
                    + ChatColor.GRAY + " - " + players + " player(s) (" + entry.getKey() + ")");
            }
        }
        World hub = VerseManager.getInstance().getWorld("hub");
        if (hub == null) hub = VerseManager.getInstance().getWorld("world_hub");
        if (hub != null && !WORLD_DISPLAY_NAMES.containsKey(hub.getName())) {
            sender.sendMessage(ChatColor.YELLOW + "Hub"
                + ChatColor.GRAY + " - " + hub.getPlayers().size() + " player(s) (" + hub.getName() + ")");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Sign Placement
    // ─────────────────────────────────────────────────────────────────

    private void placeBiomeSign(Player player, String input) {
        String worldName = resolveWorldName(input);
        if (worldName == null) {
            player.sendMessage(ChatColor.RED + "Unknown world: " + input);
            player.sendMessage(ChatColor.GRAY + "Worlds: " + getWorldList());
            return;
        }

        String displayName = WORLD_DISPLAY_NAMES.getOrDefault(worldName, worldName);

        org.bukkit.util.RayTraceResult ray = player.rayTraceBlocks(5, org.bukkit.FluidCollisionMode.NEVER);
        if (ray == null || ray.getHitBlock() == null) {
            player.sendMessage(ChatColor.RED + "Look at a block within 5 blocks to place the sign.");
            return;
        }

        Block targetBlock = ray.getHitBlock();
        BlockFace hitFace = ray.getHitBlockFace();
        if (hitFace == null) hitFace = BlockFace.UP;

        Block placeBlock = targetBlock.getRelative(hitFace);
        if (placeBlock.getType() != Material.AIR && placeBlock.getType() != Material.CAVE_AIR) {
            player.sendMessage(ChatColor.RED + "No space to place sign there.");
            return;
        }

        placeBlock.setType(Material.OAK_WALL_SIGN);
        Sign sign = (Sign) placeBlock.getState();

        org.bukkit.block.data.type.WallSign signData = (org.bukkit.block.data.type.WallSign) placeBlock.getBlockData();
        signData.setFacing(getSignDirection(hitFace, player));
        sign.setBlockData(signData);

        sign.setLine(0, ChatColor.GOLD + "" + ChatColor.BOLD + displayName);
        sign.setLine(1, ChatColor.GRAY + "Click to teleport");
        sign.setLine(2, "");
        sign.setLine(3, "");
        sign.update(true);

        player.sendMessage(ChatColor.GREEN + "Placed " + displayName + " sign!");
    }

    private BlockFace getSignDirection(BlockFace hitFace, Player player) {
        if (hitFace == BlockFace.UP || hitFace == BlockFace.DOWN) {
            return getCardinalDirection(player);
        }
        return hitFace;
    }

    private BlockFace getCardinalDirection(Player player) {
        float yaw = player.getLocation().getYaw();
        if (yaw < 0) yaw += 360;
        if (yaw >= 315 || yaw < 45) return BlockFace.SOUTH;
        if (yaw < 135) return BlockFace.WEST;
        if (yaw < 225) return BlockFace.NORTH;
        return BlockFace.EAST;
    }

    // ─────────────────────────────────────────────────────────────────
    // Travel Costs
    // ─────────────────────────────────────────────────────────────────

    private boolean chargeTravel(Player player) {
        com.shadowfang.core.faction.Faction faction = ShadowfangCorePlugin.getInstance().getFactionManager().getPlayerFaction(player.getUniqueId());
        if (faction != null) {
            return false;
        }

        com.shadowfang.core.economy.EconomyManager eco = ShadowfangCorePlugin.getInstance().getEconomyManager();
        if (!eco.removeBalance(player.getUniqueId(), TRAVEL_COST)) {
            player.sendMessage(ChatColor.RED + "You need " + String.format("%.0f", TRAVEL_COST) + " Silver Coins to travel. Join a Faction for free travel!");
            return true;
        }
        player.sendMessage(ChatColor.GRAY + "Paid " + String.format("%.0f", TRAVEL_COST) + " Silver Coins for travel.");
        return false;
    }

    // ─────────────────────────────────────────────────────────────────
    // Spawn Management
    // ─────────────────────────────────────────────────────────────────

    private void handleSetSpawn(Player player) {
        if (!player.hasPermission("shadowfang.verse.admin")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return;
        }
        Location loc = player.getLocation();
        player.getWorld().setSpawnLocation(loc);
        String worldName = player.getWorld().getName();
        String name = WORLD_DISPLAY_NAMES.getOrDefault(worldName, worldName);
        player.sendMessage(ChatColor.GREEN + "Spawn set for " + ChatColor.YELLOW + name + ChatColor.GREEN +
            " at " + ChatColor.GRAY + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    private void handleSpawn(Player player, String[] args) {
        World targetWorld;
        if (args.length >= 2) {
            String worldName = resolveWorldName(args[1]);
            if (worldName == null) {
                player.sendMessage(ChatColor.RED + "Unknown world: " + args[1]);
                player.sendMessage(ChatColor.GRAY + "Worlds: " + getWorldList());
                return;
            }
            targetWorld = VerseManager.getInstance().getWorld(worldName);
            if (targetWorld == null) {
                player.sendMessage(ChatColor.RED + "World " + args[1] + " is not loaded.");
                return;
            }
        } else {
            targetWorld = player.getWorld();
        }

        player.teleportAsync(targetWorld.getSpawnLocation());
        String name = WORLD_DISPLAY_NAMES.getOrDefault(targetWorld.getName(), targetWorld.getName());
        player.sendMessage(ChatColor.GREEN + "Teleported to " + ChatColor.YELLOW + name + ChatColor.GREEN + " spawn!");
    }

    // ─────────────────────────────────────────────────────────────────
    // Static Utilities
    // ─────────────────────────────────────────────────────────────────

    public static String getDisplayName(String worldName) {
        return WORLD_DISPLAY_NAMES.getOrDefault(worldName, worldName);
    }

    public static String getWorldNameFromSign(String signLine0) {
        String cleanLine = ChatColor.stripColor(signLine0).trim();
        for (Map.Entry<String, String> entry : WORLD_DISPLAY_NAMES.entrySet()) {
            if (cleanLine.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String resolveWorldName(String input) {
        String lower = stripNamespace(input.toLowerCase());
        if (aliases.containsKey(lower)) return aliases.get(lower);

        for (Map.Entry<String, String> entry : WORLD_DISPLAY_NAMES.entrySet()) {
            String key = stripNamespace(entry.getKey().toLowerCase());
            if (key.equals(lower) || entry.getValue().toLowerCase().equals(lower)) {
                return entry.getKey();
            }
        }

        String withPrefix = "world_" + lower;
        for (String worldName : WORLD_DISPLAY_NAMES.keySet()) {
            if (stripNamespace(worldName.toLowerCase()).equals(withPrefix)) {
                return worldName;
            }
        }

        if (VerseManager.getInstance().getWorld(lower) != null) return lower;
        if (VerseManager.getInstance().getWorld(withPrefix) != null) return withPrefix;

        return null;
    }

    private String getWorldList() {
        return WORLD_DISPLAY_NAMES.values().stream().collect(Collectors.joining(", "));
    }

    private static String stripNamespace(String name) {
        if (name == null) return null;
        int colon = name.indexOf(':');
        return colon >= 0 ? name.substring(colon + 1) : name;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
