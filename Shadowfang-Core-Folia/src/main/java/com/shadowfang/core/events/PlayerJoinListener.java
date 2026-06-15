package com.shadowfang.core.events;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.shadowfang.core.commands.FactionCommand;
import com.shadowfang.core.faction.Faction;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Faction faction = ShadowfangCorePlugin.getInstance().getFactionManager().getPlayerFaction(player.getUniqueId());
        if (faction != null) {
            player.discoverRecipe(FactionCommand.BELL_RECIPE_KEY);
        }
        handleBitterrootEntry(player, true);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        handleBitterrootEntry(event.getPlayer(), false);
    }

    private void handleBitterrootEntry(Player player, boolean isLogin) {
        String currentDimension = player.getWorld().getName();
        
        // Check if world is bitterroot
        if (currentDimension.contains("bitterroot")) {
            
            if (isLogin) {
                player.sendMessage("§8§oA cold wind bites at your heels... The Archive stirs as you return to Bitterroot.");
            } else {
                player.sendMessage("§8§oThe wind howls through the Whispering Woods. You have entered Bitterroot.");
            }

            NamespacedKey welcomeKey = new NamespacedKey(ShadowfangCorePlugin.getInstance(), "welcomed_bitterroot");
            
            // Give the Welcome Book if they haven't received it yet
            if (!player.getPersistentDataContainer().has(welcomeKey, PersistentDataType.BYTE)) {
                player.getPersistentDataContainer().set(welcomeKey, PersistentDataType.BYTE, (byte) 1);
                
                ItemStack welcomeBook = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta meta = (BookMeta) welcomeBook.getItemMeta();
                if (meta != null) {
                    meta.setTitle("A Warning");
                    meta.setAuthor("The Warden");
                    
                    List<String> pages = new ArrayList<>();
                    pages.add("Welcome to Bitterroot.\n\nThis is not a gentle place. The strong feast, and the weak are devoured. The forests remember every drop of blood spilled beneath their boughs, and the mountains do not forgive.");
                    pages.add("You cannot survive alone. You must forge or join a Howling Faction.\n\nUse /faction create to claim your Hunting Grounds and gather your Pack. Only a Pack can protect what is yours.");
                    pages.add("The history of this world is shattered.\n\nAs you hunt, you may find Lost Fragments. If you are in a Faction, right-click to absorb these fragments. Rebuild the Archive for your Hoard, and uncover the secrets of the Howling Hills.");
                    pages.add("The Warden is watching. The Howling Hills sing with voices that are not wind.\n\nKarma is patient here. Good luck. You will need it.");
                    
                    meta.setPages(pages);
                    welcomeBook.setItemMeta(meta);
                }
                
                player.getInventory().addItem(welcomeBook);
            }
        }
    }
}
