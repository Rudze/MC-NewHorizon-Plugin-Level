package fr.rudy.newhorizon.ui;

import fr.rudy.newhorizon.Main;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.stream.Collectors;

public class NameTagManager {

    private static final LuckPerms luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);

    public static void update(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        // Nom d’équipe unique (max 16 caractères)
        String teamName = "nh_" + player.getUniqueId().toString().substring(0, 14);
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        // Nettoyage ancien (évite conflits ou anciens noms)
        team.getEntries().forEach(team::removeEntry);

        // Récupération des infos LuckPerms
        CachedMetaData meta = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        String group = meta.getPrimaryGroup();

        // Format depuis la config
        String format = Main.get().getConfig().getString("nametag-groups." + group);
        if (format == null) {
            format = Main.get().getConfig().getString("nametag-format", "{prefix} &f{name}");
        }

        // Remplacements manuels
        format = format
                .replace("{prefix}", meta.getPrefix() != null ? meta.getPrefix() : "")
                .replace("{suffix}", meta.getSuffix() != null ? meta.getSuffix() : "")
                .replace("{prefixes}", String.join("", meta.getPrefixes().values()))
                .replace("{suffixes}", String.join("", meta.getSuffixes().values()))
                .replace("{world}", player.getWorld().getName())
                .replace("{name}", player.getName()) // Important : on garde le nom ici
                .replace("{displayname}", player.getDisplayName());

        // Application des placeholders (ex: %newhorizon_level_Pseudo%)
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(player, format);
        }

        // Couleurs classiques (& -> §)
        format = ChatColor.translateAlternateColorCodes('&', format);

        // Appliquer dans le tag
        team.setPrefix(format);
        team.setSuffix(""); // Optionnel, peut être utilisé si besoin
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        team.addEntry(player.getName());

        player.setScoreboard(scoreboard);
    }
}
