package com.moblevel;

// Plain copies of the Config values, refreshed by Config.bake() on config load/reload. The pure
// calculators read these so they stay testable without a loaded ModConfigSpec.
public final class Live {
    public static int maxLevel = 150;
    public static int vanillaLevel = 20;
    public static double lowLevelStatMultiplier = 0.5;
    public static double healthPerLevel = 0.05;
    public static double damagePerLevel = 0.02;
    public static double lowLevelDropChance = 0.25;
    public static double lootPerLevel = 0.02;
    public static double maxLevelLootBonus = 3.0;
    public static double xpPerLevel = 0.01;
    public static double maxLevelXpBonus = 2.0;

    private Live() {
    }
}
