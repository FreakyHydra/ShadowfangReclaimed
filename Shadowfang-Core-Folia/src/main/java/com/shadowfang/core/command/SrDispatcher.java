package com.shadowfang.core.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class SrDispatcher implements CommandExecutor, TabCompleter {

    private static final String[] NO_DEFAULTS = new String[0];

    private record Entry(CommandExecutor executor, TabCompleter completer, String[] defaultArgs, String permission) {}

    private final Map<String, Entry> registry = new LinkedHashMap<>();

    public void register(CommandExecutor executor, String... keys) {
        register(executor, null, NO_DEFAULTS, keys);
    }

    public void registerDef(CommandExecutor executor, String[] defaultArgs, String... keys) {
        register(executor, null, defaultArgs, keys);
    }

    public void registerPerm(CommandExecutor executor, String permission, String... keys) {
        register(executor, permission, NO_DEFAULTS, keys);
    }

    public void registerPermDef(CommandExecutor executor, String permission, String[] defaultArgs, String... keys) {
        register(executor, permission, defaultArgs, keys);
    }

    private void register(CommandExecutor executor, String permission, String[] defaultArgs, String... keys) {
        TabCompleter completer = executor instanceof TabCompleter t ? t : null;
        Entry entry = new Entry(executor, completer, defaultArgs, permission);
        for (String key : keys) {
            registry.put(key.toLowerCase(), entry);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String key = args[0].toLowerCase();
        Entry entry = registry.get(key);
        if (entry == null) {
            player.sendMessage("§cUnknown sub-plugin: " + key);
            showHelp(player);
            return true;
        }

        if (entry.permission != null && !player.hasPermission(entry.permission)) {
            player.sendMessage("§cYou don't have permission for that.");
            return true;
        }

        String[] subArgs;
        if (entry.defaultArgs.length > 0 && args.length == 1) {
            subArgs = entry.defaultArgs;
        } else {
            subArgs = Arrays.copyOfRange(args, 1, args.length);
        }

        return entry.executor.onCommand(sender, command, label, subArgs);
    }

    private void showHelp(Player player) {
        player.sendMessage("§6--- Shadowfang Reclaimed (/sr) ---");
        for (var e : registry.entrySet()) {
            if (e.getValue().permission != null && !player.hasPermission(e.getValue().permission)) continue;
            player.sendMessage("§e/sr " + e.getKey() + " §7<subcommand>");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return registry.entrySet().stream()
                    .filter(e -> e.getValue().permission == null || sender.hasPermission(e.getValue().permission))
                    .map(Map.Entry::getKey)
                    .filter(k -> k.startsWith(prefix))
                    .toList();
        }

        if (args.length > 1) {
            String key = args[0].toLowerCase();
            Entry entry = registry.get(key);
            if (entry == null) return List.of();
            if (entry.permission != null && !sender.hasPermission(entry.permission)) return List.of();
            if (entry.completer != null) {
                return entry.completer.onTabComplete(sender, command, alias,
                        Arrays.copyOfRange(args, 1, args.length));
            }
        }

        return List.of();
    }
}
