package com.shadowfang.core.quest;

import com.shadowfang.core.ShadowfangCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestManager {

    private final ShadowfangCorePlugin plugin;
    private final Map<UUID, PlayerQuestState> playerStates = new HashMap<>();

    private static final String NAMESPACE = "shadowfang";

    public QuestManager(ShadowfangCorePlugin plugin) {
        this.plugin = plugin;
    }

    public static NamespacedKey key(String path) {
        return new NamespacedKey(NAMESPACE, path);
    }

    public void grantAdvancement(Player player, String advancementPath) {
        Advancement advancement = Bukkit.getAdvancement(key(advancementPath));
        if (advancement == null) {
            plugin.getLogger().warning("Advancement not found: " + advancementPath);
            return;
        }

        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (progress.isDone()) return;

        for (String criterion : progress.getRemainingCriteria()) {
            progress.awardCriteria(criterion);
        }
    }

    public boolean hasAdvancement(Player player, String advancementPath) {
        Advancement advancement = Bukkit.getAdvancement(key(advancementPath));
        if (advancement == null) return false;
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        return progress.isDone();
    }

    // --- Talisman Quests ---
    public void onFirstFragmentFound(Player player) {
        grantAdvancement(player, "talismans/first_fragment");
        PlayerQuestState state = getState(player);
        state.fragmentCount = 1;
        checkFragmentQuests(player);
    }

    public void onFragmentCountChanged(Player player, int count) {
        PlayerQuestState state = getState(player);
        state.fragmentCount = count;

        if (count >= 3) {
            grantAdvancement(player, "talismans/fragment_collector");
        }
        if (count >= 5) {
            grantAdvancement(player, "talismans/archivist");
        }
    }

    public void onTalismanBound(Player player, boolean cursed) {
        grantAdvancement(player, "talismans/the_binding");
        if (cursed) {
            grantAdvancement(player, "talismans/awakened");
        }
    }

    public void onTalismanHeld(Player player) {
        grantAdvancement(player, "talismans/power_of_deep");
    }

    public void onVeinMined(Player player) {
        PlayerQuestState state = getState(player);
        state.veinCount++;
        if (state.veinCount == 10) {
            grantAdvancement(player, "talismans/vein_walker");
        }
    }

    public void onExcavateUsed(Player player) {
        PlayerQuestState state = getState(player);
        state.excavateCount++;
        if (state.excavateCount == 10) {
            grantAdvancement(player, "talismans/earth_shaper");
        }
    }

    public void onProspectUsed(Player player) {
        if (!hasAdvancement(player, "talismans/prospector")) {
            grantAdvancement(player, "talismans/prospector");
            checkModeMaster(player);
        }
    }

    public void onModeUsed(Player player, String mode) {
        PlayerQuestState state = getState(player);
        if (mode.equals("VEIN")) state.usedVein = true;
        if (mode.equals("EXCAVATE")) state.usedExcavate = true;
        if (mode.equals("PROSPECT")) state.usedProspect = true;
        checkModeMaster(player);
    }

    private void checkModeMaster(Player player) {
        PlayerQuestState state = getState(player);
        if (state.usedVein && state.usedExcavate && state.usedProspect) {
            grantAdvancement(player, "talismans/mode_master");
        }
    }

    private void checkFragmentQuests(Player player) {
        PlayerQuestState state = getState(player);
        if (state.fragmentCount >= 3) {
            grantAdvancement(player, "talismans/fragment_collector");
        }
        if (state.fragmentCount >= 5) {
            grantAdvancement(player, "talismans/archivist");
        }
    }

    // --- Faction Quests ---
    public void onBellCrafted(Player player) {
        grantAdvancement(player, "factions/bell_maker");
    }

    public void onBellRung(Player player) {
        grantAdvancement(player, "factions/loyal_member");
    }

    public void onTerritoryClaimed(Player player) {
        grantAdvancement(player, "factions/territory_claimed");
    }

    // --- Elevator Quests ---
    public void onElevatorUsed(Player player) {
        PlayerQuestState state = getState(player);
        state.elevatorUses++;
        if (state.elevatorUses == 1) {
            grantAdvancement(player, "elevators/first_step");
        }
        if (state.elevatorUses == 10) {
            grantAdvancement(player, "elevators/frequent_flyer");
        }
    }

    public void onElevatorGroupCreated(Player player) {
        grantAdvancement(player, "elevators/architect");
    }

    // --- Economy Quests ---
    public void onBountyPlaced(Player player) {
        grantAdvancement(player, "economy/bounty_hunter");
    }

    public void onBountyCollected(Player player, int amount) {
        grantAdvancement(player, "economy/bounty_hunter");
        PlayerQuestState state = getState(player);
        state.bountyEarned += amount;
        if (state.bountyEarned >= 1000) {
            grantAdvancement(player, "economy/payday");
        }
    }

    // --- Travel Quests ---
    public void onVerseSignUsed(Player player) {
        grantAdvancement(player, "travel/verse_traveler");
    }

    public void onWorldJumped(Player player) {
        grantAdvancement(player, "travel/world_jumper");
    }

    public void onHomeSet(Player player) {
        grantAdvancement(player, "travel/home_away");
    }

    // --- Lore Quests ---
    public void onLoreFragmentRead(Player player, int fragmentId) {
        String path = switch (fragmentId) {
            case 1 -> "lore/a_bloody_moon";
            case 2 -> "lore/silver_scars";
            case 3 -> "lore/the_last_stand";
            case 4 -> "lore/notes_on_warden";
            case 5 -> "lore/torn_journal";
            default -> null;
        };
        if (path != null) {
            grantAdvancement(player, path);
        }
    }

    private PlayerQuestState getState(Player player) {
        return playerStates.computeIfAbsent(player.getUniqueId(), k -> new PlayerQuestState());
    }

    public static class PlayerQuestState {
        public int fragmentCount = 0;
        public int veinCount = 0;
        public int excavateCount = 0;
        public int elevatorUses = 0;
        public int bountyEarned = 0;
        public boolean usedVein = false;
        public boolean usedExcavate = false;
        public boolean usedProspect = false;
    }
}
