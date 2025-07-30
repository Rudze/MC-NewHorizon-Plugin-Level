package fr.rudy.newhorizon.ui;

import fr.rudy.newhorizon.Main;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TablistManager {

    private final Main plugin;
    private final LuckPerms luckPerms;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public TablistManager(Main plugin) {
        this.plugin = plugin;
        this.luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);
    }

    public void start() {
        long updateInterval = plugin.getConfig().getLong("tablist.update_interval", 40L);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateTab(player);
                }
            }
        }.runTaskTimer(plugin, 0L, updateInterval);
    }

    public void updateTab(Player player) {
        // Get configurable header & footer
        String header = plugin.getConfig().getString("tablist.header", "\n\n\n§7      §f\uE0D0      §r\n\n\n\n\n§7      §fSoutenez-nous avec le §b/boutique §f!      §r\n");
        String footer = plugin.getConfig().getString("tablist.footer", "\n§7      §7/site | /discord | /boutique      §r\n ");

        // Only set header/footer if tablist is enabled
        if (plugin.getConfig().getBoolean("tablist.enabled", true)) {
            player.setPlayerListHeaderFooter(header, footer);
        }

        // Get player group from LuckPerms
        CachedMetaData meta = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        String group = meta.getPrimaryGroup();

        // Use tablist group format or fallback
        String format = plugin.getConfig().getString("tablist-groups." + group);
        if (format == null) {
            format = plugin.getConfig().getString("tablist-format", "&f[lvl %level_level_{name}%] &7{name}");
        }

        // Manual placeholders
        format = format
                .replace("{prefix}", meta.getPrefix() != null ? meta.getPrefix() : "")
                .replace("{suffix}", meta.getSuffix() != null ? meta.getSuffix() : "")
                .replace("{prefixes}", String.join("", meta.getPrefixes().values()))
                .replace("{suffixes}", String.join("", meta.getSuffixes().values()))
                .replace("{name}", player.getName())
                .replace("{displayname}", player.getDisplayName())
                .replace("{world}", player.getWorld().getName());

        // PlaceholderAPI support
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(player, format);
        }

        format = translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', format));
        player.setPlayerListName(format);
    }

    private String translateHexColorCodes(String input) {
        final char colorChar = ChatColor.COLOR_CHAR;
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer,
                    colorChar + "x" +
                            colorChar + hex.charAt(0) +
                            colorChar + hex.charAt(1) +
                            colorChar + hex.charAt(2) +
                            colorChar + hex.charAt(3) +
                            colorChar + hex.charAt(4) +
                            colorChar + hex.charAt(5)
            );
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
