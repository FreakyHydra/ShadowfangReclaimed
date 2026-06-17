package com.shadowfang.talisman;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class VeinMineListener implements Listener {

    private final AbyssalTalismanPlugin plugin;

    public VeinMineListener(AbyssalTalismanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockMine(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (!TalismanItem.isTalisman(offHand)) return;
        if (TalismanItem.getType(offHand) != TalismanType.ABYSSAL) return;

        TalismanMode mode = TalismanItem.getMode(offHand);
        boolean cursed = TalismanItem.isCursed(offHand);
        ItemStack tool = player.getInventory().getItemInMainHand();
        Block targetBlock = event.getBlock();

        plugin.getVeinMineManager().onBlockBreak(player, targetBlock, tool, TalismanType.ABYSSAL, mode, cursed);

        event.setCancelled(true);

        if (tool != null && tool.getType().getMaxDurability() > 0) {
            short durability = tool.getDurability();
            if (durability < tool.getType().getMaxDurability() - 1) {
                tool.setDurability((short) (durability + 1));
            }
        }

        List<ItemStack> drops = new ArrayList<>(targetBlock.getDrops(tool));
        targetBlock.setType(Material.AIR);
        for (ItemStack drop : drops) {
            targetBlock.getWorld().dropItemNaturally(targetBlock.getLocation().add(0.5, 0.5, 0.5), drop);
        }
    }
}
