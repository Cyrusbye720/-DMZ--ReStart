package io.github.dmzrestart.managers;

import io.github.dmzrestart.DMZRestartPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import java.util.concurrent.ConcurrentHashMap;

public class AlertManager {
    private final DMZRestartPlugin plugin;
    private final ConcurrentHashMap<Player, BossBar> activeBossBars = new ConcurrentHashMap<>();

    public enum AlertLevel {
        INFO(ChatColor.GREEN, BarColor.GREEN),
        WARNING(ChatColor.YELLOW, BarColor.YELLOW),
        ERROR(ChatColor.RED, BarColor.RED),
        EMERGENCY(ChatColor.DARK_RED, BarColor.RED);

        private final ChatColor chatColor;
        private final BarColor barColor;

        AlertLevel(ChatColor chatColor, BarColor barColor) {
            this.chatColor = chatColor;
            this.barColor = barColor;
        }

        public ChatColor getChatColor() { return chatColor; }
        public BarColor getBarColor() { return barColor; }
    }

    public AlertManager(DMZRestartPlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("AlertManager initialized successfully");
    }

    public void sendToPlayer(Player player, String message, AlertLevel level) {
        if (player == null || !player.isOnline()) return;

        String coloredMessage = level.getChatColor() + ChatColor.translateAlternateColorCodes('&', message);
        player.sendMessage(coloredMessage);

        // Create boss bar for warnings and errors
        if (level == AlertLevel.WARNING || level == AlertLevel.ERROR || level == AlertLevel.EMERGENCY) {
            showBossBar(player, message, level);
        }
    }

    private void showBossBar(Player player, String message, AlertLevel level) {
        // Remove existing boss bar
        BossBar existingBar = activeBossBars.remove(player);
        if (existingBar != null) {
            existingBar.removePlayer(player);
        }

        // Create new boss bar
        String cleanMessage = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message));
        BossBar bossBar = Bukkit.createBossBar(cleanMessage, level.getBarColor(), BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);

        activeBossBars.put(player, bossBar);

        // Remove after 10 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            BossBar bar = activeBossBars.remove(player);
            if (bar != null) {
                bar.removePlayer(player);
            }
        }, 200L);
    }

    public void broadcastAlert(String message, AlertLevel level) {
        String coloredMessage = level.getChatColor() + ChatColor.translateAlternateColorCodes('&', message);
        Bukkit.broadcastMessage(coloredMessage);

        // Show boss bar to all players for important alerts
        if (level == AlertLevel.WARNING || level == AlertLevel.ERROR || level == AlertLevel.EMERGENCY) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                showBossBar(player, message, level);
            }
        }
    }

    public void sendEmergencyAlert(String message) {
        broadcastAlert(message, AlertLevel.EMERGENCY);

        if (plugin.getLogManager() != null) {
            plugin.getLogManager().severe("EMERGENCY ALERT: " + message);
        }
    }

    public void cleanup() {
        // Remove all boss bars
        activeBossBars.values().forEach(bossBar -> {
            bossBar.removeAll();
        });
        activeBossBars.clear();
    }
}