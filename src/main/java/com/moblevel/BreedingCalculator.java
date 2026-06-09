package com.moblevel;

public class BreedingCalculator {

    // Shiny-style defaults; overridden via Config at runtime.
    public static final double MUTATION_CHANCE = 0.01;
    public static final int MUTATION_MIN_BONUS = 3;
    public static final int MUTATION_MAX_BONUS = 8;

    // Child level = average of parents, with a low chance of a mutation that raises it.
    public static int calculateChildLevel(int parentA, int parentB, double mutationRoll,
                                          double mutationChance, int mutationBonus, int maxLevel) {
        int validA = Math.max(0, parentA);
        int validB = Math.max(0, parentB);

        int base;
        if (validA == 0 && validB == 0) {
            base = 1;
        } else if (validA == 0) {
            base = validB;
        } else if (validB == 0) {
            base = validA;
        } else {
            base = Math.round((validA + validB) / 2.0f);
        }

        if (mutationRoll < mutationChance) {
            base += mutationBonus;
        }

        if (base < 1) base = 1;
        if (base > maxLevel) base = maxLevel;
        return base;
    }
}
