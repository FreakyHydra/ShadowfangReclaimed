package com.shadowfang.core.veinmining;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class VeinMineManager {

    private static final int RADIUS = 30;
    private static final Set<Material> BLACKLIST = EnumSet.noneOf(Material.class);

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
    }

    private final Set<UUID> enabledPlayers = new HashSet<>();

    public VeinMineManager() {}

    public boolean isEnabled(Player player) {
        return enabledPlayers.contains(player.getUniqueId());
    }

    public boolean toggle(Player player) {
        UUID id = player.getUniqueId();
        if (enabledPlayers.contains(id)) {
            enabledPlayers.remove(id);
            player.sendMessage("§cVeinmining disabled.");
            return false;
        } else {
            enabledPlayers.add(id);
            player.sendMessage("§aVeinmining enabled. Break a block to start mining veins.");
            return true;
        }
    }

    public void onBlockBreak(Player player, Block block, ItemStack tool) {
        UUID id = player.getUniqueId();
        if (!enabledPlayers.contains(id)) return;

        mineVein(player, block, tool);
    }

    private void mineVein(Player player, Block startBlock, ItemStack tool) {
        Location playerLoc = player.getLocation();
        Material startType = startBlock.getType();

        if (BLACKLIST.contains(startType)) return;

        Set<Block> toBreak = new HashSet<>();
        boolean isTree = isLog(startType) || isLeaves(startType);

        if (isTree) {
            collectTree(startBlock, toBreak, startType, playerLoc);
        } else {
            collectOreVein(startBlock, toBreak, startType, playerLoc);
        }

        if (toBreak.size() <= 1) return;

        toBreak.remove(startBlock);

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

            Location loc = b.getLocation();
            List<ItemStack> drops = new ArrayList<>(b.getDrops(tool));
            b.setType(Material.AIR);

            for (ItemStack drop : drops) {
                loc.getWorld().dropItemNaturally(startBlock.getLocation().add(0.5, 0.5, 0.5), drop);
            }
        }

        player.getWorld().playSound(playerLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f);
    }

    private void collectOreVein(Block start, Set<Block> collected, Material targetType, Location playerLoc) {
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Block current = queue.poll();
            collected.add(current);

            for (Block neighbor : getNeighbors(current)) {
                if (visited.contains(neighbor)) continue;
                visited.add(neighbor);

                if (!withinRadius(neighbor.getLocation(), playerLoc)) continue;
                if (neighbor.getType() != targetType) continue;
                if (BLACKLIST.contains(neighbor.getType())) continue;

                queue.add(neighbor);
            }
        }
    }

    private void collectTree(Block start, Set<Block> collected, Material targetType, Location playerLoc) {
        Queue<Block> logQueue = new LinkedList<>();
        Set<Block> visitedLogs = new HashSet<>();
        logQueue.add(start);
        visitedLogs.add(start);

        while (!logQueue.isEmpty()) {
            Block current = logQueue.poll();
            collected.add(current);

            for (Block neighbor : getNeighbors(current)) {
                if (visitedLogs.contains(neighbor)) continue;
                if (!withinRadius(neighbor.getLocation(), playerLoc)) continue;
                if (!isLog(neighbor.getType()) && neighbor.getType() != targetType) continue;
                if (BLACKLIST.contains(neighbor.getType())) continue;

                visitedLogs.add(neighbor);
                logQueue.add(neighbor);
            }
        }

        for (Block log : visitedLogs) {
            for (Block neighbor : getNeighbors(log)) {
                if (visitedLogs.contains(neighbor)) continue;
                if (!withinRadius(neighbor.getLocation(), playerLoc)) continue;
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

    private boolean withinRadius(Location blockLoc, Location playerLoc) {
        if (!blockLoc.getWorld().equals(playerLoc.getWorld())) return false;
        double dx = blockLoc.getX() - playerLoc.getX();
        double dy = blockLoc.getY() - playerLoc.getY();
        double dz = blockLoc.getZ() - playerLoc.getZ();
        return Math.sqrt(dx*dx + dy*dy + dz*dz) <= RADIUS;
    }

    private boolean isLog(Material m) {
        return m.name().endsWith("_LOG") || m.name().endsWith("_STEM") || m == Material.CRIMSON_STEM || m == Material.WARPED_STEM;
    }

    private boolean isLeaves(Material m) {
        return m.name().endsWith("_LEAVES");
    }
}
