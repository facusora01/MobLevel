package com.moblevel;

import org.junit.Test;
import static org.junit.Assert.*;

public class LevelCalculatorTest {

    private static final double HIGH_CHANCE = 0.05;
    private static final double EXPONENT = 3.0;
    private static final int MAX = 150;

    @Test
    public void testCommonBandWhenBandRollAboveChance() {
        // bandRoll 0.5 >= 0.05 -> common band 1-20
        int level = LevelCalculator.rollSpawnLevel(0.5, 0.5, HIGH_CHANCE, EXPONENT, MAX);
        assertTrue("Common band must be 1-20", level >= 1 && level <= 20);
    }

    @Test
    public void testCommonBandLowEnd() {
        int level = LevelCalculator.rollSpawnLevel(0.5, 0.0, HIGH_CHANCE, EXPONENT, MAX);
        assertEquals("curveRoll 0 -> level 1", 1, level);
    }

    @Test
    public void testCommonBandHighEnd() {
        int level = LevelCalculator.rollSpawnLevel(0.5, 0.999, HIGH_CHANCE, EXPONENT, MAX);
        assertEquals("curveRoll ~1 -> level 20", 20, level);
    }

    @Test
    public void testHighBandWhenBandRollBelowChance() {
        // bandRoll 0.01 < 0.05 -> high band >20
        int level = LevelCalculator.rollSpawnLevel(0.01, 0.5, HIGH_CHANCE, EXPONENT, MAX);
        assertTrue("High band must be > 20", level > 20);
        assertTrue("High band must be <= max", level <= MAX);
    }

    @Test
    public void testHighBandLowEnd() {
        // curveRoll 0 -> weighted 0 -> level 21 (high band floor)
        int level = LevelCalculator.rollSpawnLevel(0.01, 0.0, HIGH_CHANCE, EXPONENT, MAX);
        assertEquals("High band floor is 21", 21, level);
    }

    @Test
    public void testHighBandExponentMakesHighLevelsRare() {
        // With exponent 3, curveRoll 0.5 -> 0.125 -> low level within the high band
        int level = LevelCalculator.rollSpawnLevel(0.01, 0.5, HIGH_CHANCE, EXPONENT, MAX);
        // 21 + 0.125*130 = 21 + 16 = 37
        assertEquals(37, level);
    }

    @Test
    public void testHighBandTopReachesMax() {
        int level = LevelCalculator.rollSpawnLevel(0.01, 0.999, HIGH_CHANCE, EXPONENT, MAX);
        assertTrue("Near curveRoll 1 should approach max", level >= 140 && level <= MAX);
    }

    @Test
    public void testCommonSkewLiftsMidRoll() {
        // Flat: 0.5 -> 1 + 10 = 11. Skewed 0.75: 0.5^0.75 = 0.5946 -> 1 + 11 = 12.
        assertEquals(11, LevelCalculator.rollSpawnLevel(0.5, 0.5, HIGH_CHANCE, EXPONENT, 1.0, MAX));
        assertEquals(12, LevelCalculator.rollSpawnLevel(0.5, 0.5, HIGH_CHANCE, EXPONENT, 0.75, MAX));
    }

    @Test
    public void testCommonSkewKeepsBandBounds() {
        // The skew must never push a common-band roll outside 1-20.
        assertEquals(1, LevelCalculator.rollSpawnLevel(0.5, 0.0, HIGH_CHANCE, EXPONENT, 0.75, MAX));
        assertEquals(20, LevelCalculator.rollSpawnLevel(0.5, 0.999, HIGH_CHANCE, EXPONENT, 0.75, MAX));
    }

    @Test
    public void testCommonSkewDoesNotTouchHighBand() {
        // Same high-band result with and without the common skew.
        assertEquals(
            LevelCalculator.rollSpawnLevel(0.01, 0.5, HIGH_CHANCE, EXPONENT, 1.0, MAX),
            LevelCalculator.rollSpawnLevel(0.01, 0.5, HIGH_CHANCE, EXPONENT, 0.5, MAX));
    }

    @Test
    public void testDefaultOverloadStaysFlat() {
        // The 5-arg overload must keep the old uniform behaviour.
        assertEquals(
            LevelCalculator.rollSpawnLevel(0.5, 0.5, HIGH_CHANCE, EXPONENT, MAX),
            LevelCalculator.rollSpawnLevel(0.5, 0.5, HIGH_CHANCE, EXPONENT, 1.0, MAX));
    }

    @Test
    public void testZeroHighChanceAlwaysCommon() {
        // highChance 0 -> bandRoll 0.0 is not < 0 -> always common
        int level = LevelCalculator.rollSpawnLevel(0.0, 0.5, 0.0, EXPONENT, MAX);
        assertTrue("With 0 high chance always common", level <= 20);
    }
}
