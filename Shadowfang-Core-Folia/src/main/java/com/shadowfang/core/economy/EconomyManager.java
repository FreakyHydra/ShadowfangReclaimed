package com.shadowfang.core.economy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shadowfang.core.ShadowfangCorePlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {
    private final File dataFile;
    private final Gson gson;
    private Map<UUID, Double> balances;

    public EconomyManager() {
        ShadowfangCorePlugin plugin = ShadowfangCorePlugin.getInstance();
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        dataFile = new File(plugin.getDataFolder(), "economy.json");
        gson = new Gson();
        balances = new HashMap<>();
        
        loadBalances();
    }

    private void loadBalances() {
        if (!dataFile.exists()) return;
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<UUID, Double>>(){}.getType();
            Map<UUID, Double> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                balances = loaded;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveBalances() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(balances, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getBalance(UUID player) {
        return balances.getOrDefault(player, 0.0);
    }

    public void addBalance(UUID player, double amount) {
        balances.put(player, getBalance(player) + amount);
        saveBalances();
    }

    public boolean removeBalance(UUID player, double amount) {
        double current = getBalance(player);
        if (current >= amount) {
            balances.put(player, current - amount);
            saveBalances();
            return true;
        }
        return false;
    }
}
