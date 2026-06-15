package com.shadowfang.core.events;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.shadowfang.core.lore.LoreManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class LoreDropListener implements Listener {

    private static final Random RANDOM = new Random();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            
            // 1% chance to drop a lore fragment
            if (RANDOM.nextInt(100) == 0) {
                LoreManager manager = ShadowfangCorePlugin.getInstance().getLoreManager();
                int totalFragments = manager.getTotalFragments();
                
                if (totalFragments > 0) {
                    int fragToDrop = RANDOM.nextInt(totalFragments) + 1;
                    ItemStack book = manager.createLoreFragmentItem(fragToDrop);
                    
                    if (book != null) {
                        event.getDrops().add(book);
                        ShadowfangCorePlugin.getInstance().getLogger().info("Dropped Lore Fragment " + fragToDrop + " for " + killer.getName());
                    }
                }
            }
        }
    }
}
