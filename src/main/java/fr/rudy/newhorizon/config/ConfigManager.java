package fr.rudy.newhorizon.config;

import fr.rudy.newhorizon.Main;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    // Obtenir la configuration de config.yml
    public FileConfiguration getConfig() {
        return Main.get().getConfig();
    }

    // Sauvegarder les modifications de config.yml
    public void saveConfig() {
        Main.get().saveConfig();
    }

    // Recharger config.yml
    public void reloadConfig() {
        Main.get().reloadConfig();
    }
}
