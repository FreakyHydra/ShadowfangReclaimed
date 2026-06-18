package com.shadowfang.talisman;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class VeinMineManager {

    private static final int RADIUS = 5;
    private static final Set<Material> BLACKLIST = EnumSet.noneOf(Material.class);
    private static final Set<Material> SOFT_BLOCKS = EnumSet.noneOf(Material.class);

    static {
        BLACKLIST.add(Material.BEDROCK);
        BLACKLIST.add(Material.OBSIDIAN);
        BLACKLIST.add(Material.SPAWNER);
        BLACKLIST.add(Material.SHULKER_BOX);
        BLACKLIST.add(Material.ENDER_CHEST);
        BLACKLIST.add(Material.BEACON);
        BLACKLIST.add(Material.CRYING_OBSIDIAN);
        BLACKLIST.add(Material.END_PORTAL_FRAME);
        BLACKLIST.add(Material.RESPAWN_ANCHOR);
        BLACKLIST.add(Material.NETHER_PORTAL);
        BLACKLIST.add(Material.END_PORTAL);
        BLACKLIST.add(Material.END_GATEWAY);
        BLACKLIST.add(Material.JIGSAW);
        BLACKLIST.add(Material.STRUCTURE_BLOCK);
        BLACKLIST.add(Material.STRUCTURE_VOID);
        BLACKLIST.add(Material.COMMAND_BLOCK);
        BLACKLIST.add(Material.CHAIN_COMMAND_BLOCK);
        BLACKLIST.add(Material.REPEATING_COMMAND_BLOCK);

        SOFT_BLOCKS.add(Material.DIRT);
        SOFT_BLOCKS.add(Material.GRASS_BLOCK);
        SOFT_BLOCKS.add(Material.COARSE_DIRT);
        SOFT_BLOCKS.add(Material.ROOTED_DIRT);
        SOFT_BLOCKS.add(Material.MOSS_BLOCK);
        SOFT_BLOCKS.add(Material.SAND);
        SOFT_BLOCKS.add(Material.GRAVEL);
        SOFT_BLOCKS.add(Material.CLAY);
        SOFT_BLOCKS.add(Material.MUD);
        SOFT_BLOCKS.add(Material.SOUL_SAND);
        SOFT_BLOCKS.add(Material.SOUL_SOIL);
        SOFT_BLOCKS.add(Material.POWDER_SNOW);
    }

    private final Map<UUID, Long> prospectCooldowns = new HashMap<>();
    private final Map<UUID, Long> bloomCooldowns = new HashMap<>();
    private final Map<UUID, Long> fervorCooldowns = new HashMap<>();
    private final Map<UUID, Long> auroraCooldowns = new HashMap<>();
    private final Map<UUID, Long> fogCooldowns = new HashMap<>();
    private final AbyssalTalismanPlugin plugin;

    public VeinMineManager(AbyssalTalismanPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isProspectOnCooldown(Player player) {
        Long last = prospectCooldowns.get(player.getUniqueId());
        if (last == null) return false;
        return System.currentTimeMillis() - last < 45000;
    }

    public void useProspect(Player player) {
        prospectCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isOnCooldown(Player player, String ability) {
        return switch (ability) {
            case "prospect" -> isProspectOnCooldown(player);
            case "bloom" -> {
                Long last = bloomCooldowns.get(player.getUniqueId());
                yield last != null && System.currentTimeMillis() - last < 60000;
            }
            case "fervor" -> {
                Long last = fervorCooldowns.get(player.getUniqueId());
                yield last != null && System.currentTimeMillis() - last < 30000;
            }
            case "aurora" -> {
                Long last = auroraCooldowns.get(player.getUniqueId());
                yield last != null && System.currentTimeMillis() - last < 60000;
            }
            case "fog" -> {
                Long last = fogCooldowns.get(player.getUniqueId());
                yield last != null && System.currentTimeMillis() - last < 60000;
            }
            default -> false;
        };
    }

    public void useAbility(Player player, String ability) {
        switch (ability) {
            case "prospect" -> prospectCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            case "bloom" -> bloomCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            case "fervor" -> fervorCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            case "aurora" -> auroraCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            case "fog" -> fogCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    public void onBlockBreak(Player player, Block block, ItemStack tool, TalismanType type, TalismanMode mode, boolean cursed) {
        if (type != TalismanType.ABYSSAL) return;

        if (player.isOp()) cursed = false;

        switch (mode) {
            case PRIMARY -> mineVein(player, block, tool, cursed);
            case SECONDARY -> excavate(player, block, tool, cursed);
            case TERTIARY -> {
                if (!isProspectOnCooldown(player)) {
                    prospect(player, cursed);
                } else {
                    player.sendMessage("§cProspect is on cooldown. Wait 45 seconds.");
                }
            }
        }
    }

    private void mineVein(Player player, Block startBlock, ItemStack tool, boolean cursed) {
        Location playerLoc = player.getLocation();
        Material startType = startBlock.getType();

        if (BLACKLIST.contains(startType)) return;

        Set<Block> toBreak = new HashSet<>();
        boolean isTree = isLog(startType) || isLeaves(startType);

        if (isTree) {
            collectTree(startBlock, toBreak, startType, playerLoc, cursed);
        } else {
            collectOreVein(startBlock, toBreak, startType, playerLoc, cursed);
        }

        if (toBreak.size() <= 1) return;

        toBreak.remove(startBlock);

        int radius = cursed ? 15 : RADIUS;

        for (Block b : toBreak) {
            if (!withinRadius(b.getLocation(), playerLoc, radius)) continue;

            if (tool != null && tool.getType().getMaxDurability() > 0) {
                short durability = tool.getDurability();
                if (durability >= tool.getType().getMaxDurability() - 1) {
                    tool.setDurability(tool.getType().getMaxDurability());
                    player.getWorld().playSound(playerLoc, Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    break;
                }
                tool.setDurability((short) (durability + 1));
            }

            Location loc = b.getLocation();
            List<ItemStack> drops = new ArrayList<>(b.getDrops(tool));
            b.setType(Material.AIR);

            for (ItemStack drop : drops) {
                loc.getWorld().dropItemNaturally(startBlock.getLocation().add(0.5, 0.5, 0.5), drop);
            }
        }

        player.getWorld().playSound(playerLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f);
        int hungerTicks = cursed ? 240 : 80;
        player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, hungerTicks, cursed ? 3 : 2));
    }

    private void excavate(Player player, Block block, ItemStack tool, boolean cursed) {
        Location playerLoc = player.getLocation();
        int size = cursed ? 5 : 3;
        int half = size / 2;

        Set<Block> toBreak = new HashSet<>();
        BlockFace face = getFacing(player);

        for (int dx = -half; dx <= half; dx++) {
            for (int dy = -half; dy <= half; dy++) {
                Block target = block.getRelative(face.getModX() + dx, dy, face.getModZ() + dx);
                if (SOFT_BLOCKS.contains(target.getType()) && !BLACKLIST.contains(target.getType())) {
                    if (withinRadius(target.getLocation(), playerLoc, 5)) {
                        toBreak.add(target);
                    }
                }
            }
        }

        if (toBreak.isEmpty()) return;

        for (Block b : toBreak) {
            if (tool != null && tool.getType().getMaxDurability() > 0) {
                short durability = tool.getDurability();
                if (durability >= tool.getType().getMaxDurability() - 1) {
                    tool.setDurability(tool.getType().getMaxDurability());
                    player.getWorld().playSound(playerLoc, Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    break;
                }
                tool.setDurability((short) (durability + 1));
            }

            List<ItemStack> drops = new ArrayList<>(b.getDrops(tool));
            Location loc = b.getLocation();
            b.setType(Material.AIR);
            for (ItemStack drop : drops) {
                loc.getWorld().dropItemNaturally(loc.add(0.5, 0.5, 0.5), drop);
            }
        }

        player.getWorld().playSound(playerLoc, Sound.BLOCK_GRAVEL_BREAK, 1.0f, 1.2f);
        int fatigueTicks = cursed ? 100 : 60;
        int fatigueLevel = cursed ? 2 : 1;
        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, fatigueTicks, fatigueLevel));
    }

    private void prospect(Player player, boolean cursed) {
        useProspect(player);
        Location playerLoc = player.getLocation();
        int radius = cursed ? 20 : 12;
        int duration = 100;

        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, duration, 255, true, false, false));

        player.getWorld().playSound(playerLoc, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);

        int finalRadius = radius;
        int[] ticks = {0};
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            ticks[0] += 5;
            if (ticks[0] >= duration) {
                task.cancel();
                return;
            }

            for (Block b : getBlocksInSphere(playerLoc, finalRadius)) {
                if (isOre(b.getType())) {
                    playerLoc.getWorld().spawnParticle(org.bukkit.Particle.GLOW, b.getLocation().add(0.5, 0.5, 0.5), 3, 0.3, 0.3, 0.3, 0.02);
                }
            }
        }, 0L, 5L);
    }

    private Set<Block> getBlocksInSphere(Location center, int radius) {
        Set<Block> blocks = new HashSet<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                        Block b = center.clone().add(dx, dy, dz).getBlock();
                        blocks.add(b);
                    }
                }
            }
        }
        return blocks;
    }

    private void collectOreVein(Block start, Set<Block> collected, Material targetType, Location playerLoc, boolean cursed) {
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);
        int radius = cursed ? 15 : RADIUS;

        while (!queue.isEmpty()) {
            Block current = queue.poll();
            collected.add(current);

            for (Block neighbor : getNeighbors(current)) {
                if (visited.contains(neighbor)) continue;
                visited.add(neighbor);
                if (!withinRadius(neighbor.getLocation(), playerLoc, radius)) continue;
                if (neighbor.getType() != targetType) continue;
                if (BLACKLIST.contains(neighbor.getType())) continue;
                queue.add(neighbor);
            }
        }
    }

    private void collectTree(Block start, Set<Block> collected, Material targetType, Location playerLoc, boolean cursed) {
        Queue<Block> logQueue = new LinkedList<>();
        Set<Block> visitedLogs = new HashSet<>();
        logQueue.add(start);
        visitedLogs.add(start);
        int radius = cursed ? 15 : RADIUS;

        while (!logQueue.isEmpty()) {
            Block current = logQueue.poll();
            collected.add(current);

            for (Block neighbor : getNeighbors(current)) {
                if (visitedLogs.contains(neighbor)) continue;
                if (!withinRadius(neighbor.getLocation(), playerLoc, radius)) continue;
                if (!isLog(neighbor.getType()) && neighbor.getType() != targetType) continue;
                if (BLACKLIST.contains(neighbor.getType())) continue;
                visitedLogs.add(neighbor);
                logQueue.add(neighbor);
            }
        }

        for (Block log : visitedLogs) {
            for (Block neighbor : getNeighbors(log)) {
                if (visitedLogs.contains(neighbor)) continue;
                if (!withinRadius(neighbor.getLocation(), playerLoc, radius)) continue;
                if (!isLeaves(neighbor.getType())) continue;
                if (neighbor.getType() == Material.AIR) continue;
                if (BLACKLIST.contains(neighbor.getType())) continue;
                collected.add(neighbor);
            }
        }
    }

    private List<Block> getNeighbors(Block block) {
        List<Block> neighbors = new ArrayList<>();
        int[][] offsets = {{1,0,0}, {-1,0,0}, {0,1,0}, {0,-1,0}, {0,0,1}, {0,0,-1}};
        for (int[] o : offsets) {
            neighbors.add(block.getRelative(o[0], o[1], o[2]));
        }
        return neighbors;
    }

    private boolean withinRadius(Location blockLoc, Location playerLoc, int radius) {
        if (!blockLoc.getWorld().equals(playerLoc.getWorld())) return false;
        double dx = blockLoc.getX() - playerLoc.getX();
        double dy = blockLoc.getY() - playerLoc.getY();
        double dz = blockLoc.getZ() - playerLoc.getZ();
        return Math.sqrt(dx*dx + dy*dy + dz*dz) <= radius;
    }

    private boolean isLog(Material m) {
        return m.name().endsWith("_LOG") || m.name().endsWith("_STEM") || m == Material.CRIMSON_STEM || m == Material.WARPED_STEM;
    }

    private boolean isLeaves(Material m) {
        return m.name().endsWith("_LEAVES");
    }

    private boolean isOre(Material m) {
        return m.name().contains("ORE") || m == Material.DEEPSLATE || m.name().contains("NETHER") && m.name().contains("GOLD");
    }

    private BlockFace getFacing(Player player) {
        double pitch = player.getLocation().getPitch();
        if (pitch >= 45) return BlockFace.BOTTOM;
        if (pitch <= -45) return BlockFace.TOP;
        double yaw = player.getLocation().getYaw();
        yaw = (yaw + 360) % 360;
        if (yaw < 45 || yaw >= 315) return BlockFace.SOUTH;
        if (yaw < 135) return BlockFace.WEST;
        if (yaw < 225) return BlockFace.NORTH;
        return BlockFace.EAST;
    }

    private enum BlockFace {
        NORTH, SOUTH, EAST, WEST, TOP, BOTTOM;
        int getModX() { return this == EAST ? 1 : this == WEST ? -1 : 0; }
        int getModZ() { return this == SOUTH ? 1 : this == NORTH ? -1 : 0; }
    }
}
