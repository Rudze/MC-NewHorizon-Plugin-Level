package fr.rudy.newhorizon.placeholders;

import fr.rudy.newhorizon.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class NewHorizonPlaceholder extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "level";
    }

    @Override
    public String getAuthor() {
        return "Rudy";
    }

    @Override
    public String getVersion() {
        return "2.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    public static void registerExpansion() {
        PlaceholderExpansion expansion = new NewHorizonPlaceholder();
        boolean result = expansion.register();
        Bukkit.getLogger().info("✅ [LEVEL] Expansion enregistrée dans PAPI : " + result);
    }

    @Override
    public String onRequest(OfflinePlayer requester, String identifier) {
        if (identifier == null || identifier.isEmpty()) return null;

        // DEBUG
        //Bukkit.getLogger().info("🔍 [PAPI] Request: " + identifier);

        // LEVEL
        if (identifier.startsWith("level_")) {
            String name = identifier.substring("level_".length());
            OfflinePlayer target = Bukkit.getOfflinePlayer(name);
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) return "Niveau inconnu";
            return String.valueOf(Main.get().getLevelsManager().getLevel(target.getUniqueId()));
        }

        // EXP
        if (identifier.startsWith("exp_")) {
            String name = identifier.substring("exp_".length());
            OfflinePlayer target = Bukkit.getOfflinePlayer(name);
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) return "Exp inconnue";
            return String.valueOf(Main.get().getLevelsManager().getExp(target.getUniqueId()));
        }


        return null;
    }
}
