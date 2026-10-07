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

    // An offer's vanilla numbers, kept in an entity tag so uninstall can restore them exactly:
    // "ml_offer:<index>:<costA>,<costB>,<result>,<maxUses>[,<enchantment id>=<level>...]".
    // Enchantments go by id, so restoring them never depends on their order.
    public static final String OFFER_TAG = "ml_offer:";

    public static String encodeOffer(int index, int[] values, java.util.Map<String, Integer> enchantments) {
        StringBuilder tag = new StringBuilder(OFFER_TAG).append(index).append(':');
        for (int i = 0; i < values.length; i++) {
            if (i > 0) tag.append(',');
            tag.append(values[i]);
        }
        enchantments.forEach((id, level) -> tag.append(',').append(id).append('=').append(level));
        return tag.toString();
    }

    // The offer index of a tag made by encodeOffer, or -1 for any other tag.
    public static int offerIndex(String tag) {
        if (!tag.startsWith(OFFER_TAG)) return -1;
        try {
            return Integer.parseInt(tag.substring(OFFER_TAG.length(), tag.indexOf(':', OFFER_TAG.length())));
        } catch (RuntimeException e) {
            return -1;
        }
    }

    // costA, costB, result count and max uses.
    public static int[] offerValues(String tag) {
        String[] parts = offerParts(tag);
        int[] values = new int[4];
        for (int i = 0; i < 4; i++) values[i] = Integer.parseInt(parts[i]);
        return values;
    }

    // Enchantment id -> vanilla level.
    public static java.util.Map<String, Integer> offerEnchantments(String tag) {
        java.util.Map<String, Integer> enchantments = new java.util.LinkedHashMap<>();
        for (String part : offerParts(tag)) {
            int eq = part.indexOf('=');
            if (eq > 0) enchantments.put(part.substring(0, eq), Integer.parseInt(part.substring(eq + 1)));
        }
        return enchantments;
    }

    private static String[] offerParts(String tag) {
        return tag.substring(tag.indexOf(':', OFFER_TAG.length()) + 1).split(",");
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
