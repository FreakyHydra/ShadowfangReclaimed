package com.shadowfang.core.veinmining;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VeinMineCommand implements CommandExecutor, TabCompleter {

    private final VeinMineManager manager;

    public VeinMineCommand(VeinMineManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("shadowfang.admin")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cMust be a player.");
            return true;
        }

        if (args.length == 0) {
            boolean enabled = manager.isEnabled(player);
            player.sendMessage("§6§lVeinmining §7— " + (enabled ? "§aENABLED" : "§cDISABLED") + " §7| Break a block to mine its vein (30 block radius)");
            player.sendMessage("§7Type §e/sr veinmine toggle §7to " + (enabled ? "disable" : "enable") + ".");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "toggle" -> manager.toggle(player);
            case "on" -> {
                if (!manager.isEnabled(player)) manager.toggle(player);
                else player.sendMessage("§aVeinmining is already enabled.");
            }
            case "off" -> {
                if (manager.isEnabled(player)) manager.toggle(player);
                else player.sendMessage("§cVeinmining is already disabled.");
            }
            case "status" -> {
                boolean enabled = manager.isEnabled(player);
                player.sendMessage("§6Veinmining: " + (enabled ? "§aEnabled" : "§cDisabled"));
            }
            default -> player.sendMessage("§cUsage: /sr veinmine [toggle|on|off|status]");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("toggle");
            completions.add("on");
            completions.add("off");
            completions.add("status");
        }
        String prefix = args[args.length - 1].toLowerCase();
        return completions.stream().filter(s -> s.startsWith(prefix)).toList();
    }
}
