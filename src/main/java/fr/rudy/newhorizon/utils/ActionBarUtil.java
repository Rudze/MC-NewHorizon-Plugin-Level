package fr.rudy.newhorizon.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public class ActionBarUtil {

    public static void sendActionBar(Player player, String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        try {
            // Versions 1.8 à 1.16
            Object chatComponentText = getNMSClass("ChatComponentText")
                    .getConstructor(String.class)
                    .newInstance(message);
            Object packetPlayOutChat = getNMSClass("PacketPlayOutChat")
                    .getConstructor(getNMSClass("IChatBaseComponent"), byte.class)
                    .newInstance(chatComponentText, (byte) 2);
            sendPacket(player, packetPlayOutChat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("net.minecraft.server." + version + "." + name);
    }
}
