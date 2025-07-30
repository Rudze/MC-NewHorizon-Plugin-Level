package fr.rudy.newhorizon.stats;

import fr.rudy.newhorizon.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class StatsGUIListener implements Listener {
    
    private final SessionStatManager statManager;
    
    public StatsGUIListener(SessionStatManager statManager) {
        this.statManager = statManager;
    }
    
    public void openStatsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6§lStatistiques de Session");
        
        SessionStatManager.PlayerStats stats = statManager.getStats(player);
        if (stats == null) {
            player.sendMessage(Main.get().getPrefixError() + "Aucune statistique trouvée pour cette session.");
            return;
        }
        
        // Blocks broken
        ItemStack blocksItem = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta blocksMeta = blocksItem.getItemMeta();
        blocksMeta.setDisplayName("§e§lBlocs Cassés");
        blocksMeta.setLore(Arrays.asList(
            "§7Nombre de blocs cassés:",
            "§a" + stats.getBlocksBroken()
        ));
        blocksItem.setItemMeta(blocksMeta);
        gui.setItem(10, blocksItem);
        
        // Kills
        ItemStack killsItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta killsMeta = killsItem.getItemMeta();
        killsMeta.setDisplayName("§c§lÉliminations");
        killsMeta.setLore(Arrays.asList(
            "§7Nombre d'éliminations:",
            "§a" + stats.getKills()
        ));
        killsItem.setItemMeta(killsMeta);
        gui.setItem(12, killsItem);
        
        // Deaths
        ItemStack deathsItem = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta deathsMeta = deathsItem.getItemMeta();
        deathsMeta.setDisplayName("§4§lMorts");
        deathsMeta.setLore(Arrays.asList(
            "§7Nombre de morts:",
            "§a" + stats.getDeaths()
        ));
        deathsItem.setItemMeta(deathsMeta);
        gui.setItem(14, deathsItem);
        
        // Damage dealt
        ItemStack damageDealtItem = new ItemStack(Material.BOW);
        ItemMeta damageDealtMeta = damageDealtItem.getItemMeta();
        damageDealtMeta.setDisplayName("§6§lDégâts Infligés");
        damageDealtMeta.setLore(Arrays.asList(
            "§7Dégâts totaux infligés:",
            "§a" + String.format("%.1f", stats.getDamageDealt())
        ));
        damageDealtItem.setItemMeta(damageDealtMeta);
        gui.setItem(16, damageDealtItem);
        
        // Damage taken
        ItemStack damageTakenItem = new ItemStack(Material.SHIELD);
        ItemMeta damageTakenMeta = damageTakenItem.getItemMeta();
        damageTakenMeta.setDisplayName("§c§lDégâts Subis");
        damageTakenMeta.setLore(Arrays.asList(
            "§7Dégâts totaux subis:",
            "§a" + String.format("%.1f", stats.getDamageTaken())
        ));
        damageTakenItem.setItemMeta(damageTakenMeta);
        gui.setItem(20, damageTakenItem);
        
        // Distance walked
        ItemStack distanceItem = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta distanceMeta = distanceItem.getItemMeta();
        distanceMeta.setDisplayName("§b§lDistance Parcourue");
        distanceMeta.setLore(Arrays.asList(
            "§7Distance totale parcourue:",
            "§a" + String.format("%.1f", stats.getDistanceWalked()) + " blocs"
        ));
        distanceItem.setItemMeta(distanceMeta);
        gui.setItem(22, distanceItem);
        
        // Close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("§c§lFermer");
        closeItem.setItemMeta(closeMeta);
        gui.setItem(26, closeItem);
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        if (event.getView().getTitle().equals("§6§lStatistiques de Session")) {
            event.setCancelled(true);
            
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER) {
                event.getWhoClicked().closeInventory();
            }
        }
    }
}