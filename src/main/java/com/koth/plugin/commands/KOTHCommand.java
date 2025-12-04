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
            case "setregion":
                handleSetRegion(sender, args);
                break;
            case "setreward":
                handleSetReward(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "status":
                handleStatus(sender);
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

    private void handleSetRegion(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().colorize("§cThis command can only be used by players!"));
            return;
        }

        Player player = (Player) sender;
        
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().colorize("§cUsage: /koth setregion <pos1|pos2>"));
            return;
        }
        
        String position = args[1].toLowerCase();
        if (position.equals("pos1") || position.equals("1")) {
            plugin.getRegionManager().setPosition(player, 1);
        } else if (position.equals("pos2") || position.equals("2")) {
            plugin.getRegionManager().setPosition(player, 2);
            plugin.getRegionManager().createRegion(player);
        } else {
            sender.sendMessage(plugin.getConfigManager().colorize("§cUsage: /koth setregion <pos1|pos2>"));
        }
    }

    private void handleSetReward(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().colorize("§cUsage: /koth setreward <command>"));
            return;
        }

        StringBuilder rewardCommand = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) rewardCommand.append(" ");
            rewardCommand.append(args[i]);
        }

        plugin.getRewardManager().setReward(rewardCommand.toString());
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
        sender.sendMessage(plugin.getConfigManager().colorize("§e/koth setregion pos1 §7- Set first corner of KOTH region"));
        sender.sendMessage(plugin.getConfigManager().colorize("§e/koth setregion pos2 §7- Set second corner of KOTH region"));
        sender.sendMessage(plugin.getConfigManager().colorize("§e/koth setreward <reward> §7- Set reward command"));
        sender.sendMessage(plugin.getConfigManager().colorize("§e/koth reload §7- Reload configuration"));
        sender.sendMessage(plugin.getConfigManager().colorize("§e/koth status §7- Show KOTH status"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = Arrays.asList("start", "stop", "setregion", "setreward", "reload", "status");
            List<String> result = new ArrayList<>();
            for (String completion : completions) {
                if (completion.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(completion);
                }
            }
            return result;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("setregion")) {
            List<String> completions = Arrays.asList("pos1", "pos2");
            List<String> result = new ArrayList<>();
            for (String completion : completions) {
                if (completion.toLowerCase().startsWith(args[1].toLowerCase())) {
                    result.add(completion);
                }
            }
            return result;
        }
        return new ArrayList<>();
    }
}
