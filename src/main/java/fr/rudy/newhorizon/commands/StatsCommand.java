package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.stats.SessionStatManager;
import fr.rudy.newhorizon.stats.StatsGUIListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {
    
    private final StatsGUIListener statsGUI;
    private final SessionStatManager statManager;
    
    public StatsCommand(StatsGUIListener statsGUI, SessionStatManager statManager) {
        this.statsGUI = statsGUI;
        this.statManager = statManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.get().getPrefixError() + "Seuls les joueurs peuvent utiliser cette commande !");
            return true;
        }
        
        Player player = (Player) sender;
        
        // /stats - Open stats GUI
        if (args.length == 0) {
            statsGUI.openStatsGUI(player);
            return true;
        }
        
        // /stats reset - Reset own stats (if player has permission)
        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            if (!player.hasPermission("level.stats.reset")) {
                player.sendMessage(Main.get().getPrefixError() + "Vous n'avez pas la permission de réinitialiser vos statistiques.");
                return true;
            }
            
            statManager.removePlayer(player);
            statManager.addPlayer(player);
            player.sendMessage(Main.get().getPrefixInfo() + "Vos statistiques de session ont été réinitialisées.");
            return true;
        }
        
        // /stats reset <player> - Reset another player's stats (admin only)
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            if (!player.hasPermission("level.stats.admin")) {
                player.sendMessage(Main.get().getPrefixError() + "Vous n'avez pas la permission d'utiliser cette commande.");
                return true;
            }
            
            Player target = player.getServer().getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(Main.get().getPrefixError() + "Joueur introuvable ou hors ligne.");
                return true;
            }
            
            statManager.removePlayer(target);
            statManager.addPlayer(target);
            player.sendMessage(Main.get().getPrefixInfo() + "Les statistiques de " + target.getName() + " ont été réinitialisées.");
            target.sendMessage(Main.get().getPrefixInfo() + "Vos statistiques de session ont été réinitialisées par un administrateur.");
            return true;
        }
        
        // Invalid usage
        player.sendMessage(Main.get().getPrefixError() + "Usage: /stats [reset] [joueur]");
        return true;
    }
}