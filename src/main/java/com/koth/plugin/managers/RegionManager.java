package com.koth.plugin.managers;

import com.koth.plugin.KOTH;
import com.koth.plugin.models.KOTHRegion;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionManager {
    private final KOTH plugin;
    private KOTHRegion region;
    private final Map<UUID, Location> selections = new HashMap<>();

    public RegionManager(KOTH plugin) {
        this.plugin = plugin;
        loadRegion();
    }

    public void loadRegion() {
        ConfigurationSection section = plugin.getConfigManager().getConfig().getConfigurationSection("region");
        if (section != null) {
            region = KOTHRegion.loadFromConfig(section);
        }
    }

    public void saveRegion() {
        if (region != null) {
            ConfigurationSection section = plugin.getConfigManager().getConfig().getConfigurationSection("region");
            if (section == null) {
                section = plugin.getConfigManager().getConfig().createSection("region");
            }
            region.saveToConfig(section);
            plugin.getConfigManager().saveConfig();
        }
    }

    public void setPosition(Player player, int posNumber) {
        Location loc = player.getLocation();
        selections.put(player.getUniqueId(), loc);
        player.sendMessage(plugin.getConfigManager().colorize("§aPosition " + posNumber + " set at: " + 
            loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()));
    }

    public void createRegion(Player player) {
        Location pos1 = selections.get(player.getUniqueId());
        Location pos2 = player.getLocation();
        
        if (pos1 == null) {
            player.sendMessage(plugin.getConfigManager().colorize("§cPlease set position 1 first!"));
            return;
        }
        
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            player.sendMessage(plugin.getConfigManager().colorize("§cBoth positions must be in the same world!"));
            return;
        }
        
        region = new KOTHRegion(pos1, pos2);
        saveRegion();
        selections.remove(player.getUniqueId());
        player.sendMessage(plugin.getConfigManager().getMessage("region-set"));
    }

    public KOTHRegion getRegion() {
        return region;
    }

    public boolean hasRegion() {
        return region != null;
    }

    public boolean isInRegion(Location location) {
        return region != null && region.contains(location);
    }
}
