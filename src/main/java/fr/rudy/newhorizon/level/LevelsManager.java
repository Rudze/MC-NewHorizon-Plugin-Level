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
        this.initialExp = Main.get().getConfig().getInt("levels.initialExp");
        this.expIncrementPercent = Main.get().getConfig().getInt("levels.expIncrementPercent");
    }

    public int getExp(UUID player) {
        if (expCache.containsKey(player)) {
            return expCache.get(player);
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
        int oldLevel = getLevel(player);
        int currentExp = getExp(player);
        setExp(player, currentExp + expToAdd);
        int newLevel = getLevel(player);

        if (newLevel > oldLevel) {
            onLevelUp(Bukkit.getPlayer(player), oldLevel, newLevel);
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
        double nextLevelExp = initialExp;
        for (int i = 1; i < getLevel(player); i++) {
            nextLevelExp += nextLevelExp * (expIncrementPercent / 100.0);
        }

        return (int) Math.round(nextLevelExp - getExp(player));
    }

    private void onLevelUp(Player player, int oldLevel, int newLevel) {
        if (player == null) return;

        for (int lvl = oldLevel + 1; lvl <= newLevel; lvl++) {
            String command = "lp user " + player.getName() + " permission set lvl." + lvl;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        player.sendMessage(Main.get().getPrefixInfo() + "§aFélicitations ! Vous êtes passé niveau §e" + newLevel);
        player.playSound(player.getLocation(), "minecraft:entity.player.levelup", 1f, 1f);
    }
}
