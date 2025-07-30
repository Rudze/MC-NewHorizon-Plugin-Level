package fr.rudy.newhorizon.level;

import fr.rudy.newhorizon.Main;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.*;
import java.util.regex.Pattern;

public class PlayerListener implements Listener {
    private final LevelsManager levelsManager;
    private final List<HashMap<String, Integer>> breakBlocks;
    private final int fishExp;
    private final int breedExp;
    private final List<HashMap<String, Integer>> mobKillExp;

    public PlayerListener() {
        levelsManager = Main.get().getLevelsManager();
        breakBlocks = (List<HashMap<String, Integer>>) Main.get().getConfig().getList("levels.break_blocks", new ArrayList<>());
        fishExp = Main.get().getConfig().getInt("levels.fish_exp", 10);
        breedExp = Main.get().getConfig().getInt("levels.breed_exp", 15);
        mobKillExp = (List<HashMap<String, Integer>>) Main.get().getConfig().getList("levels.mob_kill_exp", new ArrayList<>());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        for (HashMap<String, Integer> block : breakBlocks) {
            final Set<String> keys = new HashSet<>(block.keySet());
            if (keys.stream().findFirst().isEmpty()) continue;

            final String name = keys.stream().findFirst().get();
            if (!Pattern.compile(name.replace("*", ".*").toLowerCase())
                    .matcher(event.getBlock().getType().toString().toLowerCase())
                    .matches()
            ) continue;
            keys.remove(name);

            boolean metadataMatch = true;
            for (String metadata : keys) {
                if (!event.getBlock().getBlockData().getAsString().toLowerCase().contains((metadata + "=" + block.get(metadata)).toLowerCase())) {
                    metadataMatch = false;
                    break;
                }
            }
            
            if (metadataMatch) {
                levelsManager.addExp(event.getPlayer().getUniqueId(), block.get(name));
                break; // Exit the loop once we've found a matching block and awarded experience
            }
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        World.Environment destination = event.getTo().getWorld().getEnvironment();

        int netherLevelRequired = Main.get().getConfig().getInt("levels.dimensionRequirements.nether", 20);
        int endLevelRequired = Main.get().getConfig().getInt("levels.dimensionRequirements.end", 40);
        
        int playerLevel = levelsManager.getLevel(player.getUniqueId());

        if (destination == World.Environment.NETHER && playerLevel < netherLevelRequired) {
            event.setCancelled(true);
            String message = Main.get().getConfig().getString("messages.nether_access_denied", "&cVous devez être niveau {required_level} pour accéder au Nether.");
            message = message.replace("{required_level}", String.valueOf(netherLevelRequired));
            player.sendMessage(Main.get().getPrefixError() + message);
        } else if (destination == World.Environment.THE_END && playerLevel < endLevelRequired) {
            event.setCancelled(true);
            String message = Main.get().getConfig().getString("messages.end_access_denied", "&cVous devez être niveau {required_level} pour accéder à l'End.");
            message = message.replace("{required_level}", String.valueOf(endLevelRequired));
            player.sendMessage(Main.get().getPrefixError() + message);
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            levelsManager.addExp(event.getPlayer().getUniqueId(), fishExp);
        }
    }

    @EventHandler
    public void onEntityBreed(EntityBreedEvent event) {
        if (event.getBreeder() instanceof Player) {
            Player breeder = (Player) event.getBreeder();
            levelsManager.addExp(breeder.getUniqueId(), breedExp);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player killer = event.getEntity().getKiller();
            String entityType = event.getEntity().getType().toString().toLowerCase();
            
            for (HashMap<String, Integer> mobExp : mobKillExp) {
                if (mobExp.containsKey(entityType)) {
                    levelsManager.addExp(killer.getUniqueId(), mobExp.get(entityType));
                    break;
                }
            }
        }
    }
}
