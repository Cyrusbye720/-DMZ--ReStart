package io.github.dmzrestart.managers;

import io.github.dmzrestart.DMZRestartPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RestartManager {
    private final DMZRestartPlugin plugin;
    private final Map<String, BukkitTask> scheduledRestarts = new ConcurrentHashMap<>();
    private final List<RestartHistory> restartHistory = new ArrayList<>();
    private BukkitTask mainSchedulerTask;
    private volatile boolean emergencyRestartActive = false;
    private volatile boolean restartInProgress = false;
    private volatile RestartReason currentRestartReason = null;
    private volatile String currentRestartInitiator = null;
    private volatile int remainingSeconds = 0;
    private int totalRestartsManaged = 0;

    public enum RestartReason {
        SCHEDULED("Scheduled Restart"),
        MANUAL("Manual Restart"),
        EMERGENCY_TPS("Emergency TPS"),
        EMERGENCY_MEMORY("Emergency Memory"),
        EMERGENCY_SYSTEM("System Emergency"),
        PLUGIN_REQUEST("Plugin Request");

        private final String displayName;

        RestartReason(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class RestartHistory {
        private final Date timestamp;
        private final RestartReason reason;
        private final String initiator;
        private final String details;

        public RestartHistory(RestartReason reason, String initiator, String details) {
            this.timestamp = new Date();
            this.reason = reason;
            this.initiator = initiator;
            this.details = details;
        }

        public Date getTimestamp() { return timestamp; }
        public RestartReason getReason() { return reason; }
        public String getInitiator() { return initiator; }
        public String getDetails() { return details; }
    }

    public RestartManager(DMZRestartPlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("RestartManager initialized successfully");
    }

    public void initialize() {
        try {
            cleanup();
            startMainScheduler();
            scheduleConfiguredRestarts();

            if (plugin.getLogManager() != null) {
                plugin.getLogManager().info("RestartManager services started successfully");
            }

        } catch (Exception e) {
            if (plugin.getLogManager() != null) {
                plugin.getLogManager().severe("Failed to initialize RestartManager: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void startMainScheduler() {
        mainSchedulerTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkScheduledRestarts();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void scheduleConfiguredRestarts() {
        List<String> restartTimes = plugin.getConfigManager().getRestartTimes();

        for (String timeStr : restartTimes) {
            try {
                LocalTime restartTime = LocalTime.parse(timeStr);
                scheduleNextRestart(restartTime);
            } catch (Exception e) {
                if (plugin.getLogManager() != null) {
                    plugin.getLogManager().warning("Invalid restart time format: " + timeStr);
                }
            }
        }
    }

    private void scheduleNextRestart(LocalTime time) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(plugin.getConfigManager().getTimezone()));
        LocalDateTime nextRestart = now.toLocalDate().atTime(time);

        if (nextRestart.isBefore(now)) {
            nextRestart = nextRestart.plusDays(1);
        }

        String taskId = "scheduled_" + time.toString();
        long delayTicks = java.time.Duration.between(now, nextRestart).getSeconds() * 20;

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                executeRestart(RestartReason.SCHEDULED, "System", 
                    "Scheduled restart at " + time.format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }.runTaskLater(plugin, delayTicks);

        scheduledRestarts.put(taskId, task);
    }

    private void checkScheduledRestarts() {
        scheduledRestarts.entrySet().removeIf(entry -> entry.getValue().isCancelled());
    }

    public void scheduleRestart(int delaySeconds, RestartReason reason, String initiator) {
        if (emergencyRestartActive && reason != RestartReason.MANUAL) {
            return;
        }

        // FIXED: Make variables effectively final for lambda
        final RestartReason finalReason = reason;
        final String finalInitiator = initiator;
        final String details = "Delayed restart after " + delaySeconds + " seconds";

        String taskId = "restart_" + System.currentTimeMillis();

        if (reason.name().contains("EMERGENCY")) {
            emergencyRestartActive = true;
        }

        restartInProgress = true;
        currentRestartReason = reason;
        currentRestartInitiator = initiator;
        remainingSeconds = delaySeconds;

        if (plugin.getConfigManager().isWarningsEnabled() && delaySeconds > 10) {
            startWarningSequence(delaySeconds, reason);
        }

        // FIXED: Use final variables in lambda
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                executeRestart(finalReason, finalInitiator, details);
            }
        }.runTaskLater(plugin, delaySeconds * 20L);

        scheduledRestarts.put(taskId, task);
        startCountdownTimer(delaySeconds);
    }

    private void startCountdownTimer(int totalSeconds) {
        new BukkitRunnable() {
            int remaining = totalSeconds;

            @Override
            public void run() {
                if (remaining <= 0 || !restartInProgress) {
                    cancel();
                    return;
                }

                remainingSeconds = remaining;
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void startWarningSequence(int totalDelay, RestartReason reason) {
        List<Integer> intervals = plugin.getConfigManager().getWarningIntervals();

        for (int interval : intervals) {
            if (interval < totalDelay) {
                int warningDelay = totalDelay - interval;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        String timeStr = formatTime(interval);
                        String message = plugin.getConfigManager()
                            .getMessage("restart-warning")
                            .replace("{time}", timeStr)
                            .replace("{reason}", reason.getDisplayName());

                        if (plugin.getLogManager() != null) {
                            plugin.getLogManager().broadcast(message);
                        }
                    }
                }.runTaskLater(plugin, warningDelay * 20L);
            }
        }
    }

    private String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            String result = hours + " hour" + (hours != 1 ? "s" : "");
            if (minutes > 0) {
                result += " " + minutes + " minute" + (minutes != 1 ? "s" : "");
            }
            return result;
        }
    }

    private void executeRestart(RestartReason reason, String initiator, String details) {
        try {
            addToHistory(reason, initiator, details);
            totalRestartsManaged++;

            if (plugin.getLogManager() != null) {
                plugin.getLogManager().info("Executing server restart: " + reason.getDisplayName());
            }

            String finalMessage = plugin.getConfigManager()
                .getMessage("restart-now")
                .replace("{reason}", reason.getDisplayName());

            if (plugin.getLogManager() != null) {
                plugin.getLogManager().broadcast(finalMessage);
            }

            restartInProgress = false;
            currentRestartReason = null;
            currentRestartInitiator = null;
            remainingSeconds = 0;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (reason.name().contains("EMERGENCY")) {
                        Bukkit.getServer().shutdown();
                    } else {
                        Bukkit.getServer().reload();
                    }
                }
            }.runTaskLater(plugin, 40L);

        } catch (Exception e) {
            if (plugin.getLogManager() != null) {
                plugin.getLogManager().severe("Failed to execute restart: " + e.getMessage());
            }
        }
    }

    private void addToHistory(RestartReason reason, String initiator, String details) {
        restartHistory.add(new RestartHistory(reason, initiator, details));

        if (restartHistory.size() > 50) {
            restartHistory.remove(0);
        }
    }

    public void cancelAllRestarts() {
        scheduledRestarts.values().forEach(BukkitTask::cancel);
        scheduledRestarts.clear();
        emergencyRestartActive = false;
        restartInProgress = false;
        currentRestartReason = null;
        currentRestartInitiator = null;
        remainingSeconds = 0;
    }

    public void cleanup() {
        if (mainSchedulerTask != null) {
            mainSchedulerTask.cancel();
            mainSchedulerTask = null;
        }

        cancelAllRestarts();
    }

    // Getters
    public LocalDateTime getNextScheduledRestart() {
        return null; // Simplified
    }

    public List<RestartHistory> getRestartHistory() {
        return new ArrayList<>(restartHistory);
    }

    public int getTotalRestartsManaged() {
        return totalRestartsManaged;
    }

    public boolean isEmergencyRestartActive() {
        return emergencyRestartActive;
    }

    // FIXED: Added missing methods for PlaceholderIntegration
    public boolean isRestartInProgress() {
        return restartInProgress;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public RestartReason getCurrentRestartReason() {
        return currentRestartReason;
    }

    public String getRestartInitiator() {
        return currentRestartInitiator;
    }
}