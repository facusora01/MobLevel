package com.moblevel;

public class LevelCalculator {

    /** Uniform common band, kept so existing callers and tests keep their meaning. */
    public static int rollSpawnLevel(double bandRoll, double curveRoll,
                                     double highChance, double exponent, int maxLevel) {
        return rollSpawnLevel(bandRoll, curveRoll, highChance, exponent, 1.0, maxLevel);
    }

    // bandRoll picks common (1-20) vs high (>20) band.
    // In the high band, curveRoll^exponent skews toward the low end, so high levels stay rare.
    // In the common band, curveRoll^commonSkew does the opposite when commonSkew < 1: it lifts
    // the roll, thinning out the weakest levels without ever leaving the 1-20 range.
    public static int rollSpawnLevel(double bandRoll, double curveRoll,
                                     double highChance, double exponent,
                                     double commonSkew, int maxLevel) {
        if (bandRoll < highChance) {
            double weighted = Math.pow(curveRoll, exponent);
            int level = 21 + (int) (weighted * (maxLevel - 20));
            if (level < 21) level = 21;
            if (level > maxLevel) level = maxLevel;
            return level;
        }
        double weighted = Math.pow(curveRoll, commonSkew);
        int level = 1 + (int) (weighted * 20);
        if (level < 1) level = 1;
        if (level > 20) level = 20;
        return level;
    }
}
