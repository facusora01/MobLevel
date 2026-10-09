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

    private Config() {
    }
}
