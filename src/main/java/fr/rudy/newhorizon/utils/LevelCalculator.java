package fr.rudy.newhorizon.utils;

import java.util.HashMap;
import java.util.Map;

public class LevelCalculator {

    public static Map<Integer, Integer> calculateLevelRequirements(int initialExp, double incrementPercent, int maxLevel) {
        Map<Integer, Integer> levelRequirements = new HashMap<>();
        double currentExp = initialExp;

        for (int level = 1; level <= maxLevel; level++) {
            levelRequirements.put(level, (int) Math.round(currentExp));
            currentExp += currentExp * (incrementPercent / 100);
        }

        return levelRequirements;
    }
}
