package com.koth.plugin;

import com.koth.plugin.commands.KOTHCommand;
import com.koth.plugin.listeners.PlayerMoveListener;
import com.koth.plugin.managers.BossBarManager;
import com.koth.plugin.managers.KOTHManager;
import com.koth.plugin.managers.RegionManager;
import com.koth.plugin.managers.RewardManager;
import com.koth.plugin.utils.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class KOTH extends JavaPlugin {
    private ConfigManager configManager;
    private RegionManager regionManager;
    private KOTHManager kothManager;
    private BossBarManager bossBarManager;
    private RewardManager rewardManager;

    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);
        regionManager = new RegionManager(this);
        bossBarManager = new BossBarManager(this);
        rewardManager = new RewardManager(this);
        kothManager = new KOTHManager(this);

        // Register commands
        KOTHCommand kothCommand = new KOTHCommand(this);
        getCommand("koth").setExecutor(kothCommand);
        getCommand("koth").setTabCompleter(kothCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);

        // Schedule first KOTH if region is set
        if (regionManager.hasRegion()) {
            kothManager.scheduleNextKOTH();
        }

        getLogger().info("KOTH plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Clean shutdown
        if (kothManager != null) {
            kothManager.shutdown();
        }
        
        getLogger().info("KOTH plugin has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public KOTHManager getKothManager() {
        return kothManager;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }
}
