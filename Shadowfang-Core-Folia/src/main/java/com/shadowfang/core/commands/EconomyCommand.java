package com.shadowfang.core.commands;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.shadowfang.core.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EconomyCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        EconomyManager eco = ShadowfangCorePlugin.getInstance().getEconomyManager();

        if (args.length == 0) {
            player.sendMessage("§7You have §f" + String.format("%.0f", eco.getBalance(player.getUniqueId())) + " §7Silver Coins.");
            return true;
        }

        if (args[0].equalsIgnoreCase("pay") && args.length == 3) {
            String targetName = args[1];
            double amount;
            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid amount.");
                return true;
            }

            if (amount <= 0) {
                player.sendMessage("§cAmount must be greater than zero.");
                return true;
            }

            // Using getPlayer for simple lookup in Folia context without async queries
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                player.sendMessage("§cPlayer not found online.");
                return true;
            }

            if (eco.removeBalance(player.getUniqueId(), amount)) {
                eco.addBalance(target.getUniqueId(), amount);
                player.sendMessage("§7You paid §f" + target.getName() + " §7" + amount + " Silver Coins.");
                target.sendMessage("§7You received §f" + amount + " Silver Coins §7from §f" + player.getName() + ".");
            } else {
                player.sendMessage("§cYou do not have enough Silver Coins.");
            }
            return true;
        }

        // Admin commands
        if (player.hasPermission("shadowfang.admin")) {
            if (args[0].equalsIgnoreCase("give") && args.length == 3) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) return true;
                double amt = Double.parseDouble(args[2]);
                eco.addBalance(target.getUniqueId(), amt);
                player.sendMessage("§aGave " + target.getName() + " " + amt + " Silver Coins.");
                return true;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(List.of("balance", "pay"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("pay")) {
            for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        }
        String partial = args[args.length - 1].toLowerCase();
        completions.removeIf(c -> !c.toLowerCase().startsWith(partial));
        return completions;
    }
}
