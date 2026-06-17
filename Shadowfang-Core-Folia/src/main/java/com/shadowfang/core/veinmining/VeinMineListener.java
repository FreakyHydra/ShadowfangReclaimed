package com.shadowfang.core.veinmining;

import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class VeinMineListener implements Listener {

    private final VeinMineManager manager;

    public VeinMineListener(VeinMineManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockMine(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (!manager.isEnabled(event.getPlayer())) return;

        manager.onBlockBreak(event.getPlayer(), event.getBlock(), event.getPlayer().getInventory().getItemInMainHand());
    }
}
