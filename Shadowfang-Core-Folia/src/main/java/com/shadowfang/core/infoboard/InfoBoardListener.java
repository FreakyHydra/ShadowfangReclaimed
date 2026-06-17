package com.shadowfang.core.infoboard;

import com.shadowfang.core.ShadowfangCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InfoBoardListener implements Listener {

    private final ShadowfangCorePlugin plugin;
    private final InfoBoardManager manager;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public InfoBoardListener(ShadowfangCorePlugin plugin, InfoBoardManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction)) return;
        Player player = event.getPlayer();
        UUID entityId = event.getRightClicked().getUniqueId();

        long now = System.currentTimeMillis();
        Long last = cooldowns.get(player.getUniqueId());
        if (last != null && now - last < 500) return;
        cooldowns.put(player.getUniqueId(), now);

        for (InfoBoard board : manager.getAllBoards()) {
            if (entityId.equals(board.getInteractionUuid())) {
                event.setCancelled(true);
                if (player.isSneaking()) {
                    showInfo(player, board);
                } else {
                    manager.cycleProgram(board.getId());
                    player.sendActionBar(Component.text("§aSwitched program"));
                }
                return;
            }
        }
    }

    private void showInfo(Player player, InfoBoard board) {
        String programs = String.join(", ", board.getProgramNames());
        player.sendMessage(Component.text("§6§lBOARD: §e" + board.getId()));
        player.sendMessage(Component.text("§7World: §f" + board.getWorld()));
        player.sendMessage(Component.text("§7Location: §f" + String.format("%.1f", board.getX()) + ", " +
                String.format("%.1f", board.getY()) + ", " + String.format("%.1f", board.getZ())));
        player.sendMessage(Component.text("§7Programs: §f" + (programs.isEmpty() ? "none" : programs)));
    }
}
