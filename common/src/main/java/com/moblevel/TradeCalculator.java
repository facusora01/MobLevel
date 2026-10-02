package com.moblevel;

// How a villager's or wandering trader's level changes the trades it offers.
// Level 20 is vanilla; level 0 (no level, e.g. uninstall mode) leaves trades untouched.
public final class TradeCalculator {
    private TradeCalculator() {
    }

    // What the player pays: lvl 1 = 1.5x, lvl 20 = 1.0x, lvl 150 = 0.5x.
    public static double getPriceMultiplier(int level) {
        if (level <= 0) return 1.0;
        if (level < DropsCalculator.VANILLA_LEVEL) return 1.5 - ((level - 1) / 19.0) * 0.5;
        return 1.0 - ((level - DropsCalculator.VANILLA_LEVEL) / 130.0) * 0.5;
    }

    // What the player gets, for stackable results: vanilla up to lvl 20, then up to 2x at lvl 150.
    public static double getResultMultiplier(int level) {
        if (level < DropsCalculator.VANILLA_LEVEL) return 1.0;
        return 1.0 + (level - DropsCalculator.VANILLA_LEVEL) / 130.0;
    }

    // Uses before the trade locks: lvl 1 = 0.5x, lvl 20 = 1.0x, lvl 150 = 3.0x.
    public static double getUsesMultiplier(int level) {
        if (level <= 0) return 1.0;
        if (level < DropsCalculator.VANILLA_LEVEL) return 0.5 + ((level - 1) / 19.0) * 0.5;
        return 1.0 + ((level - DropsCalculator.VANILLA_LEVEL) / 130.0) * 2.0;
    }

    // Levels added to each enchantment on a traded book or item: -1 below lvl 10, +1 from lvl 75.
    // Callers keep the result inside the enchantment's own vanilla range.
    public static int getEnchantmentBonus(int level) {
        if (level <= 0) return 0;
        if (level < 10) return -1;
        if (level >= 75) return 1;
        return 0;
    }

    // Scales an item count, kept within 1..maxStack. The fraction rounds up with its own
    // probability (roll in [0, 1)), so small counts follow the multiplier on average: 1 item
    // at 1.5x is 1 or 2 half the time each, instead of always 2.
    public static int scaleCount(int count, double multiplier, int maxStack, double roll) {
        double scaled = count * multiplier;
        int whole = (int) Math.floor(scaled);
        int rounded = whole + (roll < scaled - whole ? 1 : 0);
        return Math.max(1, Math.min(maxStack, rounded));
    }
}
