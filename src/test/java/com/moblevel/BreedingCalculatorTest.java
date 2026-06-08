package com.moblevel;

import org.junit.Test;
import static org.junit.Assert.*;

public class BreedingCalculatorTest {

    private static final int MAX = 150;
    private static final double NO_MUTATION = 0.99;   // > MUTATION_CHANCE
    private static final double MUTATION = 0.01;       // < MUTATION_CHANCE

    @Test
    public void testAverageOfParents() {
        // (30 + 50) / 2 = 40
        assertEquals(40, BreedingCalculator.calculateChildLevel(30, 50, NO_MUTATION, 15, MAX));
    }

    @Test
    public void testAverageRoundsToNearest() {
        // (30 + 51) / 2 = 40.5 -> 41
        assertEquals(41, BreedingCalculator.calculateChildLevel(30, 51, NO_MUTATION, 15, MAX));
    }

    @Test
    public void testEqualParents() {
        assertEquals(20, BreedingCalculator.calculateChildLevel(20, 20, NO_MUTATION, 15, MAX));
    }

    @Test
    public void testMutationAddsBonus() {
        // avg 40 + bonus 20 = 60
        assertEquals(60, BreedingCalculator.calculateChildLevel(30, 50, MUTATION, 20, MAX));
    }

    @Test
    public void testMutationCappedAtMax() {
        // avg 145 + 20 = 165 -> clamp 150
        assertEquals(MAX, BreedingCalculator.calculateChildLevel(140, 150, MUTATION, 20, MAX));
    }

    @Test
    public void testOneParentNoLevelUsesOther() {
        assertEquals(50, BreedingCalculator.calculateChildLevel(0, 50, NO_MUTATION, 15, MAX));
        assertEquals(30, BreedingCalculator.calculateChildLevel(30, 0, NO_MUTATION, 15, MAX));
    }

    @Test
    public void testBothParentsNoLevelDefaultsToOne() {
        assertEquals(1, BreedingCalculator.calculateChildLevel(0, 0, NO_MUTATION, 15, MAX));
    }

    @Test
    public void testBothNoLevelWithMutation() {
        // base 1 + bonus 15 = 16
        assertEquals(16, BreedingCalculator.calculateChildLevel(0, 0, MUTATION, 15, MAX));
    }

    @Test
    public void testNeverBelowOne() {
        assertTrue(BreedingCalculator.calculateChildLevel(1, 1, NO_MUTATION, 15, MAX) >= 1);
    }

    @Test
    public void testNegativeParentsTreatedAsZero() {
        assertEquals(30, BreedingCalculator.calculateChildLevel(-5, 30, NO_MUTATION, 15, MAX));
    }
}
