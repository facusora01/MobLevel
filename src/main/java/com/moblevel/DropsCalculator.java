package com.moblevel;

import java.util.Set;

public class DropsCalculator {
    // Mounts and trades keep this fixed level. The configurable vanillaLevel below only
    // drives the health/damage/loot/XP math.
    public static final int VANILLA_LEVEL = 20;

    // Live scaling values. Defaults match the config defaults so unit tests (no loaded config)
    // behave as before; Config.apply() overwrites them whenever the config file loads.
    static int vanillaLevel = 20;
    static double lowLevelStatMultiplier = 0.5;
    static double healthPerLevel = 0.05;
    static double damagePerLevel = 0.02;
    static double lowLevelDropChance = 0.25;
    static double lootPerLevel = 0.02;
    static double maxLevelLootBonus = 3.0;
    static double xpPerLevel = 0.01;
    static double maxLevelXpBonus = 2.0;
    static int maxLevel = 150;

    // Health/damage with the default config: lvl 1 = 0.5x, lvl 20 = 1.0x, lvl 80 = 4.0x, lvl 150 = 7.5x.
    public static double getStatMultiplier(int level) {
        if (level <= 0) {
            return 1.0;
        }
        if (level >= vanillaLevel) {
            return 1.0 + ((level - vanillaLevel) * healthPerLevel);
        }
        return ramp(level, lowLevelStatMultiplier);
    }

    // Linear ramp from `low` at level 1 to 1.0 at the configured vanilla level.
    private static double ramp(int level, double low) {
        return low + ((level - 1) / (double) (vanillaLevel - 1)) * (1.0 - low);
    }

    // Mounts (horses, donkeys, mules, llamas, camels...) cap at 25 hearts, and are never slower
    // than the slowest vanilla horse (a bit faster than a walking player).
    public static final double MOUNT_MAX_HEALTH = 50.0;
    public static final double MOUNT_MIN_SPEED = 0.1125;

    // Mount health: same as getStatMultiplier up to level 20, then up to 1.67x at level 150,
    // which takes the best vanilla horse (30 health) to MOUNT_MAX_HEALTH.
    public static double getMountHealthMultiplier(int level) {
        if (level < VANILLA_LEVEL) {
            return 0.5 + ((level - 1) / 19.0) * 0.5;
        }
        return 1.0 + ((level - VANILLA_LEVEL) / 130.0) * (2.0 / 3.0);
    }

    // Mount speed/jump (horses, donkeys, llamas, camels...): lvl 1 = 0.75x, lvl 20 = 1.0x,
    // lvl 150 = 1.5x. Much flatter than health: a 7.5x horse would outrun chunk loading.
    public static double getMountMultiplier(int level) {
        if (level <= 0) {
            return 1.0;
        }
        if (level >= VANILLA_LEVEL) {
            return 1.0 + ((level - VANILLA_LEVEL) / 130.0) * 0.5;
        }
        return 0.75 + ((level - 1) / 19.0) * 0.25;
    }

    public static float getDamageMultiplier(int level) {
        if (level <= 0) {
            return 1.0f;
        }
        if (level >= vanillaLevel) {
            return 1.0f + ((level - vanillaLevel) * (float) damagePerLevel);
        }
        return (float) ramp(level, lowLevelStatMultiplier);
    }

    // Sub-vanilla drop chance: lvl 1 = 0.25 ramping to 1.0 at the vanilla level and above.
    public static double getDropChance(int level) {
        if (level <= 0 || level >= vanillaLevel) {
            return 1.0;
        }
        return ramp(level, lowLevelDropChance);
    }

    public static int calculateDropCount(int originalCount, int level) {
        if (level <= 0) {
            return originalCount;
        }
        if (level < vanillaLevel) {
            return 1;
        }

        float multiplier = 1.0f + ((level - vanillaLevel) * (float) lootPerLevel);

        if (level >= maxLevel) {
            multiplier += (float) maxLevelLootBonus;
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
        if (level < vanillaLevel) {
            return Math.max(1, Math.round(originalXp * (float) getStatMultiplier(level)));
        }

        double xpMultiplier = 1.0 + ((level - vanillaLevel) * xpPerLevel);

        if (level >= maxLevel) {
            xpMultiplier += maxLevelXpBonus;
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
