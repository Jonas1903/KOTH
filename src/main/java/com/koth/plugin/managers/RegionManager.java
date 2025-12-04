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
    private final Map<UUID, Location> pos1Selections = new HashMap<>();
    private final Map<UUID, Location> pos2Selections = new HashMap<>();

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
        if (posNumber == 1) {
            pos1Selections.put(player.getUniqueId(), loc);
        } else if (posNumber == 2) {
            pos2Selections.put(player.getUniqueId(), loc);
        }
        
        // Use config message if available, otherwise use default
        String message = plugin.getConfigManager().getConfig().getString("messages.position-set");
        if (message == null) {
            message = "&aPosition %pos% set at: %x%, %y%, %z%";
        }
        message = plugin.getConfigManager().colorize(
            plugin.getConfigManager().getConfig().getString("messages.prefix", "&6[KOTH] &r") + message
                .replace("%pos%", String.valueOf(posNumber))
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()))
        );
        player.sendMessage(message);
    }

    public void createRegion(Player player) {
        Location pos1 = pos1Selections.get(player.getUniqueId());
        Location pos2 = pos2Selections.get(player.getUniqueId());
        
        if (pos1 == null) {
            player.sendMessage(plugin.getConfigManager().colorize("§cPlease set position 1 first using /koth setregion pos1"));
            return;
        }
        
        if (pos2 == null) {
            player.sendMessage(plugin.getConfigManager().colorize("§cPlease set position 2 first using /koth setregion pos2"));
            return;
        }
        
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            player.sendMessage(plugin.getConfigManager().colorize("§cBoth positions must be in the same world!"));
            return;
        }
        
        region = new KOTHRegion(pos1, pos2);
        saveRegion();
        pos1Selections.remove(player.getUniqueId());
        pos2Selections.remove(player.getUniqueId());
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
