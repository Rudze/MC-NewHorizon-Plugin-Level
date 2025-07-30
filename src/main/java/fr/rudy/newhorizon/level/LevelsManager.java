package fr.rudy.newhorizon.level;

import fr.rudy.newhorizon.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LevelsManager {

    private final Connection database;
    private final int initialExp;
    private final int expIncrementPercent;
    private final Map<UUID, Integer> expCache = new ConcurrentHashMap<>();

    public LevelsManager() {
        this.database = Main.get().getDatabase();
        this.initialExp = Math.max(1, Main.get().getConfig().getInt("levels.initialExp", 100));
        this.expIncrementPercent = Math.max(1, Main.get().getConfig().getInt("levels.expIncrementPercent", 30));
        
        // Validate configuration values
        validateConfiguration();
    }
    
    private void validateConfiguration() {
        if (initialExp <= 0) {
            Main.get().getLogger().warning("Configuration error: levels.initialExp must be positive. Using default value 100.");
        }
        if (expIncrementPercent <= 0) {
            Main.get().getLogger().warning("Configuration error: levels.expIncrementPercent must be positive. Using default value 30.");
        }
        if (getMaxLevel() <= 0) {
            Main.get().getLogger().warning("Configuration error: levels.maxLevel must be positive. Using default value 100.");
        }
    }

    public int getExp(UUID player) {
        if (expCache.containsKey(player)) {
            return expCache.get(player);
        }

        if (database == null) {
            // Database not available, return 0 experience
            return 0;
        }

        try (PreparedStatement statement = database.prepareStatement(
                "SELECT experience FROM newhorizon_player_data WHERE uuid = ?;")) {

            statement.setString(1, player.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return 0;

                int exp = resultSet.getInt("experience");
                expCache.put(player, exp);
                return exp;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return 0;
    }

    public boolean setExp(UUID player, int exp) {
        if (database == null) {
            // Database not available, only update cache
            expCache.put(player, exp);
            return false;
        }

        try (PreparedStatement statement = database.prepareStatement(
                "INSERT INTO newhorizon_player_data (uuid, experience) " +
                        "VALUES (?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET experience = excluded.experience;"
        )) {
            statement.setString(1, player.toString());
            statement.setInt(2, exp);
            statement.executeUpdate();
            expCache.put(player, exp); // met à jour le cache
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }

        return true;
    }

    public void addExp(UUID player, int expToAdd) {
        if (expToAdd <= 0) return;
        
        // Check if player is at max level
        if (isMaxLevel(player)) {
            Player bukkitPlayer = Bukkit.getPlayer(player);
            if (bukkitPlayer != null && Main.get().getConfig().getBoolean("levels.showMaxLevelMessage", true)) {
                String message = Main.get().getConfig().getString("messages.max_level_reached", "&eVous êtes au niveau maximum !");
                bukkitPlayer.sendMessage(Main.get().getPrefixInfo() + message);
            }
            return;
        }
        
        Player bukkitPlayer = Bukkit.getPlayer(player);
        
        // Apply experience multipliers
        double multiplier = calculateExpMultiplier(bukkitPlayer);
        int finalExp = (int) Math.round(expToAdd * multiplier);
        
        int oldLevel = getLevel(player);
        int currentExp = getExp(player);
        setExp(player, currentExp + finalExp);
        int newLevel = getLevel(player);

        // Show experience gain notification
        if (bukkitPlayer != null && Main.get().getConfig().getBoolean("levels.showExpGain", true)) {
            showExpGainNotification(bukkitPlayer, finalExp, multiplier);
        }

        if (newLevel > oldLevel) {
            onLevelUp(bukkitPlayer, oldLevel, newLevel);
        }
    }
    
    private double calculateExpMultiplier(Player player) {
        if (player == null) return 1.0;
        
        double multiplier = 1.0;
        
        // Weekend bonus
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
        if (dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY) {
            multiplier *= Main.get().getConfig().getDouble("levels.expMultipliers.weekend", 1.5);
        }
        
        // Night time bonus (configurable time range)
        long time = player.getWorld().getTime();
        long nightStart = Main.get().getConfig().getLong("advanced_multipliers.night_time.start", 12000);
        long nightEnd = Main.get().getConfig().getLong("advanced_multipliers.night_time.end", 6000);
        if (time >= nightStart || time <= nightEnd) { // Night time in Minecraft
            multiplier *= Main.get().getConfig().getDouble("levels.expMultipliers.nightTime", 1.2);
        }
        
        // Group bonus (configurable distance)
        double groupDistance = Main.get().getConfig().getDouble("advanced_multipliers.group_bonus_distance", 10.0);
        long nearbyPlayers = player.getWorld().getPlayers().stream()
            .filter(p -> !p.equals(player))
            .filter(p -> p.getLocation().distance(player.getLocation()) <= groupDistance)
            .count();
        
        if (nearbyPlayers > 0) {
            multiplier *= Main.get().getConfig().getDouble("levels.expMultipliers.groupBonus", 1.1);
        }
        
        return multiplier;
    }
    
    private void showExpGainNotification(Player player, int expGained, double multiplier) {
        String bonusText = "";
        if (multiplier > 1.0) {
            String bonusFormat = Main.get().getConfig().getString("messages.exp_bonus_format", " &e(x{multiplier})");
            bonusText = bonusFormat.replace("{multiplier}", String.format("%.1f", multiplier));
        }
        
        if (Main.get().getConfig().getBoolean("levels.useActionBarForExp", true)) {
            // Use ActionBar for experience notifications
            try {
                Class<?> actionBarUtil = Class.forName("fr.rudy.newhorizon.utils.ActionBarUtil");
                java.lang.reflect.Method sendActionBar = actionBarUtil.getMethod("sendActionBar", Player.class, String.class);
                
                String actionBarFormat = Main.get().getConfig().getString("messages.exp_gain_actionbar", "&a+{exp} XP{bonus} &7| &bNiveau {level}");
                String message = actionBarFormat
                    .replace("{exp}", String.valueOf(expGained))
                    .replace("{bonus}", bonusText)
                    .replace("{level}", String.valueOf(getLevel(player.getUniqueId())));
                
                sendActionBar.invoke(null, player, message);
            } catch (Exception e) {
                // Fallback to chat message if ActionBarUtil is not available
                String chatFormat = Main.get().getConfig().getString("messages.exp_gain_chat", "&a+{exp} XP{bonus}");
                String message = chatFormat
                    .replace("{exp}", String.valueOf(expGained))
                    .replace("{bonus}", bonusText);
                player.sendMessage(message);
            }
        } else {
            // Use chat message
            String chatFormat = Main.get().getConfig().getString("messages.exp_gain_chat", "&a+{exp} XP{bonus}");
            String message = chatFormat
                .replace("{exp}", String.valueOf(expGained))
                .replace("{bonus}", bonusText);
            player.sendMessage(message);
        }
    }

    public int getLevel(UUID player) {
        final double playerExp = getExp(player);
        double expNeeded = initialExp;
        int level = 1;

        while (playerExp >= expNeeded) {
            expNeeded += expNeeded * (expIncrementPercent / 100.0);
            level++;
        }

        return level;
    }

    public int expToNextLevel(UUID player) {
        int currentLevel = getLevel(player);
        double currentExp = getExp(player);
        double expForCurrentLevel = getExpRequiredForLevel(currentLevel);
        double expForNextLevel = getExpRequiredForLevel(currentLevel + 1);
        
        return (int) Math.round(expForNextLevel - currentExp);
    }
    
    public double getExpRequiredForLevel(int level) {
        if (level <= 1) return 0;
        
        double expRequired = 0;
        double expForLevel = initialExp;
        
        for (int i = 2; i <= level; i++) {
            expRequired += expForLevel;
            expForLevel += expForLevel * (expIncrementPercent / 100.0);
        }
        
        return expRequired;
    }
    
    public int getMaxLevel() {
        return Main.get().getConfig().getInt("levels.maxLevel", 100);
    }
    
    public boolean isMaxLevel(UUID player) {
        return getLevel(player) >= getMaxLevel();
    }

    private void onLevelUp(Player player, int oldLevel, int newLevel) {
        if (player == null) return;

        // Execute configurable commands for each level gained
        for (int lvl = oldLevel + 1; lvl <= newLevel; lvl++) {
            java.util.List<String> commands = Main.get().getConfig().getStringList("level_commands.on_level_up");
            for (String command : commands) {
                String processedCommand = command
                    .replace("{player}", player.getName())
                    .replace("{level}", String.valueOf(lvl));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            }
        }

        // Send configurable level-up message
        String levelUpMessage = Main.get().getConfig().getString("messages.level_up", "&aFélicitations ! Vous êtes passé niveau &e{level}");
        String message = levelUpMessage.replace("{level}", String.valueOf(newLevel));
        player.sendMessage(Main.get().getPrefixInfo() + message);
        
        // Play configurable sound
        String soundName = Main.get().getConfig().getString("sounds.level_up.sound", "minecraft:entity.player.levelup");
        float volume = (float) Main.get().getConfig().getDouble("sounds.level_up.volume", 1.0);
        float pitch = (float) Main.get().getConfig().getDouble("sounds.level_up.pitch", 1.0);
        player.playSound(player.getLocation(), soundName, volume, pitch);
    }
}
