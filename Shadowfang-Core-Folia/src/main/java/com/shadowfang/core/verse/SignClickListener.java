package com.shadowfang.core.verse;

import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SignClickListener implements Listener {

    private final ShadowfangCorePlugin plugin;

    public SignClickListener(ShadowfangCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;

        Material type = event.getClickedBlock().getType();
        if (type != Material.OAK_SIGN && type != Material.OAK_WALL_SIGN) return;

        if (!(event.getClickedBlock().getState() instanceof Sign sign)) return;

        String line0 = sign.getLine(0);
        if (line0.isEmpty()) return;

        String worldName = TeleportManager.getWorldNameFromSign(line0);
        if (worldName == null) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "That world is not loaded.");
            return;
        }

        Location spawn = world.getSpawnLocation();
        player.teleportAsync(spawn);
        String displayName = TeleportManager.getDisplayName(worldName);
        player.sendMessage(ChatColor.GREEN + "Teleported to " + displayName + "!");
    }
}
