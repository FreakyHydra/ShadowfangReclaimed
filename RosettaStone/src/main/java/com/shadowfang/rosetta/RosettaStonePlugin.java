package com.shadowfang.rosetta;

import org.bukkit.plugin.java.JavaPlugin;

public class RosettaStonePlugin extends JavaPlugin {

    private static RosettaStonePlugin INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        getLogger().info("Rosetta Stone loaded — Folia compatibility layer active");
    }

    @Override
    public void onDisable() {
        INSTANCE = null;
    }

    public static RosettaStonePlugin getInstance() {
        return INSTANCE;
    }
}
