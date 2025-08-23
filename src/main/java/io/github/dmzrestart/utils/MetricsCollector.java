package io.github.dmzrestart.utils;

import io.github.dmzrestart.DMZRestartPlugin;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MetricsCollector {
    private final DMZRestartPlugin plugin;
    private final AtomicInteger commandsExecuted = new AtomicInteger(0);
    private File metricsFile;

    public static final String COMMANDS_EXECUTED = "commands_executed";

    public MetricsCollector(DMZRestartPlugin plugin) {
        this.plugin = plugin;
        initializeMetricsFile();
        plugin.getLogger().info("MetricsCollector initialized successfully");
    }

    private void initializeMetricsFile() {
        try {
            File metricsDir = new File(plugin.getDataFolder(), "metrics");
            if (!metricsDir.exists()) {
                metricsDir.mkdirs();
            }

            String dateStr = new SimpleDateFormat("yyyy-MM").format(new Date());
            metricsFile = new File(metricsDir, "metrics-" + dateStr + ".csv");

            if (!metricsFile.exists()) {
                metricsFile.createNewFile();
                // Write CSV header
                try (FileWriter writer = new FileWriter(metricsFile, true)) {
                    writer.write("timestamp,metric,value\n");
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to initialize metrics file: " + e.getMessage());
        }
    }

    public void initialize() {
        if (plugin.getLogManager() != null) {
            plugin.getLogManager().info("Metrics collection started");
        }
    }

    public void incrementMetric(String metricName) {
        if (COMMANDS_EXECUTED.equals(metricName)) {
            commandsExecuted.incrementAndGet();
        }

        recordMetric(metricName, "1");
    }

    private void recordMetric(String metricName, String value) {
        if (metricsFile == null) return;

        try (FileWriter writer = new FileWriter(metricsFile, true)) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.write(String.format("%s,%s,%s\n", timestamp, metricName, value));
            writer.flush();
        } catch (IOException e) {
            // Silent fail to prevent log spam
        }
    }

    public int getCommandsExecuted() {
        return commandsExecuted.get();
    }

    public void shutdown() {
        if (plugin.getLogManager() != null) {
            plugin.getLogManager().info("Metrics collection stopped");
        }
    }
}