package fr.rudy.newhorizon.ui;

// WorldGuard imports removed due to Java version compatibility issues
// import com.sk89q.worldedit.bukkit.BukkitAdapter;
// import com.sk89q.worldedit.math.BlockVector3;
// import com.sk89q.worldguard.WorldGuard;
// import com.sk89q.worldguard.protection.ApplicableRegionSet;
// import com.sk89q.worldguard.protection.managers.RegionManager;
// import com.sk89q.worldguard.protection.regions.ProtectedRegion;
// import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.rudy.newhorizon.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarManager implements Listener {

    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final Main plugin;

    public BossBarManager(Main plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
        startUpdater();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Get configurable color and style
        String colorName = plugin.getConfig().getString("bossbar.color", "BLUE");
        String styleName = plugin.getConfig().getString("bossbar.style", "SOLID");
        
        BarColor color;
        try {
            color = BarColor.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            color = BarColor.BLUE;
        }
        
        BarStyle style;
        try {
            style = BarStyle.valueOf(styleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            style = BarStyle.SOLID;
        }
        
        BossBar bar = Bukkit.createBossBar("", color, style);
        bar.setVisible(plugin.getConfig().getBoolean("bossbar.enabled", true));
        bar.addPlayer(player);
        bossBars.put(player.getUniqueId(), bar);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        BossBar bar = bossBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }

    private void startUpdater() {
        long updateInterval = plugin.getConfig().getLong("bossbar.update_interval", 20L);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    BossBar bar = bossBars.get(player.getUniqueId());
                    if (bar == null) continue;

                    String territory = getTerritoryLabel(player);
                    String time = getFormattedTime(player.getWorld().getTime());
                    
                    String titleFormat = plugin.getConfig().getString("bossbar.title_format", "&f\uE1BB {territory}        &f\uE1BA {time}");
                    String title = titleFormat
                        .replace("{territory}", territory)
                        .replace("{time}", time);
                    
                    bar.setTitle(ChatColor.translateAlternateColorCodes('&', title));
                }
            }
        }.runTaskTimer(plugin, 0L, updateInterval);
    }

    private String getTerritoryLabel(Player player) {
        Location loc = player.getLocation();
        String worldName = loc.getWorld().getName().toLowerCase();

        // Check for configurable world territories
        String territoryKey = "bossbar.territories." + worldName;
        String territory = plugin.getConfig().getString(territoryKey);
        if (territory != null) {
            return territory;
        }

        // Check WorldGuard regions - using reflection for compatibility
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                String regionId = getWorldGuardRegion(loc);
                if (regionId != null) {
                    String regionKey = "bossbar.regions." + regionId.toLowerCase();
                    String regionName = plugin.getConfig().getString(regionKey);
                    if (regionName != null) {
                        return regionName;
                    }
                    // Return the region ID if no custom name is configured
                    return regionId;
                }
            } catch (Exception e) {
                // WorldGuard lookup failed, continue with fallback
            }
        }

        return plugin.getConfig().getString("bossbar.territories.no_region", "Aucune région");
    }

    private String getFormattedTime(long ticks) {
        int hours = (int) ((ticks / 1000L + 6L) % 24L);
        int minutes = (int) ((60.0 * (ticks % 1000L)) / 1000.0);
        String timeFormat = plugin.getConfig().getString("bossbar.time_format", "%02dh%02d");
        return String.format(timeFormat, hours, minutes);
    }

    /**
     * Helper method to get WorldGuard region using reflection to avoid compilation issues
     * @param location The location to check for regions
     * @return The region ID or null if not found
     */
    private String getWorldGuardRegion(Location location) {
        try {
            // Use reflection to access WorldGuard API safely
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            
            // Get WorldGuard instance
            Object worldGuard = worldGuardClass.getMethod("getInstance").invoke(null);
            Object platform = worldGuard.getClass().getMethod("getPlatform").invoke(worldGuard);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            
            // Adapt world
            Object adaptedWorld = bukkitAdapterClass.getMethod("adapt", org.bukkit.World.class).invoke(null, location.getWorld());
            Object regionManager = regionContainer.getClass().getMethod("get", Class.forName("com.sk89q.worldedit.world.World")).invoke(regionContainer, adaptedWorld);
            
            if (regionManager != null) {
                // Adapt location
                Object blockVector = bukkitAdapterClass.getMethod("asBlockVector", Location.class).invoke(null, location);
                Object regionSet = regionManager.getClass().getMethod("getApplicableRegions", Class.forName("com.sk89q.worldedit.math.BlockVector3")).invoke(regionManager, blockVector);
                
                // Get first region
                Object iterator = regionSet.getClass().getMethod("iterator").invoke(regionSet);
                if ((Boolean) iterator.getClass().getMethod("hasNext").invoke(iterator)) {
                    Object region = iterator.getClass().getMethod("next").invoke(iterator);
                    return (String) region.getClass().getMethod("getId").invoke(region);
                }
            }
            
            return null;
        } catch (Exception e) {
            // If reflection fails, return null
            return null;
        }
    }
}
