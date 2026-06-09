package com.moblevel;

import org.junit.Test;
import static org.junit.Assert.*;

public class BreedingCalculatorTest {

    private static final int MAX = 150;
    private static final double CHANCE = 0.01;
    private static final double NO_MUTATION = 0.99;   // > CHANCE
    private static final double MUTATION = 0.005;      // < CHANCE

    @Test
    public void testAverageOfParents() {
        // (30 + 50) / 2 = 40
        assertEquals(40, BreedingCalculator.calculateChildLevel(30, 50, NO_MUTATION, CHANCE, 5, MAX));
    }

    @Test
    public void testAverageRoundsToNearest() {
        // (30 + 51) / 2 = 40.5 -> 41
        assertEquals(41, BreedingCalculator.calculateChildLevel(30, 51, NO_MUTATION, CHANCE, 5, MAX));
    }

    @Test
    public void testEqualParentsSustainsLevel() {
        // Dos lvl 100 -> hijo 100 (granja sostenible, sin regresión)
        assertEquals(100, BreedingCalculator.calculateChildLevel(100, 100, NO_MUTATION, CHANCE, 5, MAX));
    }

    @Test
    public void testMutationAddsSmallBonus() {
        // avg 100 + bonus 8 = 108 (shiny-style climb)
        assertEquals(108, BreedingCalculator.calculateChildLevel(100, 100, MUTATION, CHANCE, 8, MAX));
    }

    @Test
    public void testMutationCappedAtMax() {
        // avg 148 + 8 = 156 -> clamp 150
        assertEquals(MAX, BreedingCalculator.calculateChildLevel(146, 150, MUTATION, CHANCE, 8, MAX));
    }

    @Test
    public void testOneParentNoLevelUsesOther() {
        assertEquals(50, BreedingCalculator.calculateChildLevel(0, 50, NO_MUTATION, CHANCE, 5, MAX));
        assertEquals(30, BreedingCalculator.calculateChildLevel(30, 0, NO_MUTATION, CHANCE, 5, MAX));
    }

    @Test
    public void testBothParentsNoLevelDefaultsToOne() {
        assertEquals(1, BreedingCalculator.calculateChildLevel(0, 0, NO_MUTATION, CHANCE, 5, MAX));
    }

    @Test
    public void testBothNoLevelWithMutation() {
        // base 1 + bonus 5 = 6
        assertEquals(6, BreedingCalculator.calculateChildLevel(0, 0, MUTATION, CHANCE, 5, MAX));
    }

    @Test
    public void testNeverBelowOne() {
        assertTrue(BreedingCalculator.calculateChildLevel(1, 1, NO_MUTATION, CHANCE, 5, MAX) >= 1);
    }

    @Test
    public void testNegativeParentsTreatedAsZero() {
        assertEquals(30, BreedingCalculator.calculateChildLevel(-5, 30, NO_MUTATION, CHANCE, 5, MAX));
    }

    @Test
    public void testMixedParentsDragTowardAverage() {
        // 100 y 20 -> 60: mezclar con bajos arrastra hacia abajo
        assertEquals(60, BreedingCalculator.calculateChildLevel(100, 20, NO_MUTATION, CHANCE, 5, MAX));
    }
}
