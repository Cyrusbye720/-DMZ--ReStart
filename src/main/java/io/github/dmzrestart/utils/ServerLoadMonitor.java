package io.github.dmzrestart.utils;

import io.github.dmzrestart.DMZRestartPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class ServerLoadMonitor {
    private final DMZRestartPlugin plugin;
    private BukkitTask monitoringTask;
    private double lastTPS = 20.0;
    private double lastMemoryUsage = 0.0;
    private boolean isHealthy = true;
    private int totalChecks = 0;
    private int emergencyTriggered = 0;

    public ServerLoadMonitor(DMZRestartPlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("ServerLoadMonitor initialized successfully");
    }

    public void startMonitoring() {
        if (monitoringTask != null) {
            monitoringTask.cancel();
        }

        int interval = plugin.getConfigManager().getCheckInterval();

        monitoringTask = new BukkitRunnable() {
            @Override
            public void run() {
                performHealthCheck();
            }
        }.runTaskTimer(plugin, 20L * interval, 20L * interval);

        if (plugin.getLogManager() != null) {
            plugin.getLogManager().info("Performance monitoring started (interval: " + interval + "s)");
        }
    }

    public void stopMonitoring() {
        if (monitoringTask != null) {
            monitoringTask.cancel();
            monitoringTask = null;
        }

        if (plugin.getLogManager() != null) {
            plugin.getLogManager().info("Performance monitoring stopped");
        }
    }

    private void performHealthCheck() {
        totalChecks++;

        // Calculate TPS (simplified)
        try {
            double[] tps = Bukkit.getTPS();
            if (tps != null && tps.length > 0) {
                lastTPS = tps[0];
            }
        } catch (Exception e) {
            lastTPS = 20.0; // Fallback
        }

        // Calculate memory usage
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long used = memoryBean.getHeapMemoryUsage().getUsed();
            long max = memoryBean.getHeapMemoryUsage().getMax();
            lastMemoryUsage = (double) used / max * 100.0;
        } catch (Exception e) {
            lastMemoryUsage = 0.0; // Fallback
        }

        // Health check
        double tpsThreshold = plugin.getConfigManager().getTpsThreshold();
        double memoryThreshold = plugin.getConfigManager().getMemoryThreshold();

        isHealthy = lastTPS >= tpsThreshold && lastMemoryUsage <= memoryThreshold;

        // Emergency check
        if (plugin.getConfigManager().getConfig().getBoolean("emergency.enabled", true)) {
            double emergencyTps = plugin.getConfigManager().getConfig().getDouble("emergency.tps-threshold", 12.0);
            double emergencyMemory = plugin.getConfigManager().getConfig().getDouble("emergency.memory-threshold", 95.0);

            if (lastTPS < emergencyTps || lastMemoryUsage > emergencyMemory) {
                triggerEmergencyRestart();
            }
        }

        // Debug logging
        if (plugin.getLogManager() != null && plugin.getLogManager().isDebugMode()) {
            plugin.getLogManager().debug(String.format("Health Check - TPS: %.2f, Memory: %.1f%%, Healthy: %s", 
                lastTPS, lastMemoryUsage, isHealthy ? "Yes" : "No"));
        }
    }

    private void triggerEmergencyRestart() {
        emergencyTriggered++;

        String reason = lastTPS < plugin.getConfigManager().getConfig().getDouble("emergency.tps-threshold", 12.0) 
            ? "Critical TPS: " + String.format("%.2f", lastTPS)
            : "Critical Memory: " + String.format("%.1f%%", lastMemoryUsage);

        if (plugin.getLogManager() != null) {
            plugin.getLogManager().severe("Emergency restart triggered: " + reason);
        }

        if (plugin.getRestartManager() != null) {
            int delay = plugin.getConfigManager().getConfig().getInt("emergency.delay", 30);
            plugin.getRestartManager().scheduleRestart(delay, 
                plugin.getRestartManager().RestartReason.EMERGENCY_SYSTEM, 
                "Performance Monitor");
        }
    }

    // Getters
    public double getLastTPS() {
        return lastTPS;
    }

    public double getLastMemoryUsage() {
        return lastMemoryUsage;
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public int getTotalChecks() {
        return totalChecks;
    }

    public int getEmergencyTriggered() {
        return emergencyTriggered;
    }
}