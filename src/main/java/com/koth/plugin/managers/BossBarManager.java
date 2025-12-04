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
        if (bossBar != null) {
            bossBar.removeAll();
        }
        bossBar = Bukkit.createBossBar(title, BarColor.WHITE, BarStyle.SOLID);
        
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
