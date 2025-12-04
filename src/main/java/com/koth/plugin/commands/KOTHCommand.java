package com.koth.plugin.commands;

import com.koth.plugin.KOTH;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KOTHCommand implements CommandExecutor, TabCompleter {
    private final KOTH plugin;

    public KOTHCommand(KOTH plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("koth.admin")) {
            sender.sendMessage(plugin.getConfigManager().colorize("§cYou do not have permission to use this command."));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                handleStart(sender);
                break;
            case "stop":
                handleStop(sender);
                break;
            case "setpos1":
                handleSetPos1(sender);
                break;
            case "setpos2":
                handleSetPos2(sender);
                break;
            case "setreward":
                handleSetReward(sender);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            case "help":
                sendHelp(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleStart(CommandSender sender) {
        if (!plugin.getRegionManager().hasRegion()) {
            sender.sendMessage(plugin.getConfigManager().getMessage("region-not-set"));
            return;
        }

        if (plugin.getKothManager().isActive()) {
            sender.sendMessage(plugin.getConfigManager().colorize("§cKOTH is already active!"));
            return;
        }

        plugin.getKothManager().cancelSchedule();
        plugin.getKothManager().startKOTH();
        sender.sendMessage(plugin.getConfigManager().colorize("§aKOTH has been started!"));
    }

    private void handleStop(CommandSender sender) {
        if (!plugin.getKothManager().isActive()) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-active-koth"));
            return;
        }

        plugin.getKothManager().stopKOTH(false);
        sender.sendMessage(plugin.getConfigManager().getMessage("koth-stopped"));
    }

    private void handleSetPos1(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().colorize("§cThis command can only be used by players!"));
            return;
        }

        Player player = (Player) sender;
        plugin.getRegionManager().setPosition(player, 1);
    }

    private void handleSetPos2(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().colorize("§cThis command can only be used by players!"));
            return;
        }

        Player player = (Player) sender;
        plugin.getRegionManager().setPosition(player, 2);
        // Automatically create region when both positions are set
        plugin.getRegionManager().createRegion(player);
    }

    private void handleSetReward(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().colorize("§cThis command can only be used by players!"));
            return;
        }

        Player player = (Player) sender;
        if (player.getInventory().getItemInMainHand().getType().isAir()) {
            sender.sendMessage(plugin.getConfigManager().colorize("§cYou must be holding an item to set it as a reward!"));
            return;
        }

        plugin.getRewardManager().setReward(player.getInventory().getItemInMainHand());
        sender.sendMessage(plugin.getConfigManager().getMessage("reward-set"));
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().loadConfig();
        plugin.getRegionManager().loadRegion();
        sender.sendMessage(plugin.getConfigManager().getMessage("config-reloaded"));
    }

    private void handleStatus(CommandSender sender) {
        if (plugin.getKothManager().isActive()) {
            Player capturing = plugin.getKothManager().getCapturingPlayer() != null ?
                plugin.getServer().getPlayer(plugin.getKothManager().getCapturingPlayer()) : null;
            
            if (capturing != null) {
                int progress = plugin.getKothManager().getCaptureProgress();
                int total = plugin.getConfigManager().getCaptureTime();
                String message = plugin.getConfigManager().getMessage("status-active")
                    .replace("%player%", capturing.getName())
                    .replace("%progress%", progress + "/" + total + "s");
                sender.sendMessage(message);
            } else {
                sender.sendMessage(plugin.getConfigManager().colorize("§eKOTH is active but no one is capturing!"));
            }
        } else {
            long nextTime = plugin.getKothManager().getNextKothTime();
            if (nextTime > 0) {
                long remaining = (nextTime - System.currentTimeMillis()) / 1000;
                long minutes = remaining / 60;
                long seconds = remaining % 60;
                String time = minutes + "m " + seconds + "s";
                String message = plugin.getConfigManager().getMessage("status-waiting")
                    .replace("%time%", time);
                sender.sendMessage(message);
            } else {
                sender.sendMessage(plugin.getConfigManager().getMessage("status-not-scheduled"));
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().colorize("§6=== KOTH Commands ==="));
        sender.sendMessage(plugin.getConfigManager().colorize("§e/koth start §7- Start a KOTH event"));
        sender.sendMessage(plugin.getConfigManager().colorize("§e/koth stop §7- Stop current KOTH event"));
        sender.sendMessage(plugin.getConfigManager().colorize("§e/koth setpos1 §7- Set first corner of KOTH region"));
        sender.sendMessage(plugin.getConfigManager().colorize("§e/koth setpos2 §7- Set second corner of KOTH region"));
        sender.sendMessage(plugin.getConfigManager().colorize("§e/koth setreward §7- Set reward to item in hand"));
        sender.sendMessage(plugin.getConfigManager().colorize("§e/koth reload §7- Reload configuration"));
        sender.sendMessage(plugin.getConfigManager().colorize("§e/koth status §7- Show KOTH status"));
        sender.sendMessage(plugin.getConfigManager().colorize("§e/koth help §7- Show this help message"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = Arrays.asList("start", "stop", "setpos1", "setpos2", "setreward", "reload", "status", "help");
            List<String> result = new ArrayList<>();
            for (String completion : completions) {
                if (completion.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(completion);
                }
            }
            return result;
        }
        return new ArrayList<>();
    }
}
