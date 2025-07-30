package fr.rudy.newhorizon.ui;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.rudy.newhorizon.Main;
import org.bukkit.Bukkit;
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
        BossBar bar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
        bar.setVisible(true);
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
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    BossBar bar = bossBars.get(player.getUniqueId());
                    if (bar == null) continue;

                    String territory = getTerritoryLabel(player);
                    String time = getFormattedTime(player.getWorld().getTime());
                    bar.setTitle("§f\uE1BB " + territory + "        §f\uE1BA " + time);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private String getTerritoryLabel(Player player) {
        Location loc = player.getLocation();
        String worldName = loc.getWorld().getName().toLowerCase();

        switch (worldName) {
            case "world_newhorizon" -> {
                return "Nature";
            }
            case "world_resource" -> {
                return "Ressource";
            }
            case "world_resource_nether" -> {
                return "Nether";
            }
            case "world_resource_the_end" -> {
                return "The End";
            }
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
        if (manager != null) {
            ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
            for (ProtectedRegion region : set) {
                String id = region.getId().toLowerCase();
                return switch (id) {
                    case "musee" -> "Musée";
                    case "ile_de_newhorizon" -> "Île de NewHorizon";
                    default -> id;
                };
            }
        }

        return "Aucune région";
    }

    private String getFormattedTime(long ticks) {
        int hours = (int) ((ticks / 1000L + 6L) % 24L);
        int minutes = (int) ((60.0 * (ticks % 1000L)) / 1000.0);
        return String.format("%02dh%02d", hours, minutes);
    }
}
