package com.moblevel;

public class BreedingCalculator {

    /** Defaults estilo shiny: progreso de niveles raro. (Override via Config en runtime.) */
    public static final double MUTATION_CHANCE = 0.01;
    public static final int MUTATION_MIN_BONUS = 3;
    public static final int MUTATION_MAX_BONUS = 8;

    /**
     * Nivel del hijo = promedio de los padres (granja sostenible, sin regresión),
     * con baja chance de mutación que sube el nivel (estilo shiny hunting).
     * Lógica pura/determinista: el roll, la chance y el bonus se pasan como args.
     *
     * @param parentA        nivel del padre A (0 si no tiene tag)
     * @param parentB        nivel del padre B (0 si no tiene tag)
     * @param mutationRoll   valor [0,1); si < mutationChance aplica bonus
     * @param mutationChance probabilidad de mutación
     * @param mutationBonus  bonus a sumar si hay mutación (MIN..MAX)
     * @param maxLevel       cap global
     * @return nivel del hijo, mínimo 1, máximo maxLevel
     */
    public static int calculateChildLevel(int parentA, int parentB, double mutationRoll,
                                          double mutationChance, int mutationBonus, int maxLevel) {
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

        if (mutationRoll < mutationChance) {
            base += mutationBonus;
        }

        if (base < 1) base = 1;
        if (base > maxLevel) base = maxLevel;
        return base;
    }
}
