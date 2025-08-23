package io.github.dmzrestart.managers;

import io.github.dmzrestart.DMZRestartPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionManager {
    private final DMZRestartPlugin plugin;

    // Permission constants
    public static final String ADMIN = "dmzrestart.admin";
    public static final String RESTART = "dmzrestart.restart";
    public static final String SCHEDULE = "dmzrestart.schedule";
    public static final String CANCEL = "dmzrestart.cancel";
    public static final String RELOAD = "dmzrestart.reload";
    public static final String STATUS = "dmzrestart.status";
    public static final String BYPASS_RESTART = "dmzrestart.bypass";
    public static final String ALERTS = "dmzrestart.alerts";

    public PermissionManager(DMZRestartPlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("PermissionManager initialized successfully");
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission);
    }

    public boolean isAdmin(CommandSender sender) {
        return hasPermission(sender, ADMIN);
    }

    public boolean canRestart(CommandSender sender) {
        return hasPermission(sender, RESTART) || isAdmin(sender);
    }

    public boolean canSchedule(CommandSender sender) {
        return hasPermission(sender, SCHEDULE) || isAdmin(sender);
    }

    public boolean canCancel(CommandSender sender) {
        return hasPermission(sender, CANCEL) || isAdmin(sender);
    }

    public boolean canReload(CommandSender sender) {
        return hasPermission(sender, RELOAD) || isAdmin(sender);
    }

    public boolean canBypassRestart(CommandSender sender) {
        return hasPermission(sender, BYPASS_RESTART) || isAdmin(sender);
    }

    public boolean shouldReceiveAlerts(CommandSender sender) {
        return hasPermission(sender, ALERTS);
    }

    public void sendNoPermissionMessage(CommandSender sender, String permission) {
        if (plugin.getLogManager() != null) {
            plugin.getLogManager().sendMessage(sender, "&cYou don't have permission: " + permission);
        }
    }

    public void sendInsufficientPermissionMessage(CommandSender sender) {
        if (plugin.getLogManager() != null) {
            plugin.getLogManager().sendMessage(sender, "&cInsufficient permissions for this command!");
        }
    }

    public void initializeLuckPerms() {
        // LuckPerms integration can be added here
        if (plugin.getLogManager() != null) {
            plugin.getLogManager().info("LuckPerms integration initialized");
        }
    }
}