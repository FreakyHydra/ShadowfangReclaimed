package com.shadowfang.core.commands;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.shadowfang.core.faction.Faction;
import com.shadowfang.core.faction.FactionManager;
import com.shadowfang.core.lore.LoreManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LoreCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        FactionManager manager = ShadowfangCorePlugin.getInstance().getFactionManager();
        LoreManager loreManager = ShadowfangCorePlugin.getInstance().getLoreManager();

        if (args.length > 0 && args[0].equalsIgnoreCase("spawnbook")) {
            if (!player.hasPermission("shadowfang.admin")) {
                player.sendMessage("§cYou do not have permission.");
                return true;
            }
            
            ItemStack book = loreManager.createLoreFragmentItem(1);
            player.getInventory().addItem(book);
            player.sendMessage("§aSpawned Fragment 1 of the Bitterroot Archive.");
            return true;
        }

        Faction faction = manager.getPlayerFaction(player.getUniqueId());
        if (faction == null) {
            player.sendMessage("§cYou must be in a Faction to access the Archive.");
            return true;
        }

        if (faction.getUnlockedLoreFragments().isEmpty()) {
            player.sendMessage("§cYour Faction has not deposited any Lost Fragments into the Hoard.");
            return true;
        }

        player.sendMessage("§6Unlocked Fragments for " + faction.getName() + ":");
        for (int fragmentNum : faction.getUnlockedLoreFragments()) {
            LoreManager.LoreFragment fragment = loreManager.getFragment(fragmentNum);
            if (fragment != null) {
                player.sendMessage("§e - Fragment " + fragmentNum + ": §f" + fragment.title);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(List.of("list", "read", "give"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("read")) {
            if (sender instanceof Player p) {
                Faction faction = ShadowfangCorePlugin.getInstance().getFactionManager().getPlayerFaction(p.getUniqueId());
                if (faction != null) {
                    for (int frag : faction.getUnlockedLoreFragments()) {
                        completions.add(String.valueOf(frag));
                    }
                }
            }
        }
        String partial = args[args.length - 1].toLowerCase();
        completions.removeIf(c -> !c.toLowerCase().startsWith(partial));
        return completions;
    }
}
