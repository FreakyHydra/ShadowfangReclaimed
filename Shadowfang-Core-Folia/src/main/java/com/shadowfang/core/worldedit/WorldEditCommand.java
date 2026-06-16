package com.shadowfang.core.worldedit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class WorldEditCommand implements CommandExecutor, TabCompleter {

    private final WorldEditManager manager;

    public WorldEditCommand(WorldEditManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        return switch (sub) {
            case "wand" -> handleWand(player);
            case "pos1", "1" -> handlePos1(player, args);
            case "pos2", "2" -> handlePos2(player, args);
            case "copy" -> handleCopy(player);
            case "paste" -> handlePaste(player);
            case "start" -> handleStart(player);
            case "stop" -> handleStop(player);
            case "undo" -> handleUndo(player);
            case "clear" -> handleClear(player, args);
            default -> {
                sendHelp(player);
                yield true;
            }
        };
    }

    private boolean handleWand(Player player) {
        player.getInventory().addItem(WeTool.createWand(manager.plugin()));
        player.sendMessage("§aPath wand given. Left-click = pos1, Right-click = pos2.");
        return true;
    }

    private boolean handlePos1(Player player, String[] args) {
        manager.setPos1(player, player.getLocation());
        return true;
    }

    private boolean handlePos2(Player player, String[] args) {
        manager.setPos2(player, player.getLocation());
        return true;
    }

    private boolean handleCopy(Player player) {
        manager.copy(player);
        return true;
    }

    private boolean handlePaste(Player player) {
        manager.paste(player, null);
        return true;
    }

    private boolean handleStart(Player player) {
        if (!manager.isWalkPasteActive(player)) {
            if (manager.getClipboard(player) == null) {
                player.sendMessage("§cNo pattern. Use /we copy first.");
                return true;
            }
        }
        manager.toggleWalkPaste(player);
        return true;
    }

    private boolean handleStop(Player player) {
        if (manager.isWalkPasteActive(player)) {
            manager.toggleWalkPaste(player);
        } else {
            player.sendMessage("§7Walk-paste is not active.");
        }
        return true;
    }

    private boolean handleUndo(Player player) {
        UndoTracker tracker = manager.getUndoTracker();
        if (!tracker.hasUndo(player.getUniqueId())) {
            player.sendMessage("§cNothing to undo.");
            return true;
        }

        List<UndoTracker.BlockSnapshot> snaps = tracker.pop(player.getUniqueId());
        if (snaps.isEmpty()) {
            player.sendMessage("§cNothing to undo.");
            return true;
        }

        player.sendMessage("§7Undoing " + snaps.size() + " blocks...");
        manager.getPathBuilder().restoreBlocks(snaps, () -> {
            player.sendMessage("§aUndo complete. §7" + snaps.size() + " blocks restored.");
        });
        return true;
    }

    private boolean handleClear(Player player, String[] args) {
        manager.clearPlayer(player);
        player.sendMessage("§aSelection, clipboard, and walk-mode cleared.");
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6--- Path Tools (/we) ---");
        player.sendMessage("§e/we wand §7- Get the path wand");
        player.sendMessage("§e/we pos1 §7- Set position 1 (or left-click wand)");
        player.sendMessage("§e/we pos2 §7- Set position 2 (or right-click wand)");
        player.sendMessage("§e/we copy §7- Capture pattern from selection");
        player.sendMessage("§e/we paste §7- Paste pattern at your location");
        player.sendMessage("§e/we start §7- Toggle walk-paste mode");
        player.sendMessage("§e/we stop §7- Exit walk-paste mode");
        player.sendMessage("§e/we undo §7- Revert last paste");
        player.sendMessage("§e/we clear §7- Clear your selection and clipboard");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Stream.of("wand", "pos1", "pos2", "copy", "paste", "start", "stop", "undo", "clear", "1", "2")
                    .filter(s -> s.startsWith(prefix))
                    .toList();
        }
        return List.of();
    }
}
