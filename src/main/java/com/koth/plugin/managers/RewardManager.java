package com.koth.plugin.managers;

import com.koth.plugin.KOTH;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class RewardManager {
    private final KOTH plugin;

    public RewardManager(KOTH plugin) {
        this.plugin = plugin;
    }

    public void giveReward(Player player) {
        if (!plugin.getConfigManager().isRewardEnabled()) {
            return;
        }

        ItemStack reward = getRewardItem();
        if (reward != null && !reward.getType().isAir()) {
            // Clone the reward to prevent any modifications to the original config ItemStack
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(reward.clone());
            
            // If inventory was full, drop items at player's location
            if (!leftover.isEmpty()) {
                for (ItemStack item : leftover.values()) {
                    player.getWorld().dropItem(player.getLocation(), item);
                }
                player.sendMessage(plugin.getConfigManager().getMessage("reward-received-dropped"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("reward-received"));
            }
        }
    }

    public void setReward(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return;
        }

        // Serialize the item to config
        Map<String, Object> serialized = item.serialize();
        plugin.getConfigManager().getConfig().set("reward.item", serialized);
        plugin.getConfigManager().saveConfig();
    }

    public ItemStack getRewardItem() {
        ConfigurationSection itemSection = plugin.getConfigManager().getConfig().getConfigurationSection("reward.item");
        if (itemSection == null) {
            return null;
        }

        try {
            Map<String, Object> itemMap = itemSection.getValues(false);
            if (itemMap.isEmpty()) {
                return null;
            }
            return ItemStack.deserialize(itemMap);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deserialize reward item: " + e.getMessage());
            return null;
        }
    }
}
