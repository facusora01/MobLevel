package com.moblevel;

import java.util.Set;

public class DropsCalculator {
    // Health/damage: lvl 1 = 0.5x, lvl 20 = 1.0x, lvl 80 = 4.0x, lvl 150 = 7.5x.
    public static double getStatMultiplier(int level) {
        if (level <= 0) {
            return 1.0;
        }
        if (level >= Live.vanillaLevel) {
            return 1.0 + ((level - Live.vanillaLevel) * Live.healthPerLevel);
        }
        return ramp(level, Live.lowLevelStatMultiplier);
    }

    // Linear ramp from `low` at level 1 to 1.0 at the configured vanilla level.
    private static double ramp(int level, double low) {
        return low + ((level - 1) / (double) (Live.vanillaLevel - 1)) * (1.0 - low);
    }

    public static float getDamageMultiplier(int level) {
        if (level <= 0) {
            return 1.0f;
        }
        if (level >= Live.vanillaLevel) {
            return 1.0f + ((level - Live.vanillaLevel) * (float) Live.damagePerLevel);
        }
        return (float) ramp(level, Live.lowLevelStatMultiplier);
    }

    // Sub-vanilla drop chance: lvl 1 = 0.25 ramping to 1.0 at the vanilla level and above.
    public static double getDropChance(int level) {
        if (level <= 0 || level >= Live.vanillaLevel) {
            return 1.0;
        }
        return ramp(level, Live.lowLevelDropChance);
    }

    public static int calculateDropCount(int originalCount, int level) {
        if (level <= 0) {
            return originalCount;
        }
        if (level < Live.vanillaLevel) {
            return 1;
        }

        float multiplier = 1.0f + ((level - Live.vanillaLevel) * (float) Live.lootPerLevel);

        if (level >= Live.maxLevel) {
            multiplier += (float) Live.maxLevelLootBonus;
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
        if (level < Live.vanillaLevel) {
            return Math.max(1, Math.round(originalXp * (float) getStatMultiplier(level)));
        }

        double xpMultiplier = 1.0 + ((level - Live.vanillaLevel) * Live.xpPerLevel);

        if (level >= Live.maxLevel) {
            xpMultiplier += Live.maxLevelXpBonus;
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
