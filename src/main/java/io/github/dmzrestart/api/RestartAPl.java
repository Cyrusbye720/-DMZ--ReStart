package io.github.dmzrestart.api;

import io.github.dmzrestart.DMZRestartPlugin;
import io.github.dmzrestart.managers.RestartManager;
import java.time.LocalDateTime;
import java.util.List;

public class RestartAPI {
    private final DMZRestartPlugin plugin;
    private static RestartAPI instance;

    public RestartAPI(DMZRestartPlugin plugin) {
        this.plugin = plugin;
        instance = this;
        plugin.getLogger().info("RestartAPI initialized successfully");
    }

    public static RestartAPI getInstance() {
        return instance;
    }

    public void scheduleRestart(int delaySeconds, String reason, String initiator) {
        if (plugin.getRestartManager() != null) {
            plugin.getRestartManager().scheduleRestart(delaySeconds, 
                RestartManager.RestartReason.PLUGIN_REQUEST, initiator);
        }
    }

    public boolean isServerHealthy() {
        if (plugin.getServerLoadMonitor() != null) {
            return plugin.getServerLoadMonitor().isHealthy();
        }
        return true;
    }

    public double getCurrentTPS() {
        if (plugin.getServerLoadMonitor() != null) {
            return plugin.getServerLoadMonitor().getLastTPS();
        }
        return 20.0;
    }

    public LocalDateTime getNextRestartTime() {
        if (plugin.getRestartManager() != null) {
            return plugin.getRestartManager().getNextScheduledRestart();
        }
        return null;
    }

    public List<RestartManager.RestartHistory> getRestartHistory() {
        if (plugin.getRestartManager() != null) {
            return plugin.getRestartManager().getRestartHistory();
        }
        return null;
    }

    public boolean isRestartInProgress() {
        if (plugin.getRestartManager() != null) {
            return plugin.getRestartManager().isRestartInProgress();
        }
        return false;
    }
}
