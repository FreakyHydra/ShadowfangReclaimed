package com.shadowfang.core.elevator;

import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ElevatorCommand implements CommandExecutor, TabCompleter {

    private final ElevatorManager manager;
    private final ShadowfangCorePlugin plugin;

    public ElevatorCommand(ElevatorManager manager, ShadowfangCorePlugin plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create" -> {
                if (!sender.hasPermission("shadowfang.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cMust be a player.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /sr elevator create <name>");
                    return true;
                }
                String name = args[1];
                if (manager.createGroup(name)) {
                    giveWand(player, name);
                    sender.sendMessage("§aElevator group §e" + name + " §acreated. Wand given. Right-click blocks to add floors.");
                } else {
                    sender.sendMessage("§cGroup §e" + name + " §calready exists.");
                }
            }

            case "assign" -> {
                if (!sender.hasPermission("shadowfang.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cMust be a player.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /sr elevator assign <name>");
                    return true;
                }
                String name = args[1];
                if (manager.getGroup(name) == null) {
                    sender.sendMessage("§cGroup §e" + name + " §cdoes not exist.");
                    return true;
                }
                giveWand(player, name);
                sender.sendMessage("§aWand given for group §e" + name + "§a. Right-click blocks to add floors, left-click to remove.");
            }

            case "remove" -> {
                if (!sender.hasPermission("shadowfang.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /sr elevator remove <name>");
                    return true;
                }
                String name = args[1];
                if (manager.removeGroup(name)) {
                    sender.sendMessage("§aGroup §e" + name + " §aremoved.");
                } else {
                    sender.sendMessage("§cGroup §e" + name + " §cdoes not exist.");
                }
            }

            case "list" -> {
                if (!sender.hasPermission("shadowfang.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                var groups = manager.getAllGroups();
                if (groups.isEmpty()) {
                    sender.sendMessage("§7No elevator groups defined.");
                    return true;
                }
                sender.sendMessage("§6§lElevator Groups §7(" + groups.size() + "):");
                for (ElevatorGroup group : groups) {
                    sender.sendMessage(" §e" + group.getName() + " §7- §f" + group.getFloorCount() + " floors");
                }
            }

            case "info" -> {
                if (!sender.hasPermission("shadowfang.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /sr elevator info <name>");
                    return true;
                }
                ElevatorGroup group = manager.getGroup(args[1]);
                if (group == null) {
                    sender.sendMessage("§cGroup §e" + args[1] + " §cdoes not exist.");
                    return true;
                }
                sender.sendMessage("§6§lGroup: §e" + group.getName() + " §7(" + group.getFloorCount() + " floors)");
                for (int i = 0; i < group.getFloorCount(); i++) {
                    var floor = group.getFloor(i);
                    String name = floor.getDisplayName();
                    if (name == null || name.isEmpty()) name = "Floor " + (i + 1);
                    sender.sendMessage(" §e" + (i + 1) + ". §f" + name + " §7(" + floor.getWorld() + ": " + floor.getX() + "," + floor.getY() + "," + floor.getZ() + ")");
                }
            }

            case "delfloor" -> {
                if (!sender.hasPermission("shadowfang.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /sr elevator delfloor <name> <floor_number>");
                    return true;
                }
                ElevatorGroup group = manager.getGroup(args[1]);
                if (group == null) {
                    sender.sendMessage("§cGroup §e" + args[1] + " §cdoes not exist.");
                    return true;
                }
                int index;
                try {
                    index = Integer.parseInt(args[2]) - 1;
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid floor number.");
                    return true;
                }
                if (manager.removeFloor(args[1], index)) {
                    sender.sendMessage("§aFloor removed.");
                } else {
                    sender.sendMessage("§cInvalid floor number.");
                }
            }

            case "namefloor" -> {
                if (!sender.hasPermission("shadowfang.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage("§cUsage: /sr elevator namefloor <name> <floor_number> <display_name>");
                    return true;
                }
                ElevatorGroup eg = manager.getGroup(args[1]);
                if (eg == null) {
                    sender.sendMessage("§cGroup §e" + args[1] + " §cdoes not exist.");
                    return true;
                }
                int idx;
                try {
                    idx = Integer.parseInt(args[2]) - 1;
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid floor number.");
                    return true;
                }
                String newName = args[3];
                if (args.length > 4) {
                    newName = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                }
                if (manager.renameFloor(args[1], idx, newName)) {
                    sender.sendMessage("§aFloor §e" + args[2] + " §arenamed to §e\"" + newName + "\"§a.");
                } else {
                    sender.sendMessage("§cInvalid floor number.");
                }
            }

            case "go" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cMust be a player.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /elevator go <floor_number>");
                    return true;
                }
                int num;
                try {
                    num = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid floor number.");
                    return true;
                }
                manager.goFloor(player, num);
            }

            case "help" -> sendHelp(sender);

            default -> sendHelp(sender);
        }

        return true;
    }

    private void giveWand(Player player, String groupName) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (ElevatorWand.isWand(mainHand)) {
            ElevatorWand.updateWandGroup(mainHand, groupName);
            return;
        }
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (ElevatorWand.isWand(offHand)) {
            ElevatorWand.updateWandGroup(offHand, groupName);
            return;
        }
        player.getInventory().addItem(ElevatorWand.createWand(groupName));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lTeleport/Elevator Commands:");
        sender.sendMessage("§e/sr elevator create <name> §7- Create group, get wand");
        sender.sendMessage("§e/sr elevator assign <name> §7- Get wand for existing group");
        sender.sendMessage("§e/sr elevator remove <name> §7- Delete entire group");
        sender.sendMessage("§e/sr elevator list §7- List all groups");
        sender.sendMessage("§e/sr elevator info <name> §7- Show floors in group");
        sender.sendMessage("§e/sr elevator delfloor <name> <#> §7- Remove floor by number");
        sender.sendMessage("§e/sr elevator namefloor <name> <#> <display> §7- Rename a floor");
        sender.sendMessage("");
        sender.sendMessage("§7Wand: §fRight-click §7= add floor (type name in chat), §fLeft-click §7= remove floor");
        sender.sendMessage("§7Teleporter: §fSneak §7on a floor pad to activate");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "assign", "remove", "list", "info", "delfloor", "namefloor", "go", "help"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("assign") || sub.equals("remove") || sub.equals("info") || sub.equals("delfloor") || sub.equals("namefloor")) {
                for (ElevatorGroup g : manager.getAllGroups()) {
                    completions.add(g.getName());
                }
            }
        }

        String prefix = args[args.length - 1].toLowerCase();
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(prefix))
            .sorted()
            .collect(Collectors.toList());
    }
}
