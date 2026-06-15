package com.shadowfang.core.events;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.shadowfang.core.faction.Faction;
import com.shadowfang.core.faction.FactionManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.block.Action;

public class LoreConsumeListener implements Listener {

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            NamespacedKey isLoreKey = new NamespacedKey(ShadowfangCorePlugin.getInstance(), "isLoreFragment");
            
            if (meta.getPersistentDataContainer().has(isLoreKey, PersistentDataType.BYTE)) {
                event.setCancelled(true); // Don't actually "read" the book or open the UI
                
                NamespacedKey fragmentKey = new NamespacedKey(ShadowfangCorePlugin.getInstance(), "loreFragment");
                Integer fragmentId = meta.getPersistentDataContainer().get(fragmentKey, PersistentDataType.INTEGER);
                
                if (fragmentId == null) return;
                
                FactionManager manager = ShadowfangCorePlugin.getInstance().getFactionManager();
                Faction faction = manager.getPlayerFaction(player.getUniqueId());
                
                if (faction == null) {
                    player.sendMessage("§cYou must be in a Howling Faction to absorb this knowledge.");
                    return;
                }
                
                if (faction.hasUnlockedLore(fragmentId)) {
                    player.sendMessage("§cYour Faction has already absorbed this Fragment.");
                    return;
                }
                
                // Absorb the fragment
                faction.unlockLore(fragmentId);
                item.setAmount(item.getAmount() - 1); // Consume the item
                
                player.sendMessage("§aYou have absorbed Fragment " + fragmentId + " into your Faction's Hoard!");
            }
        }
    }
}
