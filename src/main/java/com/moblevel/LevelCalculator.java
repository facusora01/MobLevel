package com.moblevel;

public class LevelCalculator {

    // bandRoll picks common (1-20) vs high (>20) band; curveRoll^exponent skews the high band toward lower levels.
    public static int rollSpawnLevel(double bandRoll, double curveRoll,
                                     double highChance, double exponent, int maxLevel) {
        if (bandRoll < highChance) {
            double weighted = Math.pow(curveRoll, exponent);
            int level = 21 + (int) (weighted * (maxLevel - 20));
            if (level < 21) level = 21;
            if (level > maxLevel) level = maxLevel;
            return level;
        }
        int level = 1 + (int) (curveRoll * 20);
        if (level < 1) level = 1;
        if (level > 20) level = 20;
        return level;
    }
}
