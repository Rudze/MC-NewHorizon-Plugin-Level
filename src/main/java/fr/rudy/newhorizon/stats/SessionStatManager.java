package fr.rudy.newhorizon.stats;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionStatManager {
    
    private final Map<UUID, PlayerStats> playerStats = new HashMap<>();
    
    public void addPlayer(Player player) {
        playerStats.put(player.getUniqueId(), new PlayerStats());
    }
    
    public void removePlayer(Player player) {
        playerStats.remove(player.getUniqueId());
    }
    
    public PlayerStats getStats(Player player) {
        return playerStats.get(player.getUniqueId());
    }
    
    public void addBlocksBroken(Player player, int amount) {
        PlayerStats stats = getStats(player);
        if (stats != null) {
            stats.addBlocksBroken(amount);
        }
    }
    
    public void addKill(Player player) {
        PlayerStats stats = getStats(player);
        if (stats != null) {
            stats.addKill();
        }
    }
    
    public void addDeath(Player player) {
        PlayerStats stats = getStats(player);
        if (stats != null) {
            stats.addDeath();
        }
    }
    
    public void addDamageDealt(Player player, double damage) {
        PlayerStats stats = getStats(player);
        if (stats != null) {
            stats.addDamageDealt(damage);
        }
    }
    
    public void addDamageTaken(Player player, double damage) {
        PlayerStats stats = getStats(player);
        if (stats != null) {
            stats.addDamageTaken(damage);
        }
    }
    
    public void addDistanceWalked(Player player, double distance) {
        PlayerStats stats = getStats(player);
        if (stats != null) {
            stats.addDistanceWalked(distance);
        }
    }
    
    public static class PlayerStats {
        private int blocksBroken = 0;
        private int kills = 0;
        private int deaths = 0;
        private double damageDealt = 0.0;
        private double damageTaken = 0.0;
        private double distanceWalked = 0.0;
        
        public void addBlocksBroken(int amount) {
            this.blocksBroken += amount;
        }
        
        public void addKill() {
            this.kills++;
        }
        
        public void addDeath() {
            this.deaths++;
        }
        
        public void addDamageDealt(double damage) {
            this.damageDealt += damage;
        }
        
        public void addDamageTaken(double damage) {
            this.damageTaken += damage;
        }
        
        public void addDistanceWalked(double distance) {
            this.distanceWalked += distance;
        }
        
        // Getters
        public int getBlocksBroken() { return blocksBroken; }
        public int getKills() { return kills; }
        public int getDeaths() { return deaths; }
        public double getDamageDealt() { return damageDealt; }
        public double getDamageTaken() { return damageTaken; }
        public double getDistanceWalked() { return distanceWalked; }
    }
}