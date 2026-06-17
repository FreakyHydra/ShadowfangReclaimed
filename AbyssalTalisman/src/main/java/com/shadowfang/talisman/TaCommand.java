package com.shadowfang.talisman;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TaCommand implements CommandExecutor, TabCompleter {

    private final AbyssalTalismanPlugin plugin;

    public TaCommand(AbyssalTalismanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPlayers only.");
            return true;
        }

        if (args.length == 0) {
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (TalismanItem.isTalisman(offHand)) {
                TalismanMode current = TalismanItem.getMode(offHand);
                TalismanMode next = current.next();
                TalismanItem.setMode(offHand, next);
                TalismanItem.refreshLore(offHand);
                String[] powerNames = TalismanItem.getPowerNames(TalismanItem.getType(offHand));
                player.sendMessage("§6Mode: §e" + powerNames[next.index]);
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.2f);
            } else {
                plugin.getVaultListener().openVault(player);
            }
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "v", "vault" -> plugin.getVaultListener().openVault(player);
            case "bind" -> plugin.getTalismanCommand().onCommand(sender, command, label, new String[]{"bind"});
            case "wand" -> plugin.getTalismanCommand().onCommand(sender, command, label, new String[]{"wand"});
            default -> player.sendMessage("§cUsage: /ta [v|vault|bind|wand]");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("v");
            completions.add("vault");
            completions.add("bind");
            completions.add("wand");
        }
        String prefix = args[args.length - 1].toLowerCase();
        return completions.stream().filter(s -> s.startsWith(prefix)).toList();
    }
}
