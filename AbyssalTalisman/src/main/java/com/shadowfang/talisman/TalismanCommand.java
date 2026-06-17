package com.shadowfang.talisman;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TalismanCommand implements CommandExecutor, TabCompleter {

    private final AbyssalTalismanPlugin plugin;

    public TalismanCommand(AbyssalTalismanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPlayers only.");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "bind" -> handleBind(player);
            case "vault", "v" -> plugin.getVaultListener().openVault(player);
            case "wand" -> handleWand(player);
            case "toggle", "on", "off", "status" -> handleToggle(player, sub);
            case "info" -> handleInfo(player);
            case "help" -> showHelp(player);
            default -> player.sendMessage("§cUnknown subcommand. Use §e/talisman help§c.");
        }

        return true;
    }

    private void handleBind(Player player) {
        if (!player.hasPermission("shadowfang.talisman.bind")) {
            player.sendMessage("§cNo permission.");
            return;
        }

        int fragmentCount = countLoreFragments(player);
        if (fragmentCount < 5) {
            player.sendMessage("§cYou need 5 lore fragments to bind a talisman. You have: " + fragmentCount);
            player.sendMessage("§7Collect Lost Fragments and hold them to prepare for binding.");
            return;
        }

        boolean cursed = Math.random() < 0.10;
        ItemStack talisman = TalismanItem.createTalisman(TalismanType.ABYSSAL, cursed);

        removeLoreFragments(player, 5);

        if (!player.getInventory().getItemInOffHand().getType().isAir()) {
            plugin.getVaultManager().addToVault(player, talisman);
            player.sendMessage("§aYour off-hand was occupied. Talisman stored in vault.");
        } else {
            player.getInventory().setItemInOffHand(talisman);
            player.sendMessage("§aYou have bound an §eAbyssal Talisman§a to your being.");
        }

        if (cursed) {
            player.sendMessage("§4§lThe fragments whispered... something darker awakened.");
        }

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WARDEN_AMBIENT, 1.0f, 0.5f);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 0.8f, 1.5f);
    }

    private void handleWand(Player player) {
        if (!player.hasPermission("shadowfang.talisman.wand")) {
            player.sendMessage("§cNo permission.");
            return;
        }

        ItemStack talisman = TalismanItem.createTalisman(TalismanType.ABYSSAL, false);
        if (!player.getInventory().getItemInOffHand().getType().isAir()) {
            plugin.getVaultManager().addToVault(player, talisman);
            player.sendMessage("§aTalisman stored in vault (off-hand occupied).");
        } else {
            player.getInventory().setItemInOffHand(talisman);
            player.sendMessage("§aAbyssal Talisman granted. Use §e/talisman vault§a to store it.");
        }
    }

    private void handleToggle(Player player, String sub) {
        if (!player.hasPermission("shadowfang.talisman")) {
            player.sendMessage("§cNo permission.");
            return;
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (!TalismanItem.isTalisman(offHand)) {
            player.sendMessage("§cYou must hold a talisman in your off-hand to use toggle.");
            return;
        }

        TalismanMode current = TalismanItem.getMode(offHand);
        TalismanMode next = current.next();
        TalismanItem.setMode(offHand, next);
        TalismanItem.refreshLore(offHand);

        String[] powerNames = TalismanItem.getPowerNames(TalismanItem.getType(offHand));
        player.sendMessage("§6Mode: §e" + powerNames[next.index]);
    }

    private void handleInfo(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (!TalismanItem.isTalisman(offHand)) {
            player.sendMessage("§cHold a talisman in your off-hand to see its info.");
            return;
        }

        TalismanType type = TalismanItem.getType(offHand);
        TalismanMode mode = TalismanItem.getMode(offHand);
        boolean cursed = TalismanItem.isCursed(offHand);
        String[] powerNames = TalismanItem.getPowerNames(type);
        String[] powerDescs = TalismanItem.getPowerDescriptions(type);

        player.sendMessage("§6§l=== Talisman Info ===");
        player.sendMessage("§eType: §f" + (cursed ? "§4§lCursed " : "") + type.displayName);
        player.sendMessage("§eCurrent Mode: §f" + powerNames[mode.index] + " §8(" + (mode.index + 1) + "/3)");
        player.sendMessage("");
        for (int i = 0; i < 3; i++) {
            String prefix = i == mode.index ? "§e▸ " : "§7  ";
            String curse = TalismanItem.getActiveCurseText(type, TalismanMode.fromIndex(i), cursed);
            player.sendMessage(prefix + powerNames[i] + " §8— " + curse);
            player.sendMessage("§8    " + powerDescs[i]);
        }
        player.sendMessage("");
        player.sendMessage("§7Sneak + Right-Click off-hand to cycle modes.");
        player.sendMessage("§7/talisman vault §8— Store in vault.");
    }

    private void showHelp(Player player) {
        player.sendMessage("§5§l=== Abyssal Talisman ===");
        player.sendMessage("§e/talisman bind §8— §7Bind a talisman using 5 lore fragments");
        player.sendMessage("§e/talisman wand §8— §7Receive an Abyssal Talisman (admin)");
        player.sendMessage("§e/talisman vault §8— §7Open your talisman vault");
        player.sendMessage("§e/talisman toggle §8— §7Cycle talisman mode");
        player.sendMessage("§e/talisman info §8— §7View current talisman details");
        player.sendMessage("§e/ta §8— §7Quick toggle mode or open vault");
        player.sendMessage("");
        player.sendMessage("§8Hold talisman in off-hand. Sneak+Right-Click to switch modes.");
        player.sendMessage("§8§oEvery gift demands a price.");
    }

    private int countLoreFragments(Player player) {
        int count = 0;
        NamespacedKey isLoreKey = new NamespacedKey("shadowfangreclaimed", "isLoreFragment");

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                BookMeta meta = (BookMeta) item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(isLoreKey, PersistentDataType.BYTE)) {
                    count++;
                }
            }
        }
        return count;
    }

    private void removeLoreFragments(Player player, int count) {
        NamespacedKey isLoreKey = new NamespacedKey("shadowfangreclaimed", "isLoreFragment");
        int removed = 0;

        for (int i = 0; i < player.getInventory().getContents().length && removed < count; i++) {
            ItemStack item = player.getInventory().getContents()[i];
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                BookMeta meta = (BookMeta) item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(isLoreKey, PersistentDataType.BYTE)) {
                    player.getInventory().setItem(i, new ItemStack(Material.AIR));
                    removed++;
                }
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("bind");
            completions.add("wand");
            completions.add("vault");
            completions.add("toggle");
            completions.add("info");
            completions.add("help");
        }
        String prefix = args[args.length - 1].toLowerCase();
        return completions.stream().filter(s -> s.startsWith(prefix)).toList();
    }
}
