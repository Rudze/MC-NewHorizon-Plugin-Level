package fr.rudy.newhorizon;

import fr.rudy.newhorizon.commands.LevelCommand;
import fr.rudy.newhorizon.commands.StatsCommand;
import fr.rudy.newhorizon.core.PlayerConnectionListener;
import fr.rudy.newhorizon.core.PlayerDisconnectListener;
import fr.rudy.newhorizon.level.LevelsManager;
import fr.rudy.newhorizon.level.PlayerListener;
import fr.rudy.newhorizon.stats.SessionStatManager;
import fr.rudy.newhorizon.stats.PlayerSessionListener;
import fr.rudy.newhorizon.stats.StatsGUIListener;
import fr.rudy.newhorizon.placeholders.NewHorizonPlaceholder;
import fr.rudy.newhorizon.ui.BossBarManager;
import fr.rudy.newhorizon.ui.NameTagListener;
import fr.rudy.newhorizon.ui.NameTagManager;
import fr.rudy.newhorizon.ui.TablistManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class Main extends JavaPlugin implements Listener {

    private static Main instance;

    public static Main get() {
        return instance;
    }

    private Connection database;
    private LevelsManager levelsManager;
    private SessionStatManager sessionStatManager;
    private TablistManager tablistManager;

    private String prefixError;
    private String prefixInfo;

    @Override
    public void onEnable() {
        instance = this;
        
        // === DIAGNOSTIC STARTUP LOGGING ===
        getLogger().info("========================================");
        getLogger().info("    LEVEL PLUGIN - DÉMARRAGE DÉTAILLÉ");
        getLogger().info("========================================");
        
        // System diagnostics
        logSystemDiagnostics();
        
        // Configuration loading with validation
        getLogger().info("📁 [ÉTAPE 1/8] Chargement de la configuration...");
        try {
            saveDefaultConfig();
            validateConfiguration();
            getLogger().info("✅ Configuration chargée et validée avec succès!");
        } catch (Exception e) {
            getLogger().severe("❌ ERREUR CRITIQUE lors du chargement de la configuration: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // DatabaseAPI integration with improved error handling
        getLogger().info("🗄️ [ÉTAPE 2/8] Initialisation de la base de données...");
        org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin("DatabaseAPI");
        
        if (plugin != null && plugin.isEnabled()) {
            getLogger().info("   🔍 DatabaseAPI détecté: v" + plugin.getDescription().getVersion());
            getLogger().info("   📡 Tentative de connexion à la base de données...");
            try {
                // Use reflection to access DatabaseAPI methods
                Object databaseManager = plugin.getClass().getMethod("getDatabaseManager").invoke(plugin);
                if (databaseManager == null) {
                    throw new RuntimeException("DatabaseManager est null");
                }
                
                database = (Connection) databaseManager.getClass().getMethod("getConnection").invoke(databaseManager);
                if (database == null) {
                    throw new RuntimeException("Connection est null");
                }

                // Test the connection
                if (database.isClosed()) {
                    throw new SQLException("La connexion à la base de données est fermée");
                }

                try (Statement stmt = database.createStatement()) {
                    // Only progression system table - player data with experience for levels
                    stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS newhorizon_player_data (" +
                                    "uuid VARCHAR(36) PRIMARY KEY, " +
                                    "experience INT DEFAULT 0)"
                    );
                    getLogger().info("✅ Table newhorizon_player_data créée/vérifiée avec succès!");
                } catch (SQLException e) {
                    getLogger().severe("❌ Erreur SQL lors de la création de la table: " + e.getMessage());
                    e.printStackTrace();
                    database = null;
                    return;
                }
                getLogger().info("✅ DatabaseAPI connecté avec succès !");
                
            } catch (NoSuchMethodException e) {
                getLogger().severe("❌ Méthode DatabaseAPI introuvable: " + e.getMessage());
                getLogger().severe("❌ Version de DatabaseAPI incompatible!");
                database = null;
            } catch (SQLException e) {
                getLogger().severe("❌ Erreur de connexion à la base de données: " + e.getMessage());
                e.printStackTrace();
                database = null;
            } catch (Exception e) {
                getLogger().warning("⚠️ Erreur lors de l'accès à DatabaseAPI: " + e.getMessage());
                getLogger().warning("⚠️ Type d'erreur: " + e.getClass().getSimpleName());
                if (e.getCause() != null) {
                    getLogger().warning("⚠️ Cause: " + e.getCause().getMessage());
                }
                database = null;
            }

        } else {
            if (plugin == null) {
                getLogger().warning("   ⚠️ DatabaseAPI introuvable dans les plugins!");
                getLogger().info("   💡 SOLUTION: Téléchargez DatabaseAPI.jar et placez-le dans plugins/");
            } else {
                getLogger().warning("   ⚠️ DatabaseAPI trouvé mais désactivé!");
                getLogger().info("   💡 SOLUTION: Vérifiez les erreurs de DatabaseAPI dans les logs");
            }
            getLogger().info("   ℹ️ Le plugin continuera sans base de données (mode local).");
            database = null;
        }

        // Initialize managers with error handling
        getLogger().info("⚙️ [ÉTAPE 3/8] Initialisation des gestionnaires...");
        try {
            setupManagers();
            getLogger().info("   ✅ LevelsManager initialisé");
            getLogger().info("   ✅ SessionStatManager initialisé");
            getLogger().info("   ✅ BossBarManager initialisé");
        } catch (Exception e) {
            getLogger().severe("   ❌ ERREUR CRITIQUE lors de l'initialisation des managers: " + e.getMessage());
            getLogger().severe("   💡 SOLUTION: Vérifiez que toutes les classes sont présentes dans le JAR");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Verify managers are properly initialized
        if (sessionStatManager == null) {
            getLogger().severe("   ❌ ERREUR CRITIQUE: SessionStatManager n'a pas pu être initialisé!");
            getLogger().severe("   💡 SOLUTION: Recompilez le plugin avec 'mvn clean package'");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // PlaceholderAPI integration
        getLogger().info("🔗 [ÉTAPE 4/8] Intégration PlaceholderAPI...");
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                NewHorizonPlaceholder.registerExpansion();
                getLogger().info("   ✅ PlaceholderAPI intégré avec succès!");
                getLogger().info("   📋 Placeholders disponibles: %newhorizon_level%, %newhorizon_experience%");
            } catch (Exception e) {
                getLogger().warning("   ⚠️ Erreur lors de l'intégration PlaceholderAPI: " + e.getMessage());
                getLogger().info("   💡 SOLUTION: Vérifiez la version de PlaceholderAPI (2.11.6+ recommandée)");
            }
        } else {
            getLogger().info("   ℹ️ PlaceholderAPI non détecté - Placeholders désactivés");
            getLogger().info("   💡 OPTIONNEL: Installez PlaceholderAPI pour les placeholders");
        }

        // Create StatsGUIListener once for both events and commands
        getLogger().info("🎮 [ÉTAPE 5/8] Création des interfaces utilisateur...");
        StatsGUIListener statsGUIListener;
        try {
            statsGUIListener = new StatsGUIListener(sessionStatManager);
            getLogger().info("   ✅ StatsGUIListener créé avec succès");
        } catch (Exception e) {
            getLogger().severe("   ❌ ERREUR CRITIQUE lors de la création de StatsGUIListener: " + e.getMessage());
            getLogger().severe("   💡 SOLUTION: Vérifiez que triumph-gui est inclus dans le JAR");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Listeners - Only progression system listeners with error handling
        getLogger().info("👂 [ÉTAPE 6/8] Enregistrement des événements...");
        try {
            Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
            getLogger().info("   ✅ PlayerListener enregistré (gestion XP/niveaux)");
            
            Bukkit.getPluginManager().registerEvents(new PlayerSessionListener(sessionStatManager), this);
            getLogger().info("   ✅ PlayerSessionListener enregistré (statistiques de session)");
            
            Bukkit.getPluginManager().registerEvents(statsGUIListener, this);
            getLogger().info("   ✅ StatsGUIListener enregistré (interface statistiques)");
            
            Bukkit.getPluginManager().registerEvents(new NameTagListener(), this);
            getLogger().info("   ✅ NameTagListener enregistré (noms personnalisés)");
            
            Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(), this);
            getLogger().info("   ✅ PlayerConnectionListener enregistré (connexions)");
            
            Bukkit.getPluginManager().registerEvents(new PlayerDisconnectListener(), this);
            getLogger().info("   ✅ PlayerDisconnectListener enregistré (déconnexions)");
            
            getLogger().info("   🎉 Tous les listeners enregistrés avec succès!");
        } catch (Exception e) {
            getLogger().severe("   ❌ ERREUR CRITIQUE lors de l'enregistrement des listeners: " + e.getMessage());
            getLogger().severe("   💡 SOLUTION: Vérifiez que toutes les classes Listener existent");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Tablist Manager
        getLogger().info("📋 [ÉTAPE 7/8] Initialisation du TabList...");
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                tablistManager = new TablistManager(this);
                tablistManager.start();
                getLogger().info("   ✅ TablistManager démarré avec succès!");
                getLogger().info("   📊 TabList personnalisé activé avec placeholders");
            } catch (Exception e) {
                getLogger().warning("   ⚠️ Erreur lors du démarrage du TablistManager: " + e.getMessage());
                getLogger().info("   💡 SOLUTION: Vérifiez la configuration du TabList");
            }
        } else {
            getLogger().info("   ℹ️ PlaceholderAPI requis pour le TabList personnalisé");
            getLogger().info("   💡 OPTIONNEL: Installez PlaceholderAPI pour le TabList");
        }

        // Commands registration
        getLogger().info("⌨️ [ÉTAPE 8/8] Enregistrement des commandes...");
        int commandsRegistered = 0;
        try {
            if (getCommand("level") != null) {
                getCommand("level").setExecutor(new LevelCommand());
                getLogger().info("   ✅ Commande /level enregistrée!");
                commandsRegistered++;
            } else {
                getLogger().warning("   ⚠️ Commande 'level' non trouvée dans plugin.yml!");
                getLogger().severe("   💡 SOLUTION: Vérifiez la section 'commands' dans plugin.yml");
            }
            
            if (getCommand("stats") != null) {
                getCommand("stats").setExecutor(new StatsCommand(statsGUIListener, sessionStatManager));
                getLogger().info("   ✅ Commande /stats enregistrée!");
                commandsRegistered++;
            } else {
                getLogger().warning("   ⚠️ Commande 'stats' non trouvée dans plugin.yml!");
                getLogger().severe("   💡 SOLUTION: Vérifiez la section 'commands' dans plugin.yml");
            }
            
            getLogger().info("   📊 " + commandsRegistered + "/2 commandes enregistrées avec succès");
        } catch (Exception e) {
            getLogger().severe("   ❌ ERREUR lors de l'enregistrement des commandes: " + e.getMessage());
            getLogger().severe("   💡 SOLUTION: Vérifiez que les classes Command existent");
            e.printStackTrace();
        }

        // Final configuration loading
        try {
            prefixError = getConfig().getString("general.prefixError", "&c[Erreur] ");
            prefixInfo = getConfig().getString("general.prefixInfo", "&a[Info] ");
            getLogger().info("   ✅ Préfixes de messages configurés");
        } catch (Exception e) {
            getLogger().warning("   ⚠️ Erreur lors du chargement des préfixes: " + e.getMessage());
            // Use default values
            prefixError = "&c[Erreur] ";
            prefixInfo = "&a[Info] ";
        }

        // Final startup summary
        getLogger().info("========================================");
        getLogger().info("🎉 LEVEL PLUGIN ACTIVÉ AVEC SUCCÈS!");
        getLogger().info("📊 Résumé du démarrage:");
        getLogger().info("   • Base de données: " + (database != null ? "✅ Connectée" : "❌ Mode local"));
        getLogger().info("   • PlaceholderAPI: " + (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? "✅ Intégré" : "❌ Non disponible"));
        getLogger().info("   • Commandes: " + commandsRegistered + "/2 enregistrées");
        getLogger().info("   • Version: v" + getDescription().getVersion());
        getLogger().info("========================================");
    }



    private void setupManagers() {
        // Only progression system managers
        levelsManager = new LevelsManager();
        sessionStatManager = new SessionStatManager();

        // UI Manager
        new BossBarManager(this);
    }



    @Override
    public void onDisable() {
        // Database connection is managed by DatabaseAPI, no need to close it manually
        getLogger().info("🛑 Plugin LEVEL désactivé proprement.");
    }

    /**
     * Log detailed system diagnostics for troubleshooting
     */
    private void logSystemDiagnostics() {
        getLogger().info("🔍 [DIAGNOSTIC] Informations système:");
        
        // Java information
        getLogger().info("   ☕ Java: " + System.getProperty("java.version") + 
                        " (" + System.getProperty("java.vendor") + ")");
        getLogger().info("   🏠 Java Home: " + System.getProperty("java.home"));
        
        // Server information
        getLogger().info("   🖥️ Serveur: " + getServer().getName() + " " + getServer().getVersion());
        getLogger().info("   📦 Bukkit API: " + getServer().getBukkitVersion());
        
        // System information
        getLogger().info("   💻 OS: " + System.getProperty("os.name") + " " + 
                        System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")");
        
        // Memory information
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        getLogger().info("   🧠 Mémoire: " + (totalMemory - freeMemory) + "MB utilisée / " + 
                        totalMemory + "MB allouée / " + maxMemory + "MB max");
        
        // Plugin information
        getLogger().info("   📋 Plugin LEVEL: v" + getDescription().getVersion());
        getLogger().info("   📁 Dossier plugin: " + getDataFolder().getAbsolutePath());
        
        // Scan for dependencies
        logDependencyStatus();
    }

    /**
     * Log the status of all plugin dependencies
     */
    private void logDependencyStatus() {
        getLogger().info("🔍 [DIAGNOSTIC] État des dépendances:");
        
        String[] dependencies = {"DatabaseAPI", "PlaceholderAPI", "ProtocolLib", "Vault", "LuckPerms"};
        
        for (String depName : dependencies) {
            org.bukkit.plugin.Plugin dep = Bukkit.getPluginManager().getPlugin(depName);
            if (dep != null) {
                if (dep.isEnabled()) {
                    getLogger().info("   ✅ " + depName + ": v" + dep.getDescription().getVersion() + " (ACTIVÉ)");
                } else {
                    getLogger().warning("   ⚠️ " + depName + ": v" + dep.getDescription().getVersion() + " (DÉSACTIVÉ)");
                }
            } else {
                getLogger().info("   ❌ " + depName + ": NON INSTALLÉ");
            }
        }
        
        // Count total plugins
        org.bukkit.plugin.Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        getLogger().info("   📊 Total plugins installés: " + plugins.length);
    }

    /**
     * Validate configuration file and report any issues
     */
    private void validateConfiguration() {
        getLogger().info("🔍 [DIAGNOSTIC] Validation de la configuration:");
        
        // Check if config file exists
        if (!getConfig().contains("general")) {
            getLogger().warning("   ⚠️ Section 'general' manquante dans config.yml");
        } else {
            getLogger().info("   ✅ Section 'general' trouvée");
        }
        
        // Validate specific config values
        String[] requiredKeys = {
            "general.prefixError",
            "general.prefixInfo"
        };
        
        for (String key : requiredKeys) {
            if (getConfig().contains(key)) {
                String value = getConfig().getString(key);
                getLogger().info("   ✅ " + key + ": '" + value + "'");
            } else {
                getLogger().warning("   ⚠️ Clé manquante: " + key + " (valeur par défaut utilisée)");
            }
        }
        
        // Check for debug mode
        boolean debugMode = getConfig().getBoolean("debug.enabled", false);
        if (debugMode) {
            getLogger().info("   🐛 MODE DEBUG ACTIVÉ - Logging verbeux");
        } else {
            getLogger().info("   ℹ️ Mode debug désactivé");
        }
        
        // Validate file permissions
        if (getDataFolder().canWrite()) {
            getLogger().info("   ✅ Permissions d'écriture: OK");
        } else {
            getLogger().severe("   ❌ ERREUR: Pas de permissions d'écriture sur " + getDataFolder().getAbsolutePath());
        }
    }

    // Getters
    public String getPrefixError() {
        return prefixError;
    }

    public String getPrefixInfo() {
        return prefixInfo;
    }

    public Connection getDatabase() {
        return database;
    }

    public LevelsManager getLevelsManager() {
        return levelsManager;
    }

    public SessionStatManager getSessionStatManager() {
        return sessionStatManager;
    }




}