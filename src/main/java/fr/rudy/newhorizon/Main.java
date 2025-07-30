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
        saveDefaultConfig();

        // DatabaseAPI integration
        org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin("DatabaseAPI");
        
        if (plugin != null && plugin.isEnabled()) {
            try {
                // Use reflection to access DatabaseAPI methods
                Object databaseManager = plugin.getClass().getMethod("getDatabaseManager").invoke(plugin);
                database = (Connection) databaseManager.getClass().getMethod("getConnection").invoke(databaseManager);

                try (Statement stmt = database.createStatement()) {
                    // Only progression system table - player data with experience for levels
                    stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS newhorizon_player_data (" +
                                    "uuid VARCHAR(36) PRIMARY KEY, " +
                                    "experience INT DEFAULT 0)"
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                getLogger().severe("❌ Erreur lors de l'accès à DatabaseAPI: " + e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

        } else {
            getLogger().severe("❌ DatabaseAPI introuvable !");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        setupManagers();

        // PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            NewHorizonPlaceholder.registerExpansion();
        }

        // Listeners - Only progression system listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSessionListener(sessionStatManager), this);
        Bukkit.getPluginManager().registerEvents(new StatsGUIListener(sessionStatManager), this);
        Bukkit.getPluginManager().registerEvents(new NameTagListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDisconnectListener(), this);

        // Tablist
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            tablistManager = new TablistManager(this);
            tablistManager.start();
        }

        // Create StatsGUIListener for command registration
        StatsGUIListener statsGUIListener = new StatsGUIListener(sessionStatManager);

        // Commands - Only progression system commands
        getCommand("level").setExecutor(new LevelCommand());
        getCommand("stats").setExecutor(new StatsCommand(statsGUIListener, sessionStatManager));

        // Préfixes
        prefixError = getConfig().getString("general.prefixError", "&c[Erreur] ");
        prefixInfo = getConfig().getString("general.prefixInfo", "&a[Info] ");

        getLogger().info("✅ LEVEL plugin activé !");
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