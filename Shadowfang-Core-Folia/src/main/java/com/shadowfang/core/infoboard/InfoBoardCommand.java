package com.shadowfang.core.infoboard;

import com.shadowfang.core.ShadowfangCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InfoBoardCommand implements CommandExecutor, TabCompleter {

    private final ShadowfangCorePlugin plugin;
    private final InfoBoardManager manager;

    public InfoBoardCommand(ShadowfangCorePlugin plugin, InfoBoardManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender);
            case "program" -> handleProgram(sender, args);
            case "move" -> handleMove(sender, args);
            case "programs" -> handlePrograms(sender);
            case "add" -> handleAddProgram(sender, args);
            case "rotate" -> handleRotate(sender, args);
            case "tilt" -> handleTilt(sender, args);
            case "nudge" -> handleNudge(sender, args);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("§6§lINFO BOARD COMMANDS"));
        sender.sendMessage(Component.text("§7/board create <id> §f- Create board ahead of you"));
        sender.sendMessage(Component.text("§7/board remove <id> §f- Remove a board"));
        sender.sendMessage(Component.text("§7/board list §f- List all boards"));
        sender.sendMessage(Component.text("§7/board add <id> <program> §f- Add program to board"));
        sender.sendMessage(Component.text("§7/board remove <id> <program> §f- Remove program from board"));
        sender.sendMessage(Component.text("§7/board move <id> §f- Move board to your location"));
        sender.sendMessage(Component.text("§7/board rotate <id> <0|90|180|270> §f- Set horizontal angle"));
        sender.sendMessage(Component.text("§7/board tilt <id> <degrees> §f- Set vertical angle (-90 to 90)"));
        sender.sendMessage(Component.text("§7/board nudge <id> <fwd|back|left|right|up|down> <blocks> §f- Move"));
        sender.sendMessage(Component.text("§7/board programs §f- List available programs"));
    }

    private double[] forwardOffset(float yaw, double dist) {
        double rad = Math.toRadians(yaw);
        return new double[]{-Math.sin(rad) * dist, Math.cos(rad) * dist};
    }

    private float snapYaw(float yaw) {
        return Math.round(yaw / 90f) * 90f;
    }

    private Location boardPlacement(Player player) {
        float yaw = snapYaw(player.getYaw());
        double[] off = forwardOffset(yaw, 2.0);
        Location loc = player.getLocation().add(off[0], 1.55, off[1]);
        loc.setYaw(yaw + 180f);
        loc.setPitch(0f);
        return loc;
    }

    private void respawnBoard(InfoBoard board) {
        manager.getRenderer().removeEntities(board);
        manager.getRenderer().spawnEntities(board);
        manager.save();
    }

    // --- CREATE ---

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("§cPlayer only"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("§cUsage: /board create <id>"));
            return;
        }
        String id = args[1];
        Location loc = boardPlacement(player);
        InfoBoard board = manager.createBoard(id, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        if (board == null) {
            sender.sendMessage(Component.text("§cA board with id '" + id + "' already exists"));
        } else {
            sender.sendMessage(Component.text("§aBoard '" + id + "' created"));
        }
    }

    // --- REMOVE ---

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("§cUsage: /board remove <id>"));
            return;
        }
        if (manager.removeBoard(args[1])) {
            sender.sendMessage(Component.text("§aBoard '" + args[1] + "' removed"));
        } else {
            sender.sendMessage(Component.text("§cBoard '" + args[1] + "' not found"));
        }
    }

    // --- LIST ---

    private void handleList(CommandSender sender) {
        var boards = manager.getAllBoards();
        if (boards.isEmpty()) {
            sender.sendMessage(Component.text("§7No boards"));
            return;
        }
        sender.sendMessage(Component.text("§6§lINFO BOARDS (" + boards.size() + ")"));
        for (InfoBoard b : boards) {
            String programs = String.join(", ", b.getProgramNames());
            sender.sendMessage(Component.text("§b" + b.getId() + " §7- " + b.getWorld() + " @ " +
                    String.format("%.1f", b.getX()) + ", " + String.format("%.1f", b.getY()) + ", " + String.format("%.1f", b.getZ()) +
                    " yaw:" + String.format("%.0f", b.getYaw()) + " pitch:" + String.format("%.0f", b.getPitch()) +
                    " §8[" + (programs.isEmpty() ? "none" : programs) + "]"));
        }
    }

    // --- PROGRAM ---

    private void handleProgram(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("§cUsage: /board program <id> <program>"));
            return;
        }
        String id = args[1];
        InfoBoard board = manager.getBoard(id);
        if (board == null) {
            sender.sendMessage(Component.text("§cBoard '" + id + "' not found"));
            return;
        }
        if (args.length < 3) {
            String programs = String.join(", ", board.getProgramNames());
            sender.sendMessage(Component.text("§ePrograms on '" + id + "': §f" + (programs.isEmpty() ? "none" : programs)));
            return;
        }

        if (args[2].equalsIgnoreCase("add")) {
            if (args.length < 4) {
                sender.sendMessage(Component.text("§cUsage: /board program add <id> <program>"));
                return;
            }
            if (manager.addProgramToBoard(id, args[3])) {
                sender.sendMessage(Component.text("§aProgram '" + args[3] + "' added to '" + id + "'"));
            } else {
                sender.sendMessage(Component.text("§cFailed to add program"));
            }
        } else if (args[2].equalsIgnoreCase("remove")) {
            if (args.length < 4) {
                sender.sendMessage(Component.text("§cUsage: /board program remove <id> <program>"));
                return;
            }
            if (manager.removeProgramFromBoard(id, args[3])) {
                sender.sendMessage(Component.text("§aProgram '" + args[3] + "' removed from '" + id + "'"));
            } else {
                sender.sendMessage(Component.text("§cProgram not found on board"));
            }
        } else {
            sender.sendMessage(Component.text("§cUsage: /board program <id> add|remove <program>"));
        }
    }

    // --- ADD PROGRAM ---

    private void handleAddProgram(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("§cUsage: /board add <id> <program>"));
            return;
        }
        if (manager.addProgramToBoard(args[1], args[2])) {
            sender.sendMessage(Component.text("§aProgram '" + args[2] + "' added to '" + args[1] + "'"));
        } else {
            sender.sendMessage(Component.text("§cBoard '" + args[1] + "' not found"));
        }
    }

    // --- MOVE ---

    private void handleMove(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("§cPlayer only"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("§cUsage: /board move <id>"));
            return;
        }
        InfoBoard board = manager.getBoard(args[1]);
        if (board == null) {
            sender.sendMessage(Component.text("§cBoard '" + args[1] + "' not found"));
            return;
        }
        Location loc = boardPlacement(player);
        manager.getRenderer().removeEntities(board);
        board.setWorld(loc.getWorld().getName());
        board.setX(loc.getX());
        board.setY(loc.getY());
        board.setZ(loc.getZ());
        board.setYaw(loc.getYaw());
        board.setPitch(loc.getPitch());
        manager.getRenderer().spawnEntities(board);
        manager.save();
        sender.sendMessage(Component.text("§aBoard '" + args[1] + "' moved"));
    }

    // --- ROTATE ---

    private void handleRotate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("§cUsage: /board rotate <id> <0|90|180|270>"));
            return;
        }
        InfoBoard board = manager.getBoard(args[1]);
        if (board == null) {
            sender.sendMessage(Component.text("§cBoard '" + args[1] + "' not found"));
            return;
        }
        float yaw;
        try {
            yaw = Float.parseFloat(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("§cInvalid angle: " + args[2]));
            return;
        }
        board.setYaw(yaw);
        respawnBoard(board);
        sender.sendMessage(Component.text("§aBoard '" + args[1] + "' rotated to " + yaw + "°"));
    }

    // --- TILT ---

    private void handleTilt(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("§cUsage: /board tilt <id> <degrees>"));
            return;
        }
        InfoBoard board = manager.getBoard(args[1]);
        if (board == null) {
            sender.sendMessage(Component.text("§cBoard '" + args[1] + "' not found"));
            return;
        }
        float pitch;
        try {
            pitch = Float.parseFloat(args[2]);
            if (pitch < -90 || pitch > 90) {
                sender.sendMessage(Component.text("§cPitch must be between -90 and 90"));
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("§cInvalid pitch: " + args[2]));
            return;
        }
        board.setPitch(pitch);
        respawnBoard(board);
        sender.sendMessage(Component.text("§aBoard '" + args[1] + "' tilted to " + pitch + "°"));
    }

    // --- NUDGE ---

    private void handleNudge(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("§cUsage: /board nudge <id> <fwd|back|left|right|up|down> <blocks>"));
            return;
        }
        InfoBoard board = manager.getBoard(args[1]);
        if (board == null) {
            sender.sendMessage(Component.text("§cBoard '" + args[1] + "' not found"));
            return;
        }
        double dist;
        try {
            dist = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("§cInvalid distance: " + args[3]));
            return;
        }

        double dx = 0, dy = 0, dz = 0;
        double rad = Math.toRadians(board.getYaw());
        switch (args[2].toLowerCase()) {
            case "forward", "fwd" -> {
                dx = -Math.sin(rad) * dist;
                dz = Math.cos(rad) * dist;
            }
            case "back", "backward" -> {
                dx = Math.sin(rad) * dist;
                dz = -Math.cos(rad) * dist;
            }
            case "left" -> {
                dx = -Math.cos(rad) * dist;
                dz = -Math.sin(rad) * dist;
            }
            case "right" -> {
                dx = Math.cos(rad) * dist;
                dz = Math.sin(rad) * dist;
            }
            case "up" -> dy = dist;
            case "down" -> dy = -dist;
            default -> {
                sender.sendMessage(Component.text("§cInvalid direction. Use: forward, back, left, right, up, down"));
                return;
            }
        }

        manager.getRenderer().removeEntities(board);
        board.setX(board.getX() + dx);
        board.setY(board.getY() + dy);
        board.setZ(board.getZ() + dz);
        manager.getRenderer().spawnEntities(board);
        manager.save();
        sender.sendMessage(Component.text("§aBoard '" + args[1] + "' nudged"));
    }

    // --- PROGRAMS LIST ---

    private void handlePrograms(CommandSender sender) {
        List<String> names = manager.getAllProgramNames();
        if (names.isEmpty()) {
            sender.sendMessage(Component.text("§7No programs available"));
            return;
        }
        sender.sendMessage(Component.text("§6§lAVAILABLE PROGRAMS"));
        for (String name : names) {
            sender.sendMessage(Component.text(" §b- " + name));
        }
    }

    // --- TAB COMPLETER ---

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(args[0], Arrays.asList("create", "remove", "list", "program", "move", "programs", "add", "rotate", "tilt", "nudge"));
        }
        if (args.length == 2) {
            if (List.of("remove", "move", "program", "add", "image", "rotate", "tilt", "nudge").contains(args[0].toLowerCase())) {
                return filter(args[1], manager.getAllBoards().stream().map(InfoBoard::getId).collect(Collectors.toList()));
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add")) {
                return filter(args[2], manager.getAllProgramNames());
            }
            if (args[0].equalsIgnoreCase("program")) {
                return filter(args[2], Arrays.asList("add", "remove"));
            }
            if (args[0].equalsIgnoreCase("rotate")) {
                return filter(args[2], Arrays.asList("0", "90", "180", "270"));
            }
            if (args[0].equalsIgnoreCase("nudge")) {
                return filter(args[2], Arrays.asList("forward", "back", "left", "right", "up", "down"));
            }
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("program")) {
            return filter(args[3], manager.getAllProgramNames());
        }
        return null;
    }

    private List<String> filter(String prefix, List<String> options) {
        String lower = prefix.toLowerCase();
        return options.stream().filter(s -> s.toLowerCase().startsWith(lower)).collect(Collectors.toList());
    }
}
