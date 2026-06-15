package com.shadowfang.core.commands;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.shadowfang.core.bounty.BountyManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BountyCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        BountyManager bountyManager = ShadowfangCorePlugin.getInstance().getBountyManager();
        BountyManager.BountyTask task = bountyManager.getBounty(player.getUniqueId());

        if (args.length > 0 && args[0].equalsIgnoreCase("new")) {
            if (task != null) {
                player.sendMessage("§cYou already have an active bounty: Hunt " + task.required + " " + task.target.name() + "s.");
                return true;
            }
            bountyManager.assignBounty(player.getUniqueId());
            task = bountyManager.getBounty(player.getUniqueId());
            player.sendMessage("§aNew Bounty Assigned: §7Hunt §f" + task.required + " " + task.target.name() + "s §7for §f" + task.reward + " Silver Coins.");
            return true;
        }

        if (task == null) {
            player.sendMessage("§7You have no active bounties. Use §f/bounty new §7to get one.");
        } else {
            player.sendMessage("§aActive Bounty: §7Hunt §f" + task.required + " " + task.target.name() + "s §7(Kills: " + task.currentKills + ").");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("new");
        }
        String partial = args[args.length - 1].toLowerCase();
        completions.removeIf(c -> !c.toLowerCase().startsWith(partial));
        return completions;
    }
}
