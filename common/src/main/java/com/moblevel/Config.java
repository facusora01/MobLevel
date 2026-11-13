package com.moblevel;

/**
 * Current config values, as plain fields the shared code can read. Each loader owns
 * the actual config file (with the comments and ranges) and copies its values in here
 * whenever it (re)loads.
 */
public final class Config {
    public static double highLevelChance = 0.020;
    public static double levelRarityExponent = 8.0;
    public static double hostileHighLevelChance = 0.045;
    public static double hostileLevelRarityExponent = 6.0;
    public static double commonLevelSkew = 0.75;
    public static int maxLevel = 150;
    public static double breedingMutationChance = 0.01;
    public static int breedingMutationMinBonus = 3;
    public static int breedingMutationMaxBonus = 8;
    public static boolean uninstallMode = false;
    public static boolean bossMobsHaveLevelLimits = true;
    public static int bossMinLevel = 20;
    public static int bossMaxLevel = 80;

    public static int vanillaLevel = 20;
    public static double lowLevelStatMultiplier = 0.5;
    public static double healthPerLevel = 0.05;
    public static double damagePerLevel = 0.02;
    public static double lowLevelDropChance = 0.25;
    public static double lootPerLevel = 0.02;
    public static double maxLevelLootBonus = 3.0;
    public static double xpPerLevel = 0.01;
    public static double maxLevelXpBonus = 2.0;

    public static boolean armorEnabled = false;
    public static double armorPerLevel = 0.1;
    public static double armorMax = 20.0;

    public static boolean distanceScalingEnabled = false;
    public static int blocksPerLevel = 100;
    public static int distanceMaxBonus = 50;

    private Config() {
    }
}
