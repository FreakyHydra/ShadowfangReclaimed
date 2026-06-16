package com.shadowfang.core.command;

import com.shadowfang.core.verse.TeleportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class VerseCommand implements CommandExecutor, TabCompleter {

    private final TeleportManager teleportManager;

    public VerseCommand(TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return teleportManager.handleCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return teleportManager.onTabComplete(sender, command, alias, args);
    }
}
