package com.shadowfang.core.bounty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BountyManager {
    private final Map<UUID, BountyTask> activeBounties;
    private final File dataFile;
    private final Gson gson;

    public BountyManager() {
        activeBounties = new HashMap<>();
        ShadowfangCorePlugin plugin = ShadowfangCorePlugin.getInstance();
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        dataFile = new File(plugin.getDataFolder(), "bounties.json");
        gson = new GsonBuilder().setPrettyPrinting().create();
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!dataFile.exists()) return;
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> raw = gson.fromJson(reader, type);
            if (raw != null) {
                for (Map.Entry<String, Object> entry : raw.entrySet()) {
                    UUID playerId = UUID.fromString(entry.getKey());
                    Map<String, Object> taskData = (Map<String, Object>) entry.getValue();
                    String targetName = (String) taskData.get("target");
                    double required = ((Number) taskData.get("required")).doubleValue();
                    double reward = ((Number) taskData.get("reward")).doubleValue();
                    int currentKills = ((Number) taskData.get("currentKills")).intValue();
                    try {
                        EntityType target = EntityType.valueOf(targetName);
                        BountyTask task = new BountyTask(target, (int) required, reward);
                        task.currentKills = currentKills;
                        activeBounties.put(playerId, task);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            ShadowfangCorePlugin.getInstance().getLogger().warning("Failed to load bounties: " + e.getMessage());
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            Map<String, Map<String, Object>> toSave = new HashMap<>();
            for (Map.Entry<UUID, BountyTask> entry : activeBounties.entrySet()) {
                Map<String, Object> taskData = new HashMap<>();
                taskData.put("target", entry.getValue().target.name());
                taskData.put("required", entry.getValue().required);
                taskData.put("reward", entry.getValue().reward);
                taskData.put("currentKills", entry.getValue().currentKills);
                toSave.put(entry.getKey().toString(), taskData);
            }
            gson.toJson(toSave, writer);
        } catch (Exception e) {
            ShadowfangCorePlugin.getInstance().getLogger().warning("Failed to save bounties: " + e.getMessage());
        }
    }

    public void assignBounty(UUID player) {
        EntityType target = Math.random() > 0.5 ? EntityType.ZOMBIE : EntityType.SKELETON;
        int requiredKills = (int) (Math.random() * 5) + 5;
        double reward = requiredKills * 10.0;

        activeBounties.put(player, new BountyTask(target, requiredKills, reward));
        save();
    }

    public BountyTask getBounty(UUID player) {
        return activeBounties.get(player);
    }

    public void clearBounty(UUID player) {
        activeBounties.remove(player);
        save();
    }

    public static class BountyTask {
        public final EntityType target;
        public final int required;
        public final double reward;
        public int currentKills;

        public BountyTask(EntityType target, int required, double reward) {
            this.target = target;
            this.required = required;
            this.reward = reward;
            this.currentKills = 0;
        }

        public boolean isComplete() {
            return currentKills >= required;
        }
    }
}
