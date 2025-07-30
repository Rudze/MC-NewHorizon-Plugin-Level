package fr.rudy.newhorizon.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;


public class MessageUtil {

    // Méthode pour envoyer un message avec un préfixe
    public static void sendMessage(CommandSender sender, String prefix, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    // Méthode pour broadcast un message avec un préfixe
    public static void broadcastMessage(String prefix, String message) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }
}
