package com.shadowfang.core;

import com.shadowfang.core.commands.FactionCommand;
import com.shadowfang.core.commands.LoreCommand;
import com.shadowfang.core.commands.EconomyCommand;
import com.shadowfang.core.commands.BountyCommand;
import com.shadowfang.core.economy.EconomyManager;
import com.shadowfang.core.bounty.BountyManager;
import com.shadowfang.core.events.*;
import com.shadowfang.core.faction.FactionManager;
import com.shadowfang.core.lore.LoreManager;
import com.shadowfang.core.verse.HubManager;
import com.shadowfang.core.verse.TeleportManager;
import com.shadowfang.core.verse.VerseManager;
import com.shadowfang.core.verse.SignClickListener;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ShadowfangCorePlugin extends JavaPlugin {

    private static ShadowfangCorePlugin instance;
    private FactionManager factionManager;
    private LoreManager loreManager;
    private EconomyManager economyManager;
    private BountyManager bountyManager;
    private HubManager hubManager;
    private TeleportManager teleportManager;
    private VerseManager verseManager;
    private com.shadowfang.core.terminal.WebTerminalServer webTerminalServer;

    @Override
    public void onEnable() {
        instance = this;

        webTerminalServer = new com.shadowfang.core.terminal.WebTerminalServer(this);
        webTerminalServer.start();

        factionManager = new FactionManager();
        loreManager = new LoreManager();
        bountyManager = new BountyManager();
        economyManager = new EconomyManager();
        verseManager = new VerseManager(this);
        hubManager = new HubManager(this);
        teleportManager = new TeleportManager(this);

        // Load data
        factionManager.load();
        loreManager.load();

        // Register Commands
        FactionCommand factionCommand = new FactionCommand();
        LoreCommand loreCommand = new LoreCommand();
        EconomyCommand economyCommand = new EconomyCommand();
        BountyCommand bountyCommand = new BountyCommand();

        getCommand("faction").setExecutor(factionCommand);
        getCommand("faction").setTabCompleter(factionCommand);
        getCommand("lore").setExecutor(loreCommand);
        getCommand("lore").setTabCompleter(loreCommand);
        getCommand("economy").setExecutor(economyCommand);
        getCommand("economy").setTabCompleter(economyCommand);
        getCommand("bounty").setExecutor(bountyCommand);
        getCommand("bounty").setTabCompleter(bountyCommand);

        // Register Events
        getServer().getPluginManager().registerEvents(new FactionProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new FactionBellListener(), this);
        getServer().getPluginManager().registerEvents(new LoreDropListener(), this);
        getServer().getPluginManager().registerEvents(new LoreConsumeListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new WorldChangeListener(), this);
        getServer().getPluginManager().registerEvents(new PortalBlockListener(), this);
        getServer().getPluginManager().registerEvents(new BountyKillListener(), this);
        getServer().getPluginManager().registerEvents(new HelpCommandListener(), this);
        getServer().getPluginManager().registerEvents(new SignClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PortalLinkListener(this), this);

        // Register custom Faction Bell recipe
        registerBellRecipe();

        // Load Verse Settings — only register core vanilla worlds that always exist
        TeleportManager.registerWorld("world", "Overworld");
        TeleportManager.registerWorld("world_nether", "Nether");
        TeleportManager.registerWorld("world_the_end", "End");

        // Register any custom worlds that are already loaded (e.g. from previous session)
        getServer().getGlobalRegionScheduler().execute(this, () -> {
            teleportManager.loadSavedWorlds();
            teleportManager.autoDetectWorlds();
        });

        String[] aliases = {"sfv", "hub", "warp", "worlds", "sign", "list", "setspawn", "spawn"};
        for (String alias : aliases) {
            if (getCommand(alias) != null) {
                getCommand(alias).setExecutor(teleportManager);
                getCommand(alias).setTabCompleter(teleportManager);
            }
        }
        getLogger().info("Shadowfang Core (Folia) enabled.");
    }

    @Override
    public void onDisable() {
        if (factionManager != null) {
            factionManager.save();
        }
        if (webTerminalServer != null) {
            webTerminalServer.stop();
        }
        getLogger().info("Shadowfang Core disabled.");
    }

    private void registerBellRecipe() {
        NamespacedKey key = FactionCommand.BELL_RECIPE_KEY;
        NamespacedKey tagKey = new NamespacedKey(this, "isFactionBell");

        ItemStack result = new ItemStack(Material.BELL);
        ItemMeta meta = result.getItemMeta();
        meta.getPersistentDataContainer().set(tagKey, PersistentDataType.BYTE, (byte) 1);
        meta.setCustomModelData(1001);
        result.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("GGG", "G G", "G G");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        getServer().addRecipe(recipe);
    }

    public static ShadowfangCorePlugin getInstance() { return instance; }
    public FactionManager getFactionManager() { return factionManager; }
    public LoreManager getLoreManager() { return loreManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public BountyManager getBountyManager() { return bountyManager; }
    public HubManager getHubManager() { return hubManager; }
    public TeleportManager getTeleportManager() { return teleportManager; }
    public VerseManager getVerseManager() { return verseManager; }
}
