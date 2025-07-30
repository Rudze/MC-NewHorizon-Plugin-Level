package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.level.LevelsManager;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LevelCommand implements CommandExecutor {
    private final LevelsManager levelsManager;

    public LevelCommand() {
        levelsManager = Main.get().getLevelsManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /level
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Seuls les joueurs peuvent utiliser cette commande !");
                return true;
            }

            final UUID player = ((Player) sender).getUniqueId();
            MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Votre niveau : " + levelsManager.getLevel(player));
            MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Votre expérience actuelle : " + levelsManager.getExp(player));
            MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Expérience prochain niveau : " + levelsManager.expToNextLevel(player));
            return true;
        }

        // /level set <exp> <joueur>
        if (args.length > 1 && args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("level.level.admin")) {
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Vous n'avez pas la permission d'utiliser cette commande.");
                return true;
            }

            final Player target = Bukkit.getPlayer(args.length > 2 ? args[2] : sender.getName());
            if (target == null) {
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Joueur introuvable ou hors ligne.");
                return true;
            }

            try {
                levelsManager.setExp(target.getUniqueId(), Integer.parseInt(args[1]));
                MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "L'expérience de " + target.getName() + " a été définie à " + args[1] + ".");
            } catch (NumberFormatException exception) {
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Veuillez entrer un nombre valide.");
                exception.printStackTrace();
            }

            return true;
        }

        // /level give <joueur> <exp>
        if (args.length >= 3 && args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("level.level.admin")) {
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Vous n'avez pas la permission d'utiliser cette commande.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Joueur introuvable ou hors ligne.");
                return true;
            }

            try {
                int expToAdd = Integer.parseInt(args[2]);
                levelsManager.addExp(target.getUniqueId(), expToAdd);

                // MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Vous avez donné " + expToAdd + " d'expérience à " + target.getName() + ".");
                // MessageUtil.sendMessage(target, Main.get().getPrefixInfo(), "Vous avez reçu " + expToAdd + " d'expérience !");
            } catch (NumberFormatException exception) {
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Veuillez entrer un nombre valide.");
            }

            return true;
        }

        // Commande invalide
        sender.sendMessage("Commande invalide. Utilisez /level, /level set <exp> <player> ou /level give <player> <exp>.");
        return false;
    }

}