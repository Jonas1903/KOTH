package com.koth.plugin.utils;

import com.koth.plugin.KOTH;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final KOTH plugin;
    private FileConfiguration config;

    public ConfigManager(KOTH plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getMessage(String key) {
        String message = config.getString("messages." + key, key);
        String prefix = config.getString("messages.prefix", "&6[KOTH] &r");
        return colorize(prefix + message);
    }

    public String getMessageWithoutPrefix(String key) {
        String message = config.getString("messages." + key, key);
        return colorize(message);
    }

    public String colorize(String message) {
        return message.replace("&", "§");
    }

    public int getCaptureTime() {
        return config.getInt("capture-time", 60);
    }

    public int getEventInterval() {
        return config.getInt("event-interval", 60);
    }

    public boolean isRewardEnabled() {
        return config.getBoolean("reward.enabled", true);
    }
}
