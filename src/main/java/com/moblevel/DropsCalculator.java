package com.moblevel;

import java.util.Set;

public class DropsCalculator {
    private static final double LOOT_MULTIPLIER_PER_LEVEL = 0.02;
    private static final double LEVEL_150_BONUS = 3.0;

    public static int calculateDropCount(int originalCount, int level) {
        if (level <= 0) {
            return originalCount;
        }

        float multiplier = 1.0f + (level * (float) LOOT_MULTIPLIER_PER_LEVEL);

        if (level >= 150) {
            multiplier += (float) LEVEL_150_BONUS;
        }

        return Math.round(originalCount * multiplier);
    }

    public static boolean shouldIncreaseDrops(int originalCount, int newCount) {
        return newCount > originalCount;
    }

    public static int calculateExperienceDrop(int originalXp, int level) {
        if (level <= 0) {
            return originalXp;
        }

        double xpMultiplier = 1.0 + (level * 0.01);

        if (level >= 150) {
            xpMultiplier += 2.0;
        }

        return Math.round(originalXp * (float) xpMultiplier);
    }

    /**
     * Extract level from mob tags. Returns 0 if no lvl:X tag found.
     */
    public static int getLevelFromTags(Set<String> tags) {
        for (String tag : tags) {
            if (tag.startsWith("lvl:")) {
                try {
                    return Integer.parseInt(tag.substring(4));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }
}
