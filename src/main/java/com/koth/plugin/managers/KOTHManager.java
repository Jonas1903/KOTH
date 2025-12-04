package com.koth.plugin.managers;

import com.koth.plugin.KOTH;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KOTHManager {
    private final KOTH plugin;
    private boolean active = false;
    private UUID capturingPlayer = null;
    private int captureProgress = 0;
    private BukkitTask captureTask = null;
    private BukkitTask scheduleTask = null;
    private long nextKothTime = 0;
    private final Map<UUID, Long> playerTimeInZone = new HashMap<>();

    public KOTHManager(KOTH plugin) {
        this.plugin = plugin;
    }

    public void startKOTH() {
        if (active) {
            return;
        }

        if (!plugin.getRegionManager().hasRegion()) {
            return;
        }

        active = true;
        captureProgress = 0;
        capturingPlayer = null;
        playerTimeInZone.clear();

        // Announce start
        Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("koth-started"));

        // Create boss bar
        plugin.getBossBarManager().createBossBar(
            plugin.getConfigManager().colorize("§fKOTH: Waiting for players...")
        );

        // Start capture check task (runs every second)
        captureTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkCapture, 20L, 20L);
    }

    public void stopKOTH(boolean withWinner) {
        if (!active) {
            return;
        }

        active = false;
        
        if (captureTask != null) {
            captureTask.cancel();
            captureTask = null;
        }

        Player winner = null;
        if (withWinner && capturingPlayer != null) {
            winner = Bukkit.getPlayer(capturingPlayer);
        }

        if (winner != null) {
            Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("koth-ended")
                .replace("%player%", winner.getName()));
            plugin.getRewardManager().giveReward(winner);
        } else {
            Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("koth-no-winner"));
        }

        plugin.getBossBarManager().removeBossBar();
        capturingPlayer = null;
        captureProgress = 0;
        playerTimeInZone.clear();

        // Schedule next KOTH
        scheduleNextKOTH();
    }

    private void checkCapture() {
        if (!active) {
            return;
        }

        // Find players in the region
        UUID oldCapturing = capturingPlayer;
        UUID newCapturing = null;
        long maxTime = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getRegionManager().isInRegion(player.getLocation())) {
                UUID playerId = player.getUniqueId();
                long currentTime = playerTimeInZone.getOrDefault(playerId, 0L);
                currentTime += 1; // Add 1 second
                playerTimeInZone.put(playerId, currentTime);

                if (currentTime > maxTime) {
                    maxTime = currentTime;
                    newCapturing = playerId;
                }
            }
        }

        // Reset times for players not in zone
        playerTimeInZone.entrySet().removeIf(entry -> {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !plugin.getRegionManager().isInRegion(player.getLocation())) {
                if (entry.getKey().equals(capturingPlayer) && player != null) {
                    Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("player-left-area")
                        .replace("%player%", player.getName()));
                }
                return true;
            }
            return false;
        });

        capturingPlayer = newCapturing;

        if (capturingPlayer != null) {
            Player player = Bukkit.getPlayer(capturingPlayer);
            if (player != null) {
                captureProgress = (int) playerTimeInZone.get(capturingPlayer);
                int captureTime = plugin.getConfigManager().getCaptureTime();
                
                double progress = (double) captureProgress / captureTime;
                String title = plugin.getConfigManager().colorize(
                    "§f" + player.getName() + " is capturing! " + captureProgress + "/" + captureTime + "s"
                );
                plugin.getBossBarManager().updateBossBar(title, progress);

                // Check if capture is complete
                if (captureProgress >= captureTime) {
                    stopKOTH(true);
                }
            }
        } else {
            captureProgress = 0;
            plugin.getBossBarManager().updateBossBar(
                plugin.getConfigManager().colorize("§fKOTH: Waiting for players..."),
                0.0
            );
        }
    }

    public void scheduleNextKOTH() {
        if (scheduleTask != null) {
            scheduleTask.cancel();
        }

        int intervalMinutes = plugin.getConfigManager().getEventInterval();
        nextKothTime = System.currentTimeMillis() + (intervalMinutes * 60 * 1000L);

        // Get announcement times
        List<Integer> announcements = plugin.getConfigManager().getConfig().getIntegerList("announcements");
        
        // Schedule announcements
        for (int minutes : announcements) {
            if (minutes < intervalMinutes) {
                long delay = (intervalMinutes - minutes) * 60 * 20L; // Convert to ticks
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!active) {
                        String time = minutes == 1 ? "1 minute" : minutes + " minutes";
                        Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("koth-starting")
                            .replace("%time%", time));
                    }
                }, delay);
            }
        }

        // Schedule the actual start
        scheduleTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!active) {
                startKOTH();
            }
        }, intervalMinutes * 60 * 20L);
    }

    public void cancelSchedule() {
        if (scheduleTask != null) {
            scheduleTask.cancel();
            scheduleTask = null;
        }
        nextKothTime = 0;
    }

    public boolean isActive() {
        return active;
    }

    public UUID getCapturingPlayer() {
        return capturingPlayer;
    }

    public int getCaptureProgress() {
        return captureProgress;
    }

    public long getNextKothTime() {
        return nextKothTime;
    }

    public void shutdown() {
        if (captureTask != null) {
            captureTask.cancel();
        }
        if (scheduleTask != null) {
            scheduleTask.cancel();
        }
        plugin.getBossBarManager().removeBossBar();
    }
}
