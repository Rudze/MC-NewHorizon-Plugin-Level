package fr.rudy.newhorizon.core;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(ChatColor.WHITE + "[" + ChatColor.GREEN + "+" + ChatColor.WHITE + "] " + player.getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(ChatColor.WHITE + "[" + ChatColor.DARK_RED + "-" + ChatColor.WHITE + "] " + player.getName());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        event.setDeathMessage(ChatColor.WHITE + "[☠] " + ChatColor.RED + victim.getName());
    }
}
