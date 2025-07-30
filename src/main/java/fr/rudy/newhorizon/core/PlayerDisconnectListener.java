
package fr.rudy.newhorizon.core;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;


import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Base64;

public class PlayerDisconnectListener implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        try {
            Connection conn = fr.rudy.newhorizon.Main.get().getDatabase();
            if (conn == null) {
                // Database not available, skip saving player data
                return;
            }
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE newhorizon_player_data SET " +
                            "worn_helmet_data=?, worn_chestplate_data=?, worn_leggings_data=?, worn_boots_data=?," +
                            "cosmetic_helmet_id=?, cosmetic_backpack_id=?, cosmetic_offhand_id=?, cosmetic_balloon_id=? " +
                            "WHERE uuid=?"
            );

            ps.setString(1, serialize(p.getInventory().getHelmet()));
            ps.setString(2, serialize(p.getInventory().getChestplate()));
            ps.setString(3, serialize(p.getInventory().getLeggings()));
            ps.setString(4, serialize(p.getInventory().getBoots()));

            // HMCCosmetics integration - disabled due to Java version compatibility
            // Check if HMCCosmetics plugin is available at runtime
            String helmetId = null, backpackId = null, offhandId = null, balloonId = null;
            
            if (Bukkit.getPluginManager().getPlugin("HMCCosmetics") != null) {
                try {
                    // Use reflection to access HMCCosmetics API safely
                    helmetId = getCosmeticId(p, "HELMET");
                    backpackId = getCosmeticId(p, "BACKPACK");
                    offhandId = getCosmeticId(p, "OFFHAND");
                    balloonId = getCosmeticId(p, "BALLOON");
                } catch (Exception ex2) {
                    Bukkit.getLogger().warning("Failed to access HMCCosmetics API: " + ex2.getMessage());
                }
            }
            
            ps.setString(5, helmetId);
            ps.setString(6, backpackId);
            ps.setString(7, offhandId);
            ps.setString(8, balloonId);

            ps.setString(9, p.getUniqueId().toString());
            ps.executeUpdate();
        } catch (Exception ex) {
            Bukkit.getLogger().severe("Erreur lors de la sauvegarde des données de profil: " + ex.getMessage());
        }
    }

    private String serialize(ItemStack item) {
        if (item == null) return null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos);
            oos.writeObject(item);
            oos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            Bukkit.getLogger().severe("Erreur de sérialisation d'ItemStack: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to get cosmetic ID using reflection to avoid compilation issues
     * @param player The player to get cosmetics for
     * @param slotName The cosmetic slot name (HELMET, BACKPACK, OFFHAND, BALLOON)
     * @return The cosmetic ID or null if not available
     */
    private String getCosmeticId(Player player, String slotName) {
        try {
            // Use reflection to access HMCCosmetics API safely
            Class<?> apiClass = Class.forName("com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI");
            Class<?> userClass = Class.forName("com.hibiscusmc.hmccosmetics.user.CosmeticUser");
            Class<?> slotClass = Class.forName("com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot");
            
            // Get the user
            Object user = apiClass.getMethod("getUser", java.util.UUID.class).invoke(null, player.getUniqueId());
            if (user == null) return null;
            
            // Get the slot enum value
            Object slot = Enum.valueOf((Class<Enum>) slotClass, slotName);
            
            // Get the cosmetic
            Object cosmetic = userClass.getMethod("getCosmetic", slotClass).invoke(user, slot);
            if (cosmetic == null) return null;
            
            // Get the ID
            return (String) cosmetic.getClass().getMethod("getId").invoke(cosmetic);
            
        } catch (Exception e) {
            // If reflection fails, return null (cosmetic data won't be saved)
            return null;
        }
    }

}
