package com.koth.plugin.managers;

import com.koth.plugin.KOTH;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RewardManager {
    private final KOTH plugin;

    public RewardManager(KOTH plugin) {
        this.plugin = plugin;
    }

    public void giveReward(Player player) {
        if (!plugin.getConfigManager().isRewardEnabled()) {
            return;
        }

        String command = plugin.getConfigManager().getRewardCommand();
        if (command != null && !command.isEmpty()) {
            command = command.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    public void setReward(String command) {
        plugin.getConfigManager().getConfig().set("reward.command", command);
        plugin.getConfigManager().saveConfig();
    }
}
