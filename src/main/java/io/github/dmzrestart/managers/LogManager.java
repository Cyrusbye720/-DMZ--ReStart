package io.github.dmzrestart.managers;

import io.github.dmzrestart.DMZRestartPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {
    private final DMZRestartPlugin plugin;
    private boolean debugMode = false;
    private File logFile;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LogManager(DMZRestartPlugin plugin) {
        this.plugin = plugin;
        initializeLogFile();
        plugin.getLogger().info("LogManager initialized successfully");
    }

    private void initializeLogFile() {
        try {
            File logDir = new File(plugin.getDataFolder(), "logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            logFile = new File(logDir, "dmz-restart-" + dateStr + ".log");

            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to initialize log file: " + e.getMessage());
        }
    }

    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void info(String message) {
        plugin.getLogger().info(message);
        writeToFile("INFO", message);
    }

    public void warning(String message) {
        plugin.getLogger().warning(message);
        writeToFile("WARN", message);
    }

    public void severe(String message) {
        plugin.getLogger().severe(message);
        writeToFile("ERROR", message);
    }

    public void debug(String message) {
        if (debugMode) {
            plugin.getLogger().info("[DEBUG] " + message);
            writeToFile("DEBUG", message);
        }
    }

    private void writeToFile(String level, String message) {
        if (logFile == null) return;

        try (FileWriter writer = new FileWriter(logFile, true)) {
            String timestamp = dateFormat.format(new Date());
            writer.write(String.format("[%s] [%s] %s\n", timestamp, level, message));
            writer.flush();
        } catch (IOException e) {
            // Silent fail to prevent log spam
        }
    }

    public void broadcast(String message) {
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        Bukkit.broadcastMessage(colored);
        info("BROADCAST: " + message);
    }

    public void sendMessage(CommandSender sender, String message) {
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        sender.sendMessage(colored);
    }

    public void logPlayerAction(Player player, String action) {
        if (debugMode) {
            String logMessage = String.format("Player %s (%s): %s", 
                player.getName(), player.getUniqueId().toString(), action);
            debug(logMessage);
        }
    }

    public void logSystemEvent(String event, String details) {
        String logMessage = String.format("SYSTEM EVENT [%s]: %s", event, details);
        info(logMessage);
    }
}