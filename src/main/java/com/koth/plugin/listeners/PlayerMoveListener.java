package com.koth.plugin.listeners;

import com.koth.plugin.KOTH;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerMoveListener implements Listener {
    private final KOTH plugin;

    public PlayerMoveListener(KOTH plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Add player to boss bar if active
        if (plugin.getBossBarManager().hasBossBar()) {
            plugin.getBossBarManager().addPlayer(event.getPlayer());
        }
    }
}
