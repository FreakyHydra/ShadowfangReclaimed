package com.shadowfang.core.commands;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.shadowfang.core.economy.EconomyManager;
import com.shadowfang.core.faction.Faction;
import com.shadowfang.core.faction.FactionManager;
import com.shadowfang.core.faction.FactionRank;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FactionCommand implements CommandExecutor, TabCompleter {

    public static final NamespacedKey BELL_RECIPE_KEY = new NamespacedKey(ShadowfangCorePlugin.getInstance(), "faction_bell");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        FactionManager manager = ShadowfangCorePlugin.getInstance().getFactionManager();

        if (args.length == 0) {
            player.sendMessage("§cUsage: /faction <create|invite|accept|deny|claim|info|setspawn|spawn|disband|deposit|withdraw|kick|promote|demote|leave>");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("create")) {
            return handleCreate(player, manager, args);
        }

        if (sub.equals("accept")) {
            return handleAccept(player, manager);
        }

        if (sub.equals("deny")) {
            return handleDeny(player, manager);
        }

        Faction faction = manager.getPlayerFaction(player.getUniqueId());

        if (sub.equals("info")) {
            return handleInfo(player, faction);
        }

        if (faction == null) {
            player.sendMessage("§cYou must be in a Faction to use this command.");
            return true;
        }

        switch (sub) {
            case "setspawn" -> { return handleSetSpawn(player, faction, manager); }
            case "spawn" -> { return handleSpawn(player, faction); }
            case "disband" -> { return handleDisband(player, faction, manager); }
            case "deposit" -> { return handleDeposit(player, faction, args); }
            case "withdraw" -> { return handleWithdraw(player, faction, args); }
            case "invite" -> { return handleInvite(player, faction, manager, args); }
            case "claim" -> { return handleClaim(player, faction, manager, args); }
            case "kick" -> { return handleKick(player, faction, manager, args); }
            case "promote" -> { return handlePromote(player, faction, manager, args); }
            case "demote" -> { return handleDemote(player, faction, manager, args); }
            case "leave" -> { return handleLeave(player, faction, manager); }
            default -> player.sendMessage("§cUnknown subcommand.");
        }
        return true;
    }

    private boolean handleCreate(Player player, FactionManager manager, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /faction create <name>");
            return true;
        }
        String name = args[1];

        if (manager.getPlayerFaction(player.getUniqueId()) != null) {
            player.sendMessage("§cYou are already in a Faction!");
            return true;
        }

        Faction faction = manager.createFaction(name, player.getUniqueId());
        if (faction == null) {
            player.sendMessage("§cA Faction with that name already exists.");
            return true;
        }

        player.discoverRecipe(BELL_RECIPE_KEY);
        player.sendMessage("§aSuccessfully created the Howling Faction: " + name);
        player.sendMessage("§eYou have unlocked the Faction Bell recipe!");
        player.sendMessage("§eCraft a Bell with 8 Gold Blocks and place it to claim territory.");
        return true;
    }

    private boolean handleInfo(Player player, Faction faction) {
        if (faction == null) {
            player.sendMessage("§cYou are not in a Faction.");
            return true;
        }

        player.sendMessage("§aFaction: §f" + faction.getName() + " §a| Hoard: §f" + String.format("%.0f", faction.getHoardBalance()) + " Silver Coins");
        Location bell = faction.getBellLocation();
        if (bell != null) {
            player.sendMessage("§aBell Location: §f" + bell.getBlockX() + ", " + bell.getBlockY() + ", " + bell.getBlockZ());
        }
        player.sendMessage("§aMembers: §f" + faction.getMembers().size() + " §a| Lore Fragments: §f" + faction.getUnlockedLoreFragments().size());
        return true;
    }

    private boolean handleSetSpawn(Player player, Faction faction, FactionManager manager) {
        if (!player.getUniqueId().equals(faction.getAlpha())) {
            player.sendMessage("§cOnly the Alpha can set the Faction spawn.");
            return true;
        }

        faction.setSpawnLocation(player.getLocation());
        manager.save();
        player.sendMessage("§aFaction spawn point set to your current location.");
        return true;
    }

    private boolean handleSpawn(Player player, Faction faction) {
        Location spawn = faction.getSpawnLocation();
        if (spawn == null) {
            player.sendMessage("§cYour Faction has not set a spawn point yet.");
            return true;
        }

        player.teleportAsync(spawn);
        player.sendMessage("§aTeleported to your Faction's spawn point.");
        return true;
    }

    private boolean handleDisband(Player player, Faction faction, FactionManager manager) {
        if (!player.getUniqueId().equals(faction.getAlpha())) {
            player.sendMessage("§cOnly the Alpha can disband the Faction.");
            return true;
        }

        String factionName = faction.getName();
        manager.disbandFaction(faction.getId());
        manager.save();
        player.sendMessage("§cThe Howling Faction §f" + factionName + " §chas been disbanded.");
        return true;
    }

    private boolean handleDeposit(Player player, Faction faction, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /faction deposit <amount>");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount.");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage("§cAmount must be greater than zero.");
            return true;
        }

        EconomyManager eco = ShadowfangCorePlugin.getInstance().getEconomyManager();
        if (eco.removeBalance(player.getUniqueId(), amount)) {
            faction.addHoardBalance(amount);
            ShadowfangCorePlugin.getInstance().getFactionManager().save();
            player.sendMessage("§aDeposited §f" + String.format("%.0f", amount) + " §aSilver Coins into the Faction Hoard.");
        } else {
            player.sendMessage("§cYou do not have enough Silver Coins.");
        }
        return true;
    }

    private boolean handleWithdraw(Player player, Faction faction, String[] args) {
        if (!player.getUniqueId().equals(faction.getAlpha())) {
            player.sendMessage("§cOnly the Alpha can withdraw from the Faction Hoard.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /faction withdraw <amount>");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount.");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage("§cAmount must be greater than zero.");
            return true;
        }

        if (faction.getHoardBalance() < amount) {
            player.sendMessage("§cThe Faction Hoard does not have enough Silver Coins.");
            return true;
        }

        faction.removeHoardBalance(amount);
        EconomyManager eco = ShadowfangCorePlugin.getInstance().getEconomyManager();
        eco.addBalance(player.getUniqueId(), amount);
        ShadowfangCorePlugin.getInstance().getFactionManager().save();
        player.sendMessage("§aWithdrew §f" + String.format("%.0f", amount) + " §aSilver Coins from the Faction Hoard.");
        return true;
    }

    private boolean handleInvite(Player player, Faction faction, FactionManager manager, String[] args) {
        if (!player.getUniqueId().equals(faction.getAlpha()) && !faction.getRank(player.getUniqueId()).isAtLeast(FactionRank.BETA)) {
            player.sendMessage("§cOnly the Alpha and Beta can invite players.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /faction invite <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found online.");
            return true;
        }

        if (manager.getPlayerFaction(target.getUniqueId()) != null) {
            player.sendMessage("§cThat player is already in a Faction.");
            return true;
        }

        if (manager.hasPendingInvite(target.getUniqueId())) {
            player.sendMessage("§cThat player already has a pending invitation.");
            return true;
        }

        manager.addPendingInvite(target.getUniqueId(), faction.getId(), faction.getName(), player.getName());

        player.sendMessage("§aInvitation sent to " + target.getName() + ".");
        target.sendMessage("§eYou have been invited to join the Howling Faction: " + faction.getName() + " §eby " + player.getName() + ".");
        target.sendMessage("§eType §f/f accept §eto accept or §f/f deny §eto decline.");
        return true;
    }

    private boolean handleAccept(Player player, FactionManager manager) {
        if (!manager.hasPendingInvite(player.getUniqueId())) {
            player.sendMessage("§cYou have no pending faction invitations.");
            return true;
        }

        if (manager.getPlayerFaction(player.getUniqueId()) != null) {
            manager.removePendingInvite(player.getUniqueId());
            player.sendMessage("§cYou are already in a Faction. Invitation discarded.");
            return true;
        }

        com.shadowfang.core.faction.FactionPendingInvite invite = manager.getPendingInvite(player.getUniqueId());
        Faction faction = manager.getFaction(invite.getFactionId());
        if (faction == null) {
            manager.removePendingInvite(player.getUniqueId());
            player.sendMessage("§cThat Faction no longer exists.");
            return true;
        }

        faction.addMember(player.getUniqueId(), FactionRank.OMEGA);
        manager.addPlayerToIndex(player.getUniqueId(), faction.getId());
        manager.removePendingInvite(player.getUniqueId());
        manager.save();

        player.discoverRecipe(BELL_RECIPE_KEY);
        player.sendMessage("§aYou have joined the Howling Faction: " + faction.getName());
        player.sendMessage("§eYou have unlocked the Faction Bell recipe!");
        return true;
    }

    private boolean handleDeny(Player player, FactionManager manager) {
        if (!manager.hasPendingInvite(player.getUniqueId())) {
            player.sendMessage("§cYou have no pending faction invitations.");
            return true;
        }

        com.shadowfang.core.faction.FactionPendingInvite invite = manager.getPendingInvite(player.getUniqueId());
        manager.removePendingInvite(player.getUniqueId());
        player.sendMessage("§cYou have declined the invitation from " + invite.getFactionName() + ".");
        return true;
    }

    private boolean handleClaim(Player player, Faction faction, FactionManager manager, String[] args) {
        if (!player.getUniqueId().equals(faction.getAlpha())) {
            player.sendMessage("§cOnly the Alpha can claim territory.");
            return true;
        }

        if (faction.getBellLocation() == null) {
            player.sendMessage("§cYour Faction has no bell placed. Place a Faction Bell first.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /faction claim <radius>");
            return true;
        }

        int radius;
        try {
            radius = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid radius. Use a positive number.");
            return true;
        }

        if (radius < 1) {
            player.sendMessage("§cRadius must be at least 1.");
            return true;
        }

        if (radius > 100) {
            player.sendMessage("§cRadius cannot exceed 100.");
            return true;
        }

        Location bell = faction.getBellLocation();
        String dim = bell.getWorld().getName();
        int centerChunkX = bell.getBlockX() >> 4;
        int centerChunkZ = bell.getBlockZ() >> 4;

        int newClaimed = manager.claimArea(faction, dim, centerChunkX, centerChunkZ, radius);
        int totalClaimed = manager.getClaimedChunkCount(faction.getId());
        manager.save();

        player.sendMessage("§aClaimed §f" + newClaimed + " §anew chunks. Territory is now §f" + totalClaimed + " §achunks wide.");
        return true;
    }

    private boolean handleKick(Player player, Faction faction, FactionManager manager, String[] args) {
        if (!player.getUniqueId().equals(faction.getAlpha())) {
            player.sendMessage("§cOnly the Alpha can kick members.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /faction kick <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found online.");
            return true;
        }

        if (target.getUniqueId().equals(faction.getAlpha())) {
            player.sendMessage("§cYou cannot kick yourself. Use /faction disband to dissolve the Faction.");
            return true;
        }

        if (faction.getRank(target.getUniqueId()) == null) {
            player.sendMessage("§cThat player is not in your Faction.");
            return true;
        }

        faction.removeMember(target.getUniqueId());
        manager.removePlayerFromIndex(target.getUniqueId());
        manager.save();

        player.sendMessage("§c" + target.getName() + " has been kicked from the Faction.");
        target.sendMessage("§cYou have been kicked from " + faction.getName() + ".");
        return true;
    }

    private boolean handlePromote(Player player, Faction faction, FactionManager manager, String[] args) {
        if (!player.getUniqueId().equals(faction.getAlpha())) {
            player.sendMessage("§cOnly the Alpha can promote members.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /faction promote <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found online.");
            return true;
        }

        FactionRank currentRank = faction.getRank(target.getUniqueId());
        if (currentRank == null) {
            player.sendMessage("§cThat player is not in your Faction.");
            return true;
        }

        FactionRank newRank = switch (currentRank) {
            case OMEGA -> FactionRank.DELTA;
            case DELTA -> FactionRank.BETA;
            default -> {
                player.sendMessage("§cThat player is already at the highest rank.");
                yield null;
            }
        };

        if (newRank != null) {
            faction.setRank(target.getUniqueId(), newRank);
            manager.save();
            player.sendMessage("§a" + target.getName() + " has been promoted to " + newRank.name() + ".");
            target.sendMessage("§aYou have been promoted to " + newRank.name() + " in " + faction.getName() + ".");
        }
        return true;
    }

    private boolean handleDemote(Player player, Faction faction, FactionManager manager, String[] args) {
        if (!player.getUniqueId().equals(faction.getAlpha())) {
            player.sendMessage("§cOnly the Alpha can demote members.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /faction demote <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found online.");
            return true;
        }

        if (target.getUniqueId().equals(faction.getAlpha())) {
            player.sendMessage("§cYou cannot demote yourself.");
            return true;
        }

        FactionRank currentRank = faction.getRank(target.getUniqueId());
        if (currentRank == null) {
            player.sendMessage("§cThat player is not in your Faction.");
            return true;
        }

        FactionRank newRank = switch (currentRank) {
            case BETA -> FactionRank.DELTA;
            case DELTA -> FactionRank.OMEGA;
            default -> {
                player.sendMessage("§cThat player is already at the lowest rank.");
                yield null;
            }
        };

        if (newRank != null) {
            faction.setRank(target.getUniqueId(), newRank);
            manager.save();
            player.sendMessage("§c" + target.getName() + " has been demoted to " + newRank.name() + ".");
            target.sendMessage("§cYou have been demoted to " + newRank.name() + " in " + faction.getName() + ".");
        }
        return true;
    }

    private boolean handleLeave(Player player, Faction faction, FactionManager manager) {
        if (player.getUniqueId().equals(faction.getAlpha())) {
            player.sendMessage("§cThe Alpha cannot leave the Faction. Use /faction disband to dissolve it.");
            return true;
        }

        String factionName = faction.getName();
        faction.removeMember(player.getUniqueId());
        manager.removePlayerFromIndex(player.getUniqueId());
        manager.save();

        player.sendMessage("§cYou have left " + factionName + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(List.of("create", "invite", "accept", "deny", "claim", "info", "setspawn", "spawn", "disband", "deposit", "withdraw", "kick", "promote", "demote", "leave"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (List.of("invite", "kick", "promote", "demote").contains(sub)) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        }
        String partial = args[args.length - 1].toLowerCase();
        completions.removeIf(c -> !c.toLowerCase().startsWith(partial));
        return completions;
    }
}
