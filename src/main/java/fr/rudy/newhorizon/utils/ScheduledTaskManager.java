package fr.rudy.newhorizon.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class ScheduledTaskManager {

    private final JavaPlugin plugin;

    private static class ScheduledCommand {
        LocalTime time;
        Set<DayOfWeek> days;
        String command;

        ScheduledCommand(LocalTime time, Set<DayOfWeek> days, String command) {
            this.time = time;
            this.days = days;
            this.command = command;
        }
    }

    private final List<ScheduledCommand> scheduledCommands = new ArrayList<>();

    public ScheduledTaskManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    private void loadFromConfig() {
        List<Map<?, ?>> configList = plugin.getConfig().getMapList("scheduled-commands");

        for (Map<?, ?> map : configList) {
            try {
                String timeString = (String) map.get("time"); // "21:30"
                List<Integer> dayInts = (List<Integer>) map.get("days");
                String command = (String) map.get("command");

                LocalTime time = LocalTime.parse(timeString);
                Set<DayOfWeek> days = new HashSet<>();

                for (int day : dayInts) {
                    if (day == -1 || day == 7) {
                        days.addAll(Arrays.asList(DayOfWeek.values()));
                    } else {
                        days.add(DayOfWeek.of((day + 1) % 7 == 0 ? 7 : (day + 1) % 7)); // Adjust because DayOfWeek starts at 1 (Monday)
                    }
                }

                scheduledCommands.add(new ScheduledCommand(time, days, command));
            } catch (Exception e) {
                plugin.getLogger().warning("Erreur dans la tâche planifiée: " + map);
                e.printStackTrace();
            }
        }
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            LocalDateTime now = LocalDateTime.now();
            LocalTime currentTime = now.toLocalTime().withSecond(0).withNano(0);
            DayOfWeek currentDay = now.getDayOfWeek();

            for (ScheduledCommand task : scheduledCommands) {
                if (task.time.equals(currentTime) && task.days.contains(currentDay)) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), task.command);
                        plugin.getLogger().info("⏰ Commande planifiée exécutée : " + task.command);
                    });
                }
            }
        }, 20L, 1200L); // every 60 seconds
    }
}
