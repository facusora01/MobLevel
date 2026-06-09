package com.moblevel;

import java.util.Set;

public class DropsCalculator {
    private static final double LOOT_MULTIPLIER_PER_LEVEL = 0.02;
    private static final double LEVEL_150_BONUS = 3.0;

    // Stats equal vanilla at this level; below is weaker, above scales up.
    public static final int VANILLA_LEVEL = 20;

    // Health/damage: lvl 1 = 0.5x, lvl 20 = 1.0x, lvl 80 = 4.0x, lvl 150 = 7.5x.
    public static double getStatMultiplier(int level) {
        if (level <= 0) {
            return 1.0;
        }
        if (level >= VANILLA_LEVEL) {
            return 1.0 + ((level - VANILLA_LEVEL) * 0.05);
        }
        return 0.5 + ((level - 1) / 19.0) * 0.5;
    }

    public static float getDamageMultiplier(int level) {
        if (level <= 0) {
            return 1.0f;
        }
        if (level >= VANILLA_LEVEL) {
            return 1.0f + ((level - VANILLA_LEVEL) * 0.02f);
        }
        return (float) getStatMultiplier(level);
    }

    // Sub-vanilla drop chance: lvl 1 = 0.25 ramping to 1.0 at lvl 20+.
    public static double getDropChance(int level) {
        if (level <= 0 || level >= VANILLA_LEVEL) {
            return 1.0;
        }
        return 0.25 + ((level - 1) / 19.0) * 0.75;
    }

    public static int calculateDropCount(int originalCount, int level) {
        if (level <= 0) {
            return originalCount;
        }
        if (level < VANILLA_LEVEL) {
            return 1;
        }

        float multiplier = 1.0f + ((level - VANILLA_LEVEL) * (float) LOOT_MULTIPLIER_PER_LEVEL);

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
        if (level < VANILLA_LEVEL) {
            return Math.max(1, Math.round(originalXp * (float) getStatMultiplier(level)));
        }

        double xpMultiplier = 1.0 + ((level - VANILLA_LEVEL) * 0.01);

        if (level >= 150) {
            xpMultiplier += 2.0;
        }

        return Math.round(originalXp * (float) xpMultiplier);
    }

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
