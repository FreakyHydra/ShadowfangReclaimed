package com.shadowfang.core.infoboard;

import com.shadowfang.core.ShadowfangCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.TextDisplay;

import java.util.List;
import java.util.UUID;

public class BoardRenderer {

    private final ShadowfangCorePlugin plugin;

    public BoardRenderer(ShadowfangCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnEntities(InfoBoard board) {
        World world = Bukkit.getWorld(board.getWorld());
        if (world == null) return;
        Location loc = new Location(world, board.getX(), board.getY(), board.getZ(), board.getYaw(), board.getPitch());

        Bukkit.getRegionScheduler().execute(plugin, loc, () -> {
            if (!loc.isWorldLoaded()) return;

            TextDisplay textDisplay = (TextDisplay) world.spawnEntity(loc, EntityType.TEXT_DISPLAY);
            textDisplay.text(Component.text("§7Loading..."));
            textDisplay.setBillboard(TextDisplay.Billboard.FIXED);
            textDisplay.setBackgroundColor(Color.fromARGB(0xBB, 0x00, 0x00, 0x00));
            textDisplay.setDefaultBackground(false);
            textDisplay.setSeeThrough(false);
            textDisplay.setShadowed(false);
            textDisplay.setLineWidth(400);
            textDisplay.setAlignment(TextDisplay.TextAlignment.LEFT);

            Interaction interaction = (Interaction) world.spawnEntity(loc, EntityType.INTERACTION);
            interaction.setInteractionWidth(3.0f);
            interaction.setInteractionHeight(2.0f);
            interaction.setResponsive(true);

            board.setTextDisplayUuid(textDisplay.getUniqueId());
            board.setInteractionUuid(interaction.getUniqueId());
        });
    }

    public void removeEntities(InfoBoard board) {
        if (board.getTextDisplayUuid() != null) removeEntity(board.getTextDisplayUuid());
        if (board.getInteractionUuid() != null) removeEntity(board.getInteractionUuid());
        board.setTextDisplayUuid(null);
        board.setInteractionUuid(null);
    }

    private void removeEntity(UUID uuid) {
        if (uuid == null) return;
        org.bukkit.entity.Entity entity = Bukkit.getEntity(uuid);
        if (entity == null) return;
        entity.getScheduler().run(plugin, scheduledTask -> entity.remove(), null);
    }

    public void updateText(InfoBoard board, List<String> lines) {
        UUID uuid = board.getTextDisplayUuid();
        if (uuid == null) return;
        String raw = String.join("\n", lines);

        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            org.bukkit.entity.Entity e = Bukkit.getEntity(uuid);
            if (!(e instanceof TextDisplay td)) return;
            td.getScheduler().run(plugin, scheduledTask -> {
                td.text(Component.text(raw));
            }, null);
        });
    }

    public TextDisplay getTextDisplay(InfoBoard board) {
        UUID uuid = board.getTextDisplayUuid();
        if (uuid == null) return null;
        org.bukkit.entity.Entity e = Bukkit.getEntity(uuid);
        return e instanceof TextDisplay ? (TextDisplay) e : null;
    }

    public Interaction getInteraction(InfoBoard board) {
        UUID uuid = board.getInteractionUuid();
        if (uuid == null) return null;
        org.bukkit.entity.Entity e = Bukkit.getEntity(uuid);
        return e instanceof Interaction ? (Interaction) e : null;
    }

    public Location getLocation(InfoBoard board) {
        World world = Bukkit.getWorld(board.getWorld());
        if (world == null) return null;
        return new Location(world, board.getX(), board.getY(), board.getZ(), board.getYaw(), board.getPitch());
    }
}
