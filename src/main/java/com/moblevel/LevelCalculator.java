package com.moblevel;

public class LevelCalculator {

    /**
     * Nivel de spawn natural.
     * <p>
     * {@code bandRoll} decide la banda: común (1-20, mayoría) vs alta (>20, rara).
     * {@code curveRoll} ubica dentro de la banda; en la banda alta se eleva a
     * {@code exponent} para que los niveles más altos sean cada vez más raros.
     *
     * @param bandRoll   [0,1); si < highChance cae en banda alta
     * @param curveRoll  [0,1); posición dentro de la banda
     * @param highChance probabilidad de banda alta (ej 0.05 = 5%)
     * @param exponent   curva de rareza de la banda alta (mayor = altos más raros)
     * @param maxLevel   nivel máximo global
     * @return nivel 1..maxLevel
     */
    public static int rollSpawnLevel(double bandRoll, double curveRoll,
                                     double highChance, double exponent, int maxLevel) {
        if (bandRoll < highChance) {
            double weighted = Math.pow(curveRoll, exponent);
            int level = 21 + (int) (weighted * (maxLevel - 20));
            if (level < 21) level = 21;
            if (level > maxLevel) level = maxLevel;
            return level;
        }
        int level = 1 + (int) (curveRoll * 20); // 1..20
        if (level < 1) level = 1;
        if (level > 20) level = 20;
        return level;
    }
}
