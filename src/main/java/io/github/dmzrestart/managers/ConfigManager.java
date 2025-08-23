package io.github.dmzrestart.managers;

import io.github.dmzrestart.DMZRestartPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;
import java.util.Arrays;

public class ConfigManager {
    private final DMZRestartPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    private File dataFolder;

    public ConfigManager(DMZRestartPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.configFile = new File(dataFolder, "config.yml");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
            plugin.getLogger().info("Created plugin data directory");
        }

        plugin.getLogger().info("ConfigManager initialized successfully");
    }

    public void loadConfig() {
        try {
            if (!configFile.exists()) {
                createDefaultConfig();
            }

            config = YamlConfiguration.loadConfiguration(configFile);

            InputStream defaultConfigStream = plugin.getResource("config.yml");
            if (defaultConfigStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new java.io.InputStreamReader(defaultConfigStream));
                config.setDefaults(defaultConfig);
            }

            plugin.getLogger().info("Configuration loaded successfully");

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load configuration: " + e.getMessage());
            throw new RuntimeException("Configuration loading failed", e);
        }
    }

    private void createDefaultConfig() {
        try {
            plugin.getLogger().info("Creating default configuration file...");

            if (plugin.getResource("config.yml") != null) {
                plugin.saveDefaultConfig();
                plugin.getLogger().info("Default configuration created from resources");
            } else {
                createBasicConfig();
                plugin.getLogger().info("Basic configuration created");
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create default config: " + e.getMessage());
            createBasicConfig();
        }
    }

    private void createBasicConfig() {
        try {
            configFile.createNewFile();

            YamlConfiguration basicConfig = new YamlConfiguration();

            basicConfig.set("debug", false);
            basicConfig.set("timezone", TimeZone.getDefault().getID());
            basicConfig.set("backup-config-on-reload", true);

            basicConfig.set("restart-times", Arrays.asList(
                "04:00", "12:00", "20:00"
            ));

            basicConfig.set("monitoring.enabled", true);
            basicConfig.set("monitoring.check-interval", 30);
            basicConfig.set("monitoring.tps-threshold", 16.0);
            basicConfig.set("monitoring.memory-threshold", 85.0);
            basicConfig.set("monitoring.consecutive-checks", 3);
            basicConfig.set("monitoring.log-performance", false);
            basicConfig.set("monitoring.debug-log-interval-hours", 5);

            basicConfig.set("emergency.enabled", true);
            basicConfig.set("emergency.delay", 30);
            basicConfig.set("emergency.tps-threshold", 12.0);
            basicConfig.set("emergency.memory-threshold", 95.0);

            basicConfig.set("metrics.enabled", true);
            basicConfig.set("integrations.placeholderapi.enabled", true);
            basicConfig.set("integrations.luckperms.enabled", true);

            basicConfig.set("warnings.enabled", true);
            basicConfig.set("warnings.intervals", Arrays.asList(300, 180, 60, 30, 10, 5, 3, 2, 1));
            basicConfig.set("warnings.sound-enabled", true);

            basicConfig.set("messages.restart-warning", "&e&l[WARNING] Server restart in {time}!");
            basicConfig.set("messages.restart-now", "&c&l[RESTART] Server restarting NOW!");
            basicConfig.set("messages.emergency-restart", "&4&l[EMERGENCY] Emergency restart initiated!");

            basicConfig.save(configFile);
            plugin.getLogger().info("Basic configuration file created with defaults");

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create basic config file: " + e.getMessage());
        }
    }

    public boolean validateConfig() {
        try {
            if (config == null) {
                plugin.getLogger().severe("Configuration is null - cannot validate");
                return false;
            }

            boolean valid = true;

            List<String> restartTimes = config.getStringList("restart-times");
            for (String time : restartTimes) {
                if (!isValidTimeFormat(time)) {
                    plugin.getLogger().warning("Invalid time format: " + time + " (should be HH:MM)");
                    valid = false;
                }
            }

            double tpsThreshold = config.getDouble("monitoring.tps-threshold", 16.0);
            if (tpsThreshold < 1.0 || tpsThreshold > 20.0) {
                plugin.getLogger().warning("TPS threshold should be between 1.0 and 20.0, got: " + tpsThreshold);
                valid = false;
            }

            if (valid) {
                plugin.getLogger().info("Configuration validation passed");
            } else {
                plugin.getLogger().warning("Configuration validation found issues - using defaults where needed");
            }

            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Configuration validation failed: " + e.getMessage());
            return false;
        }
    }

    private boolean isValidTimeFormat(String time) {
        try {
            String[] parts = time.split(":");
            if (parts.length != 2) return false;

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void saveConfig() {
        try {
            if (config != null && configFile != null) {
                config.save(configFile);
                plugin.getLogger().info("Configuration saved successfully");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save configuration: " + e.getMessage());
        }
    }

    public void backupConfig() {
        try {
            if (configFile.exists()) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                File backupFile = new File(dataFolder, "config-backup-" + timestamp + ".yml");
                Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Configuration backed up to: " + backupFile.getName());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to backup configuration: " + e.getMessage());
        }
    }

    // Configuration getters with defaults
    public FileConfiguration getConfig() { 
        return config; 
    }

    public boolean isDebugMode() {
        return config.getBoolean("debug", false);
    }

    public boolean isMonitoringEnabled() {
        return config.getBoolean("monitoring.enabled", true);
    }

    public boolean isMetricsEnabled() {
        return config.getBoolean("metrics.enabled", true);
    }

    public boolean isPlaceholdersEnabled() {
        return config.getBoolean("integrations.placeholderapi.enabled", true);
    }

    public boolean isLuckPermsIntegrationEnabled() {
        return config.getBoolean("integrations.luckperms.enabled", true);
    }

    public boolean isBackupConfigOnReload() {
        return config.getBoolean("backup-config-on-reload", true);
    }

    public int getCheckInterval() {
        return config.getInt("monitoring.check-interval", 30);
    }

    public double getTpsThreshold() {
        return config.getDouble("monitoring.tps-threshold", 16.0);
    }

    public double getMemoryThreshold() {
        return config.getDouble("monitoring.memory-threshold", 85.0);
    }

    public int getConsecutiveChecks() {
        return config.getInt("monitoring.consecutive-checks", 3);
    }

    public String getTimezone() {
        return config.getString("timezone", TimeZone.getDefault().getID());
    }

    // FIXED: Added getZoneId() method
    public ZoneId getZoneId() {
        return ZoneId.of(getTimezone());
    }

    public List<String> getRestartTimes() {
        return config.getStringList("restart-times");
    }

    public boolean isWarningsEnabled() {
        return config.getBoolean("warnings.enabled", true);
    }

    public List<Integer> getWarningIntervals() {
        return config.getIntegerList("warnings.intervals");
    }

    public boolean isSoundEnabled() {
        return config.getBoolean("warnings.sound-enabled", true);
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "&7[DMZ-ReStart] " + key);
    }

    public void setConfigValue(String path, Object value) {
        config.set(path, value);
    }

    public boolean hasPath(String path) {
        return config.contains(path);
    }

    public void reloadFromDisk() {
        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
            plugin.getLogger().info("Configuration reloaded from disk");
        }
    }
}