// PlayerDisconnectListener.java
package fr.rudy.newhorizon.core;

import com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
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

            CosmeticUser user = HMCCosmeticsAPI.getUser(p.getUniqueId());
            ps.setString(5, (user.getCosmetic(CosmeticSlot.HELMET) != null ? user.getCosmetic(CosmeticSlot.HELMET).getId() : null));
            ps.setString(6, (user.getCosmetic(CosmeticSlot.BACKPACK) != null ? user.getCosmetic(CosmeticSlot.BACKPACK).getId() : null));
            ps.setString(7, (user.getCosmetic(CosmeticSlot.OFFHAND) != null ? user.getCosmetic(CosmeticSlot.OFFHAND).getId() : null));
            ps.setString(8, (user.getCosmetic(CosmeticSlot.BALLOON) != null ? user.getCosmetic(CosmeticSlot.BALLOON).getId() : null));

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

}
