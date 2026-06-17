package com.shadowfang.talisman;

import org.bukkit.plugin.java.JavaPlugin;

public class AbyssalTalismanPlugin extends JavaPlugin {

    private static AbyssalTalismanPlugin instance;
    private VeinMineManager veinMineManager;
    private TalismanVaultManager vaultManager;
    private TalismanVaultListener vaultListener;
    private TalismanCommand talismanCommand;

    @Override
    public void onEnable() {
        instance = this;

        vaultManager = new TalismanVaultManager(this);
        veinMineManager = new VeinMineManager(this);
        vaultListener = new TalismanVaultListener(this);
        talismanCommand = new TalismanCommand(this);

        getServer().getPluginManager().registerEvents(new VeinMineListener(this), this);
        getServer().getPluginManager().registerEvents(new TalismanProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(vaultListener, this);

        getCommand("talisman").setExecutor(talismanCommand);
        getCommand("talisman").setTabCompleter(talismanCommand);
        getCommand("ta").setExecutor(new TaCommand(this));
        getCommand("ta").setTabCompleter(new TaCommand(this));

        getLogger().info("Abyssal Talisman (alpha 0.1.0) enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Abyssal Talisman disabled.");
    }

    public static AbyssalTalismanPlugin getInstance() { return instance; }
    public VeinMineManager getVeinMineManager() { return veinMineManager; }
    public TalismanVaultManager getVaultManager() { return vaultManager; }
    public TalismanVaultListener getVaultListener() { return vaultListener; }
    public TalismanCommand getTalismanCommand() { return talismanCommand; }
}
