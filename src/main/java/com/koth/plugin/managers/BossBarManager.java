package com.koth.plugin.managers;

import com.koth.plugin.KOTH;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarManager {
    private final KOTH plugin;
    private BossBar bossBar;

    public BossBarManager(KOTH plugin) {
        this.plugin = plugin;
    }

    public void createBossBar(String title) {
        // Check if boss bar is enabled in config
        if (!plugin.getConfig().getBoolean("boss-bar.enabled", true)) {
            return;
        }
        
        if (bossBar != null) {
            bossBar.removeAll();
        }
        
        // Get boss bar settings from config
        String colorStr = plugin.getConfig().getString("boss-bar.color", "WHITE");
        String styleStr = plugin.getConfig().getString("boss-bar.style", "SOLID");
        
        BarColor color;
        try {
            color = BarColor.valueOf(colorStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            color = BarColor.WHITE;
            plugin.getLogger().warning("Invalid boss bar color in config: " + colorStr + ", using WHITE");
        }
        
        BarStyle style;
        try {
            style = BarStyle.valueOf(styleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            style = BarStyle.SOLID;
            plugin.getLogger().warning("Invalid boss bar style in config: " + styleStr + ", using SOLID");
        }
        
        bossBar = Bukkit.createBossBar(title, color, style);
        
        // Add all online players to the boss bar
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
        bossBar.setVisible(true);
    }

    public void updateBossBar(String title, double progress) {
        if (bossBar != null) {
            bossBar.setTitle(title);
            bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
        }
    }

    public void addPlayer(Player player) {
        if (bossBar != null && !bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
        }
    }

    public void removeBossBar() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
            bossBar = null;
        }
    }

    public boolean hasBossBar() {
        return bossBar != null;
    }
}
