package io.github.dmzrestart;

import io.github.dmzrestart.commands.RestartCommand;
import io.github.dmzrestart.listeners.PlayerListener;
import io.github.dmzrestart.managers.*;
import io.github.dmzrestart.utils.ServerLoadMonitor;
import io.github.dmzrestart.utils.MetricsCollector;
import io.github.dmzrestart.api.RestartAPI;
import io.github.dmzrestart.integrations.PlaceholderIntegration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class DMZRestartPlugin extends JavaPlugin {

    // Static instance for safe access
    private static DMZRestartPlugin instance;

    // Core managers - initialized in strict order
    private ConfigManager configManager;
    private LogManager logManager;
    private RestartManager restartManager;
    private AlertManager alertManager;
    private PermissionManager permissionManager;

    // Utilities - initialized after managers
    private ServerLoadMonitor serverLoadMonitor;
    private MetricsCollector metricsCollector;
    private RestartAPI restartAPI;

    // Plugin state tracking
    private boolean isEnabled = false;
    private boolean isInitialized = false;
    private long pluginStartTime;
    private long initializationTime = 0;

    @Override
    public void onEnable() {
        instance = this; // Set static instance immediately
        pluginStartTime = System.currentTimeMillis();
        long startTime = System.currentTimeMillis();

        getLogger().info("========================================");
        getLogger().info("    DMZ ReStart v" + getDescription().getVersion());
        getLogger().info("    Professional Server Management");
        getLogger().info("========================================");

        try {
            // Phase 1: Core System Initialization
            getLogger().info("[1/7] Initializing core configuration system...");
            if (!initializeConfiguration()) {
                failStartup("Configuration system failed to initialize");
                return;
            }

            // Phase 2: Logging System
            getLogger().info("[2/7] Initializing advanced logging system...");
            if (!initializeLogging()) {
                failStartup("Logging system failed to initialize");
                return;
            }

            // Phase 3: Core Managers
            getLogger().info("[3/7] Initializing core management systems...");
            if (!initializeManagers()) {
                failStartup("Core managers failed to initialize");
                return;
            }

            // Phase 4: Utilities and Monitoring
            getLogger().info("[4/7] Initializing utilities and monitoring...");
            if (!initializeUtilities()) {
                failStartup("Utilities failed to initialize");
                return;
            }

            // Phase 5: Commands and Listeners
            getLogger().info("[5/7] Registering commands and event listeners...");
            if (!registerComponents()) {
                failStartup("Component registration failed");
                return;
            }

            // Phase 6: External Integrations
            getLogger().info("[6/7] Initializing external integrations...");
            registerIntegrations();

            // Phase 7: Service Startup
            getLogger().info("[7/7] Starting all services and finalizing...");
            if (!startAllServices()) {
                failStartup("Service startup failed");
                return;
            }

            // Mark as successfully enabled
            isEnabled = true;
            isInitialized = true;
            initializationTime = System.currentTimeMillis() - startTime;

            // Success messages
            logManager.info("========================================");
            logManager.info("DMZ ReStart ENABLED SUCCESSFULLY!");
            logManager.info("Startup Time: " + initializationTime + "ms");
            logManager.info("Server: " + getServer().getName() + " " + getServer().getVersion());
            logManager.info("Java: " + System.getProperty("java.version"));
            logManager.info("Players Online: " + Bukkit.getOnlinePlayers().size());
            logManager.info("========================================");

        } catch (Exception e) {
            failStartup("Unexpected error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (isEnabled) {
            if (logManager != null) {
                logManager.info("========================================");
                logManager.info("DMZ ReStart SHUTTING DOWN...");
                logManager.info("========================================");
            }

            // Stop all services in reverse order
            stopAllServices();

            // Final cleanup
            if (logManager != null) {
                long uptime = getPluginUptime();
                long hours = uptime / (1000 * 60 * 60);
                long minutes = (uptime % (1000 * 60 * 60)) / (1000 * 60);

                logManager.info("Plugin Statistics:");
                logManager.info("- Total Uptime: " + hours + "h " + minutes + "m");
                logManager.info("- Initialization Time: " + initializationTime + "ms");
                if (restartManager != null) {
                    logManager.info("- Restarts Managed: " + restartManager.getTotalRestartsManaged());
                }

                logManager.info("========================================");
                logManager.info("DMZ ReStart DISABLED SUCCESSFULLY");
                logManager.info("Thank you for using DMZ ReStart!");
                logManager.info("========================================");
            }
        }

        instance = null; // Clear static instance
    }

    // Phase 1: Configuration Initialization
    private boolean initializeConfiguration() {
        try {
            configManager = new ConfigManager(this);
            configManager.loadConfig();

            if (!configManager.validateConfig()) {
                getLogger().severe("Configuration validation failed - check your config.yml!");
                return false;
            }

            getLogger().info("Configuration loaded and validated successfully");
            return true;

        } catch (Exception e) {
            getLogger().severe("Failed to initialize configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Phase 2: Logging System Initialization
    private boolean initializeLogging() {
        try {
            logManager = new LogManager(this);
            logManager.setDebugMode(configManager.isDebugMode());

            logManager.info("Advanced logging system initialized successfully");
            logManager.debug("Debug logging is active");

            return true;

        } catch (Exception e) {
            getLogger().severe("Failed to initialize logging system: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Phase 3: Core Managers Initialization
    private boolean initializeManagers() {
        try {
            alertManager = new AlertManager(this);
            logManager.debug("AlertManager initialized");

            permissionManager = new PermissionManager(this);
            logManager.debug("PermissionManager initialized");

            restartManager = new RestartManager(this);
            logManager.debug("RestartManager initialized");

            logManager.info("All core managers initialized successfully");
            return true;

        } catch (Exception e) {
            logManager.severe("Failed to initialize core managers: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Phase 4: Utilities Initialization
    private boolean initializeUtilities() {
        try {
            serverLoadMonitor = new ServerLoadMonitor(this);
            logManager.debug("ServerLoadMonitor initialized");

            metricsCollector = new MetricsCollector(this);
            logManager.debug("MetricsCollector initialized");

            restartAPI = new RestartAPI(this);
            logManager.debug("RestartAPI initialized");

            logManager.info("All utilities initialized successfully");
            return true;

        } catch (Exception e) {
            logManager.severe("Failed to initialize utilities: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Phase 5: Component Registration
    private boolean registerComponents() {
        try {
            RestartCommand restartCommand = new RestartCommand(this);
            if (getCommand("dmzrestart") != null) {
                getCommand("dmzrestart").setExecutor(restartCommand);
                getCommand("dmzrestart").setTabCompleter(restartCommand);
                logManager.debug("Commands registered successfully");
            } else {
                logManager.warning("Command 'dmzrestart' not found in plugin.yml - commands will not work!");
            }

            getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
            logManager.debug("Event listeners registered successfully");

            logManager.info("All components registered successfully");
            return true;

        } catch (Exception e) {
            logManager.severe("Failed to register components: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Phase 6: External Integrations
    private void registerIntegrations() {
        int integrations = 0;

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null && 
            configManager.isPlaceholdersEnabled()) {
            try {
                new PlaceholderIntegration(this).register();
                logManager.info("✓ PlaceholderAPI integration enabled");
                integrations++;
            } catch (Exception e) {
                logManager.warning("✗ Failed to enable PlaceholderAPI integration: " + e.getMessage());
            }
        } else {
            logManager.debug("PlaceholderAPI integration disabled or plugin not found");
        }

        if (getServer().getPluginManager().getPlugin("LuckPerms") != null && 
            configManager.isLuckPermsIntegrationEnabled()) {
            try {
                permissionManager.initializeLuckPerms();
                logManager.info("✓ LuckPerms integration enabled");
                integrations++;
            } catch (Exception e) {
                logManager.warning("✗ Failed to enable LuckPerms integration: " + e.getMessage());
            }
        } else {
            logManager.debug("LuckPerms integration disabled or plugin not found");
        }

        logManager.info("External integrations: " + integrations + " active");
    }

    // Phase 7: Service Startup
    private boolean startAllServices() {
        try {
            restartManager.initialize();
            logManager.debug("RestartManager services started");

            if (configManager.isMonitoringEnabled()) {
                serverLoadMonitor.startMonitoring();
                logManager.info("✓ Performance monitoring active");
            } else {
                logManager.info("✗ Performance monitoring disabled");
            }

            if (configManager.isMetricsEnabled()) {
                metricsCollector.initialize();
                logManager.info("✓ Metrics collection active");
            } else {
                logManager.info("✗ Metrics collection disabled");
            }

            validateAllServices();

            logManager.info("All services started successfully");
            return true;

        } catch (Exception e) {
            logManager.severe("Failed to start services: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void validateAllServices() {
        int activeServices = 0;

        if (configManager != null) activeServices++;
        if (logManager != null) activeServices++;
        if (restartManager != null) activeServices++;
        if (alertManager != null) activeServices++;
        if (permissionManager != null) activeServices++;
        if (serverLoadMonitor != null) activeServices++;
        if (metricsCollector != null) activeServices++;
        if (restartAPI != null) activeServices++;

        logManager.info("Service validation: " + activeServices + "/8 core services active");

        if (activeServices < 8) {
            logManager.warning("Some services failed to initialize - check logs above");
        }
    }

    private void stopAllServices() {
        try {
            if (serverLoadMonitor != null) {
                serverLoadMonitor.stopMonitoring();
                if (logManager != null) logManager.debug("ServerLoadMonitor stopped");
            }

            if (restartManager != null) {
                restartManager.cleanup();
                if (logManager != null) logManager.debug("RestartManager cleaned up");
            }

            if (metricsCollector != null) {
                metricsCollector.shutdown();
                if (logManager != null) logManager.debug("MetricsCollector shut down");
            }

            if (logManager != null) logManager.debug("All services stopped gracefully");

        } catch (Exception e) {
            getLogger().warning("Error during service shutdown: " + e.getMessage());
        }
    }

    private void failStartup(String reason) {
        getLogger().severe("========================================");
        getLogger().severe("DMZ RESTART STARTUP FAILED!");
        getLogger().severe("Reason: " + reason);
        getLogger().severe("========================================");
        getLogger().severe("Plugin will be disabled to prevent further issues");
        getLogger().severe("Please check your configuration and try again");
        getLogger().severe("========================================");

        getServer().getPluginManager().disablePlugin(this);
    }

    public void reloadPlugin() {
        if (logManager != null) {
            logManager.info("========================================");
            logManager.info("DMZ ReStart RELOAD INITIATED");
            logManager.info("========================================");
        }

        try {
            long reloadStart = System.currentTimeMillis();

            if (configManager != null && configManager.isBackupConfigOnReload()) {
                configManager.backupConfig();
                logManager.info("Configuration backed up before reload");
            }

            logManager.info("Stopping all services...");
            stopAllServices();

            logManager.info("Reloading configuration...");
            if (configManager != null) {
                configManager.loadConfig();
                if (!configManager.validateConfig()) {
                    logManager.severe("Configuration reload failed validation!");
                    return;
                }
                logManager.setDebugMode(configManager.isDebugMode());
            }

            logManager.info("Restarting services...");
            if (!startAllServices()) {
                logManager.severe("Failed to restart services after reload!");
                return;
            }

            long reloadTime = System.currentTimeMillis() - reloadStart;
            logManager.info("========================================");
            logManager.info("DMZ ReStart RELOAD COMPLETED");
            logManager.info("Reload Time: " + reloadTime + "ms");
            logManager.info("========================================");

        } catch (Exception e) {
            if (logManager != null) {
                logManager.severe("Reload failed: " + e.getMessage());
                e.printStackTrace();
            } else {
                getLogger().severe("Reload failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Plugin uptime and statistics
    public long getPluginStartTime() {
        return pluginStartTime;
    }

    public long getPluginUptime() {
        return System.currentTimeMillis() - pluginStartTime;
    }

    public long getInitializationTime() {
        return initializationTime;
    }

    // Safe manager access with validation
    public ConfigManager getConfigManager() { 
        if (configManager == null) {
            getLogger().warning("ConfigManager accessed before initialization!");
        }
        return configManager; 
    }

    public LogManager getLogManager() { 
        if (logManager == null) {
            getLogger().warning("LogManager accessed before initialization!");
        }
        return logManager; 
    }

    public RestartManager getRestartManager() { 
        if (restartManager == null && logManager != null) {
            logManager.warning("RestartManager accessed before initialization!");
        }
        return restartManager; 
    }

    public AlertManager getAlertManager() { 
        if (alertManager == null && logManager != null) {
            logManager.warning("AlertManager accessed before initialization!");
        }
        return alertManager; 
    }

    public PermissionManager getPermissionManager() { 
        if (permissionManager == null && logManager != null) {
            logManager.warning("PermissionManager accessed before initialization!");
        }
        return permissionManager; 
    }

    public ServerLoadMonitor getServerLoadMonitor() { 
        if (serverLoadMonitor == null && logManager != null) {
            logManager.warning("ServerLoadMonitor accessed before initialization!");
        }
        return serverLoadMonitor; 
    }

    public MetricsCollector getMetricsCollector() { 
        if (metricsCollector == null && logManager != null) {
            logManager.warning("MetricsCollector accessed before initialization!");
        }
        return metricsCollector; 
    }

    public RestartAPI getRestartAPI() { 
        if (restartAPI == null && logManager != null) {
            logManager.warning("RestartAPI accessed before initialization!");
        }
        return restartAPI; 
    }

    // Status checks
    public boolean isPluginEnabled() { 
        return isEnabled; 
    }

    public boolean isFullyInitialized() {
        return isInitialized && isEnabled;
    }

    // Static instance access
    public static DMZRestartPlugin getInstance() {
        return instance;
    }

    public String getPluginInfo() {
        return String.format("DMZ ReStart v%s | Uptime: %d minutes | Services: %s", 
            getDescription().getVersion(),
            getPluginUptime() / (1000 * 60),
            isFullyInitialized() ? "Active" : "Initializing");
    }
}