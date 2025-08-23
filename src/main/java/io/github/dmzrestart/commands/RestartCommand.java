package io.github.dmzrestart.commands;

import io.github.dmzrestart.DMZRestartPlugin;
import io.github.dmzrestart.managers.RestartManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestartCommand implements CommandExecutor, TabCompleter {
    private final DMZRestartPlugin plugin;

    public RestartCommand(DMZRestartPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "status":
                showStatus(sender);
                return true;

            case "info":
                showInfo(sender);
                return true;

            case "restart":
                handleRestart(sender, args);
                return true;

            case "schedule":
                handleSchedule(sender, args);
                return true;

            case "cancel":
                handleCancel(sender);
                return true;

            case "reload":
                handleReload(sender);
                return true;

            default:
                plugin.getLogManager().sendMessage(sender, "&cUnknown subcommand. Use /dmzrestart help");
                return true;
        }
    }

    private void showHelp(CommandSender sender) {
        plugin.getLogManager().sendMessage(sender, "&6DMZ ReStart Commands:");
        plugin.getLogManager().sendMessage(sender, "&e/dmzrestart help &7- Show this help message");
        plugin.getLogManager().sendMessage(sender, "&e/dmzrestart status &7- Show restart status");
        plugin.getLogManager().sendMessage(sender, "&e/dmzrestart info &7- Show plugin information");

        if (plugin.getPermissionManager().canRestart(sender)) {
            plugin.getLogManager().sendMessage(sender, "&e/dmzrestart restart [delay] &7- Schedule server restart");
        }

        if (plugin.getPermissionManager().canSchedule(sender)) {
            plugin.getLogManager().sendMessage(sender, "&e/dmzrestart schedule <seconds> &7- Schedule restart with delay");
        }

        if (plugin.getPermissionManager().canCancel(sender)) {
            plugin.getLogManager().sendMessage(sender, "&e/dmzrestart cancel &7- Cancel scheduled restarts");
        }

        if (plugin.getPermissionManager().canReload(sender)) {
            plugin.getLogManager().sendMessage(sender, "&e/dmzrestart reload &7- Reload configuration");
        }
    }

    private void showStatus(CommandSender sender) {
        plugin.getLogManager().sendMessage(sender, "&6DMZ ReStart Status:");
        plugin.getLogManager().sendMessage(sender, "&7Plugin Version: &a" + plugin.getDescription().getVersion());
        plugin.getLogManager().sendMessage(sender, "&7Status: &a" + (plugin.isFullyInitialized() ? "Active" : "Initializing"));

        long uptime = plugin.getPluginUptime() / (1000 * 60);
        plugin.getLogManager().sendMessage(sender, "&7Uptime: &a" + uptime + " minutes");

        if (plugin.getRestartManager() != null) {
            boolean restartActive = plugin.getRestartManager().isRestartInProgress();
            plugin.getLogManager().sendMessage(sender, "&7Restart Active: " + (restartActive ? "&cYes" : "&aNo"));

            if (restartActive) {
                int remaining = plugin.getRestartManager().getRemainingSeconds();
                plugin.getLogManager().sendMessage(sender, "&7Time Remaining: &e" + remaining + " seconds");
            }

            plugin.getLogManager().sendMessage(sender, "&7Restarts Managed: &a" + plugin.getRestartManager().getTotalRestartsManaged());
        }
    }

    private void showInfo(CommandSender sender) {
        plugin.getLogManager().sendMessage(sender, "&6DMZ ReStart v" + plugin.getDescription().getVersion());
        plugin.getLogManager().sendMessage(sender, "&7Professional Minecraft Server Management");
        plugin.getLogManager().sendMessage(sender, "&7GitHub: &ahttps://github.com/YourUsername/DMZ-ReStart");
        plugin.getLogManager().sendMessage(sender, "&7Support: &ahttps://github.com/YourUsername/DMZ-ReStart/issues");
    }

    private void handleRestart(CommandSender sender, String[] args) {
        if (!plugin.getPermissionManager().canRestart(sender)) {
            plugin.getPermissionManager().sendInsufficientPermissionMessage(sender);
            return;
        }

        int delay = 30; // Default 30 seconds
        if (args.length > 1) {
            try {
                delay = Integer.parseInt(args[1]);
                if (delay < 0 || delay > 3600) {
                    plugin.getLogManager().sendMessage(sender, "&cDelay must be between 0 and 3600 seconds!");
                    return;
                }
            } catch (NumberFormatException e) {
                plugin.getLogManager().sendMessage(sender, "&cInvalid delay value! Must be a number.");
                return;
            }
        }

        plugin.getRestartManager().scheduleRestart(delay, RestartManager.RestartReason.MANUAL, sender.getName());
        plugin.getLogManager().sendMessage(sender, "&aServer restart scheduled for " + delay + " seconds!");
    }

    private void handleSchedule(CommandSender sender, String[] args) {
        if (!plugin.getPermissionManager().canSchedule(sender)) {
            plugin.getPermissionManager().sendInsufficientPermissionMessage(sender);
            return;
        }

        if (args.length < 2) {
            plugin.getLogManager().sendMessage(sender, "&cUsage: /dmzrestart schedule <seconds>");
            return;
        }

        try {
            int delay = Integer.parseInt(args[1]);
            if (delay < 1 || delay > 86400) {
                plugin.getLogManager().sendMessage(sender, "&cDelay must be between 1 and 86400 seconds!");
                return;
            }

            plugin.getRestartManager().scheduleRestart(delay, RestartManager.RestartReason.MANUAL, sender.getName());
            plugin.getLogManager().sendMessage(sender, "&aRestart scheduled for " + delay + " seconds!");
        } catch (NumberFormatException e) {
            plugin.getLogManager().sendMessage(sender, "&cInvalid number format!");
        }
    }

    private void handleCancel(CommandSender sender) {
        if (!plugin.getPermissionManager().canCancel(sender)) {
            plugin.getPermissionManager().sendInsufficientPermissionMessage(sender);
            return;
        }

        plugin.getRestartManager().cancelAllRestarts();
        plugin.getLogManager().sendMessage(sender, "&aAll scheduled restarts have been cancelled!");
    }

    private void handleReload(CommandSender sender) {
        if (!plugin.getPermissionManager().canReload(sender)) {
            plugin.getPermissionManager().sendInsufficientPermissionMessage(sender);
            return;
        }

        plugin.getLogManager().sendMessage(sender, "&eReloading DMZ ReStart configuration...");
        plugin.reloadPlugin();
        plugin.getLogManager().sendMessage(sender, "&aConfiguration reloaded successfully!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = Arrays.asList("help", "status", "info");

            if (plugin.getPermissionManager().canRestart(sender)) {
                commands = new ArrayList<>(commands);
                commands.add("restart");
            }

            if (plugin.getPermissionManager().canSchedule(sender)) {
                commands = new ArrayList<>(commands);
                commands.add("schedule");
            }

            if (plugin.getPermissionManager().canCancel(sender)) {
                commands = new ArrayList<>(commands);
                commands.add("cancel");
            }

            if (plugin.getPermissionManager().canReload(sender)) {
                commands = new ArrayList<>(commands);
                commands.add("reload");
            }

            String partial = args[0].toLowerCase();
            for (String cmd : commands) {
                if (cmd.startsWith(partial)) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("restart") || args[0].equalsIgnoreCase("schedule"))) {
            completions.addAll(Arrays.asList("30", "60", "300", "600"));
        }

        return completions;
    }
}
