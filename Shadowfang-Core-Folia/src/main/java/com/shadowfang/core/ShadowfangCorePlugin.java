package com.shadowfang.core;

import com.shadowfang.core.bounty.BountyManager;
import com.shadowfang.core.command.SrDispatcher;
import com.shadowfang.core.command.VerseCommand;
import com.shadowfang.core.commands.BountyCommand;
import com.shadowfang.core.commands.EconomyCommand;
import com.shadowfang.core.commands.FactionCommand;
import com.shadowfang.core.commands.LoreCommand;
import com.shadowfang.core.economy.EconomyManager;
import com.shadowfang.core.events.*;
import com.shadowfang.core.faction.FactionManager;
import com.shadowfang.core.infoboard.BuiltinPrograms;
import com.shadowfang.core.infoboard.InfoBoardCommand;
import com.shadowfang.core.infoboard.InfoBoardListener;
import com.shadowfang.core.infoboard.InfoBoardManager;
import com.shadowfang.core.lore.LoreManager;
import com.shadowfang.core.verse.HubManager;
import com.shadowfang.core.verse.SignClickListener;
import com.shadowfang.core.verse.TeleportManager;
import com.shadowfang.core.verse.VerseManager;
import com.shadowfang.core.worldedit.WorldEditCommand;
import com.shadowfang.core.worldedit.WorldEditListener;
import com.shadowfang.core.worldedit.WorldEditManager;
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
    private InfoBoardManager infoBoardManager;
    private WorldEditManager worldEditManager;

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

        factionManager.load();
        loreManager.load();

        // --- Collect all command executors ---
        FactionCommand factionCommand = new FactionCommand();
        LoreCommand loreCommand = new LoreCommand();
        EconomyCommand economyCommand = new EconomyCommand();
        BountyCommand bountyCommand = new BountyCommand();

        // --- Register Events ---
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

        // --- Initialize InfoBoards ---
        infoBoardManager = new InfoBoardManager(this);
        infoBoardManager.setBuiltinPrograms(new BuiltinPrograms(this).all());
        infoBoardManager.load();
        InfoBoardCommand boardCmd = new InfoBoardCommand(this, infoBoardManager);
        getServer().getPluginManager().registerEvents(new InfoBoardListener(this, infoBoardManager), this);

        // --- Initialize WorldEdit / Path Tools ---
        worldEditManager = new WorldEditManager(this);
        WorldEditCommand weCmd = new WorldEditCommand(worldEditManager);
        getServer().getPluginManager().registerEvents(new WorldEditListener(worldEditManager), this);

        // --- Verse sub-plugin ---
        VerseCommand verseCmd = new VerseCommand(teleportManager);

        // --- SrDispatcher: single /sr root for all commands ---
        SrDispatcher sr = new SrDispatcher();
        sr.register(factionCommand, "f", "faction");
        sr.register(economyCommand, "e", "economy");
        sr.register(bountyCommand, "b", "bounty");
        sr.register(loreCommand, "l", "lore");
        sr.registerPerm(weCmd, "shadowfang.we.use", "r", "we", "worldedit", "road", "path", "pave");
        sr.registerPerm(boardCmd, "shadowfang.admin", "i", "infoboard", "board");
        sr.register(verseCmd, "v", "verse", "sign", "list", "worlds");
        sr.registerDef(verseCmd, new String[]{"travel"}, "t", "travel");
        sr.registerDef(verseCmd, new String[]{"hub"}, "h", "hub");
        sr.registerDef(verseCmd, new String[]{"warp"}, "w", "warp");
        sr.registerDef(verseCmd, new String[]{"spawn"}, "s", "spawn", "setspawn");

        if (getCommand("sr") != null) {
            getCommand("sr").setExecutor(sr);
            getCommand("sr").setTabCompleter(sr);
        }

        // --- Custom Faction Bell recipe ---
        registerBellRecipe();

        // --- Initialize Verse ---
        TeleportManager.registerWorld("world", "Overworld");
        TeleportManager.registerWorld("world_nether", "Nether");
        TeleportManager.registerWorld("world_the_end", "End");

        getServer().getGlobalRegionScheduler().execute(this, () -> {
            teleportManager.loadSavedWorlds();
            teleportManager.autoDetectWorlds();
        });

        getLogger().info("Shadowfang Core (Folia) enabled.");
    }

    @Override
    public void onDisable() {
        if (factionManager != null) {
            factionManager.save();
        }
        if (infoBoardManager != null) {
            infoBoardManager.shutdown();
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
    public InfoBoardManager getInfoBoardManager() { return infoBoardManager; }
    public WorldEditManager getWorldEditManager() { return worldEditManager; }
}
