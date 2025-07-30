package fr.rudy.newhorizon.stats;

import fr.rudy.newhorizon.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerSessionListener implements Listener {
    
    private final SessionStatManager statManager;
    
    public PlayerSessionListener(SessionStatManager statManager) {
        this.statManager = statManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        statManager.addPlayer(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        statManager.removePlayer(event.getPlayer());
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            statManager.addBlocksBroken(event.getPlayer(), 1);
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player killer = event.getEntity().getKiller();
            statManager.addKill(killer);
        }
        
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            statManager.addDeath(player);
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            statManager.addDamageDealt(damager, event.getFinalDamage());
        }
        
        if (event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            statManager.addDamageTaken(damaged, event.getFinalDamage());
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && !(event instanceof EntityDamageByEntityEvent)) {
            Player player = (Player) event.getEntity();
            statManager.addDamageTaken(player, event.getFinalDamage());
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getWorld() != event.getTo().getWorld()) {
            return; // Ignore world changes
        }
        
        double distance = event.getFrom().distance(event.getTo());
        if (distance > 0) {
            statManager.addDistanceWalked(event.getPlayer(), distance);
        }
    }
}