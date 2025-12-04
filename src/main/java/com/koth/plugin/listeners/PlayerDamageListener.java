package com.koth.plugin.listeners;

import com.koth.plugin.KOTH;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageListener implements Listener {
    private final KOTH plugin;

    public PlayerDamageListener(KOTH plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        // Check if a player was damaged by another player
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            
            // If KOTH is active and the damaged player is in the region
            if (plugin.getKothManager().isActive() && 
                plugin.getRegionManager().isInRegion(damaged.getLocation())) {
                
                // Reset their capture progress
                plugin.getKothManager().resetPlayerProgress(damaged.getUniqueId());
            }
        }
    }
}
