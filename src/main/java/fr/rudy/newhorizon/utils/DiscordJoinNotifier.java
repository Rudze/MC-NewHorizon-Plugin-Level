package fr.rudy.newhorizon.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordJoinNotifier implements Listener {

    //private static final String WEBHOOK_URL = "####";
    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1379072730963509369/PBqAgxzi92RQEP14sOplT5uJRTaXVSEwycsiCfkiP2pIpSuBcoWyOanBRP6-niCCDJqk";

    public DiscordJoinNotifier(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        sendJoinMessage(player.getName());
    }

    private void sendJoinMessage(String playerName) {
        try {
            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            String jsonPayload = String.format("{\"content\":\"🟢 **%s** a rejoint le serveur !\"}", playerName);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonPayload.getBytes());
                os.flush();
            }

            connection.getInputStream().close(); // Pour forcer la requête
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
