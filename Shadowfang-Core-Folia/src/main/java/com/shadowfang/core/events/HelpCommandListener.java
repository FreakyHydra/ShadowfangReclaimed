package com.shadowfang.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class HelpCommandListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        if (message.equals("/help") || message.startsWith("/help ") || message.equals("/?")) {
            event.setCancelled(true);
            Player player = event.getPlayer();

            // Send our fancy thematic help menu
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("           §4§lS H A D O W F A N G  R E C L A I M E D");
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§c/bounty §8- §7Hunt down targets for Silver Coins");
            player.sendMessage("§c/economy §8- §7Check your wealth and pay others");
            player.sendMessage("§c/faction §8- §7Manage your bloodline and claims");
            player.sendMessage("§c/lore §8- §7Delve into the forgotten archives");
            player.sendMessage("§c/warp <world> §8- §7Traverse between dimensions");
            player.sendMessage("§c/hub §8- §7Return to the void Hub");
            player.sendMessage("§c/spawn §8- §7Return to world spawn");
            
            if (player.hasPermission("shadowfang.verse.admin") || player.isOp()) {
                player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                player.sendMessage("§e§lAdmin Commands:");
                player.sendMessage("§6/sfv gen <name> <type> §8- §7Generate dimension Datapack");
                player.sendMessage("§6/sfv remove <name> §8- §7Unregister a dimension");
                player.sendMessage("§6/sfv setspawn §8- §7Set spawn for current world");
                player.sendMessage("§6/sfv sign <world> §8- §7Place a portal sign");
                player.sendMessage("§6/sfv list §8- §7List all dimensions");
            }

            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§7Type §f/bukkit:help §7for standard server commands.");
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }
    }
}
