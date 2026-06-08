package com.moblevel;

public class BreedingCalculator {

    /** Probabilidad de mutación (hijo notablemente más fuerte que el promedio). */
    public static final double MUTATION_CHANCE = 0.05;
    /** Rango del bonus de mutación. */
    public static final int MUTATION_MIN_BONUS = 10;
    public static final int MUTATION_MAX_BONUS = 20;

    /**
     * Nivel base del hijo = promedio de los padres, con chance de mutación.
     * Lógica pura/determinista para tests: el roll y el bonus se pasan como args.
     *
     * @param parentA       nivel del padre A (0 si no tiene tag)
     * @param parentB       nivel del padre B (0 si no tiene tag)
     * @param mutationRoll  valor [0,1); si < MUTATION_CHANCE aplica bonus
     * @param mutationBonus bonus a sumar si hay mutación (MIN..MAX)
     * @param maxLevel      cap global
     * @return nivel del hijo, mínimo 1, máximo maxLevel
     */
    public static int calculateChildLevel(int parentA, int parentB, double mutationRoll,
                                          int mutationBonus, int maxLevel) {
        int validA = Math.max(0, parentA);
        int validB = Math.max(0, parentB);

        int base;
        if (validA == 0 && validB == 0) {
            base = 1;                       // sin info: hijo débil
        } else if (validA == 0) {
            base = validB;
        } else if (validB == 0) {
            base = validA;
        } else {
            base = Math.round((validA + validB) / 2.0f);
        }

        if (mutationRoll < MUTATION_CHANCE) {
            base += mutationBonus;
        }

        if (base < 1) base = 1;
        if (base > maxLevel) base = maxLevel;
        return base;
    }
}
