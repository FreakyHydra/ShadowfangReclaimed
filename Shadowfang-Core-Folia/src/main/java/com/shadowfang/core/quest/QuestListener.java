package com.shadowfang.core.quest;

import com.shadowfang.core.ShadowfangCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

public class QuestListener implements Listener {

    private final ShadowfangCorePlugin plugin;
    private final QuestManager questManager;
    private final NamespacedKey loreKey;

    public QuestListener(ShadowfangCorePlugin plugin, QuestManager questManager) {
        this.plugin = plugin;
        this.questManager = questManager;
        this.loreKey = new NamespacedKey("shadowfangreclaimed", "is_lore_fragment");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            questManager.grantAdvancement(player, "root/root");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand.getType() == Material.WRITTEN_BOOK) {
            BookMeta meta = (BookMeta) hand.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(loreKey, PersistentDataType.BYTE)) {
                String title = meta.getTitle();
                if (title != null) {
                    if (title.contains("A Bloody Moon")) questManager.onLoreFragmentRead(player, 1);
                    else if (title.contains("Silver Scars")) questManager.onLoreFragmentRead(player, 2);
                    else if (title.contains("The Last Stand")) questManager.onLoreFragmentRead(player, 3);
                    else if (title.contains("Notes on the Warden")) questManager.onLoreFragmentRead(player, 4);
                    else if (title.contains("Torn Journal")) questManager.onLoreFragmentRead(player, 5);
                }
            }
        }

        if (hand.getType() == Material.BELL) {
            questManager.onBellRung(player);
        }

        if (event.getClickedBlock() != null) {
            Material mat = event.getClickedBlock().getType();
            if (mat.name().contains("SIGN")) {
                Block block = event.getClickedBlock();
                Sign sign = (Sign) block.getState();
                String firstLine = sign.line(0).toString().toLowerCase();
                if (firstLine.contains("[verse]") || firstLine.contains("[v]") || firstLine.contains("[warp]") || firstLine.contains("[hub]") || firstLine.contains("[spawn]")) {
                    questManager.onVerseSignUsed(player);
                }
            }

            if (mat == Material.LADDER || mat == Material.BIRCH_BUTTON || mat == Material.OAK_BUTTON || mat == Material.STONE_BUTTON) {
                questManager.onElevatorUsed(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        String fromWorld = event.getFrom().getWorld().getName();
        String toWorld = event.getTo().getWorld().getName();

        if (!fromWorld.equals(toWorld)) {
            questManager.onWorldJumped(player);
        }
    }
}
