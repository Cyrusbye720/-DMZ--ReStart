package io.github.dmzrestart.listeners;

import io.github.dmzrestart.DMZRestartPlugin;
import io.github.dmzrestart.managers.AlertManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerListener implements Listener {
    private final DMZRestartPlugin plugin;
    private final ConcurrentHashMap<UUID, Long> playerJoinTimes = new ConcurrentHashMap<>();

    public PlayerListener(DMZRestartPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            UUID playerId = event.getPlayer().getUniqueId();
            playerJoinTimes.put(playerId, System.currentTimeMillis());

            if (plugin.getLogManager() != null && plugin.getLogManager().isDebugMode()) {
                plugin.getLogManager().logPlayerAction(event.getPlayer(), "JOINED");
            }

            if (plugin.getRestartManager() != null && plugin.getRestartManager().getNextScheduledRestart() != null) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (event.getPlayer().isOnline() && 
                        plugin.getPermissionManager().shouldReceiveAlerts(event.getPlayer())) {

                        // FIXED: Proper AlertLevel enum usage
                        plugin.getAlertManager().sendToPlayer(
                            event.getPlayer(),
                            "Welcome! Server restart may be scheduled - check /dmzrestart status",
                            AlertManager.AlertLevel.INFO
                        );
                    }
                }, 40L);
            }

        } catch (Exception e) {
            if (plugin.getLogManager() != null) {
                plugin.getLogManager().debug("Error in PlayerJoinEvent: " + e.getMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            UUID playerId = event.getPlayer().getUniqueId();
            Long joinTime = playerJoinTimes.remove(playerId);

            if (plugin.getLogManager() != null && plugin.getLogManager().isDebugMode()) {
                String sessionInfo = "";
                if (joinTime != null) {
                    long sessionLength = System.currentTimeMillis() - joinTime;
                    sessionInfo = " (session: " + (sessionLength / 1000) + "s)";
                }
                plugin.getLogManager().logPlayerAction(event.getPlayer(), "QUIT" + sessionInfo);
            }

        } catch (Exception e) {
            if (plugin.getLogManager() != null) {
                plugin.getLogManager().debug("Error in PlayerQuitEvent: " + e.getMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        try {
            String command = event.getMessage().toLowerCase();

            if (command.startsWith("/dmzrestart") || command.startsWith("/restart")) {
                if (plugin.getLogManager() != null) {
                    plugin.getLogManager().logPlayerAction(event.getPlayer(), 
                        "COMMAND: " + event.getMessage());
                }
            }

            if (plugin.getRestartManager() != null && 
                plugin.getRestartManager().isEmergencyRestartActive()) {

                if (command.startsWith("/stop") || command.startsWith("/reload") || command.startsWith("/restart")) {
                    if (!plugin.getPermissionManager().canBypassRestart(event.getPlayer())) {

                        event.setCancelled(true);
                        // FIXED: Proper AlertLevel enum usage
                        plugin.getAlertManager().sendToPlayer(event.getPlayer(),
                            "&cCommand blocked - Emergency restart in progress!",
                            AlertManager.AlertLevel.ERROR);
                    }
                }
            }

        } catch (Exception e) {
            if (plugin.getLogManager() != null) {
                plugin.getLogManager().debug("Error in PlayerCommandPreprocessEvent: " + e.getMessage());
            }
        }
    }

    public long getPlayerSessionLength(UUID playerId) {
        Long joinTime = playerJoinTimes.get(playerId);
        if (joinTime != null) {
            return (System.currentTimeMillis() - joinTime) / 1000;
        }
        return 0;
    }

    public int getActivePlayerSessions() {
        return playerJoinTimes.size();
    }
}
