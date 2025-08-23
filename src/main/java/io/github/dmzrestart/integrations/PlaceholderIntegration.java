package io.github.dmzrestart.integrations;

import io.github.dmzrestart.DMZRestartPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;

public class PlaceholderIntegration extends PlaceholderExpansion {
    private final DMZRestartPlugin plugin;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public PlaceholderIntegration(DMZRestartPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "dmzrestart";
    }

    @Override
    public @NotNull String getAuthor() {
        return "DMZ Development";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return plugin.isPluginEnabled();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        try {
            switch (identifier.toLowerCase()) {
                case "version":
                    return plugin.getDescription().getVersion();

                case "uptime":
                    return formatUptime(plugin.getPluginUptime());

                case "status":
                    return plugin.isFullyInitialized() ? "Active" : "Initializing";

                // FIXED: All restart information placeholders now work
                case "restart_in_progress":
                    if (plugin.getRestartManager() != null && plugin.getRestartManager().isRestartInProgress()) {
                        return "Yes";
                    }
                    return "No";

                case "restart_remaining_seconds":
                    if (plugin.getRestartManager() != null) {
                        int remaining = plugin.getRestartManager().getRemainingSeconds();
                        return String.valueOf(remaining);
                    }
                    return "0";

                case "restart_reason":
                    if (plugin.getRestartManager() != null && plugin.getRestartManager().isRestartInProgress()) {
                        if (plugin.getRestartManager().getCurrentRestartReason() != null) {
                            return plugin.getRestartManager().getCurrentRestartReason().getDisplayName();
                        }
                    }
                    return "None";

                case "restart_initiator":
                    if (plugin.getRestartManager() != null && plugin.getRestartManager().isRestartInProgress()) {
                        String initiator = plugin.getRestartManager().getRestartInitiator();
                        return initiator != null ? initiator : "Unknown";
                    }
                    return "None";

                case "emergency_active":
                    if (plugin.getRestartManager() != null) {
                        return plugin.getRestartManager().isEmergencyRestartActive() ? "Yes" : "No";
                    }
                    return "No";

                case "restarts_managed":
                    if (plugin.getRestartManager() != null) {
                        return String.valueOf(plugin.getRestartManager().getTotalRestartsManaged());
                    }
                    return "0";

                // Performance data
                case "tps":
                    if (plugin.getServerLoadMonitor() != null) {
                        return String.format("%.2f", plugin.getServerLoadMonitor().getLastTPS());
                    }
                    return "20.00";

                case "memory_usage":
                    if (plugin.getServerLoadMonitor() != null) {
                        return String.format("%.1f", plugin.getServerLoadMonitor().getLastMemoryUsage());
                    }
                    return "0.0";

                case "server_healthy":
                    if (plugin.getServerLoadMonitor() != null) {
                        return plugin.getServerLoadMonitor().isHealthy() ? "Yes" : "No";
                    }
                    return "Yes";

                case "debug_mode":
                    if (plugin.getConfigManager() != null) {
                        return plugin.getConfigManager().isDebugMode() ? "Enabled" : "Disabled";
                    }
                    return "Unknown";

                case "timezone":
                    if (plugin.getConfigManager() != null) {
                        return plugin.getConfigManager().getTimezone();
                    }
                    return ZoneId.systemDefault().getId();

                case "player_bypass":
                    if (player != null && plugin.getPermissionManager() != null) {
                        return plugin.getPermissionManager().canBypassRestart(player) ? "Yes" : "No";
                    }
                    return "Unknown";

                case "player_admin":
                    if (player != null && plugin.getPermissionManager() != null) {
                        return plugin.getPermissionManager().isAdmin(player) ? "Yes" : "No";
                    }
                    return "Unknown";

                default:
                    return null;
            }
        } catch (Exception e) {
            if (plugin.getLogManager() != null) {
                plugin.getLogManager().debug("PlaceholderAPI error for '" + identifier + "': " + e.getMessage());
            }
            return "Error";
        }
    }

    private String formatUptime(long uptimeMs) {
        long days = uptimeMs / (1000 * 60 * 60 * 24);
        long hours = (uptimeMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (uptimeMs % (1000 * 60 * 60)) / (1000 * 60);

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
}
