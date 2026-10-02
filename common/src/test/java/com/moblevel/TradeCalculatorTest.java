package com.moblevel;

import org.junit.Test;
import static org.junit.Assert.*;

public class TradeCalculatorTest {

    @Test
    public void testLevel20IsVanilla() {
        assertEquals(1.0, TradeCalculator.getPriceMultiplier(20), 0.0001);
        assertEquals(1.0, TradeCalculator.getResultMultiplier(20), 0.0001);
        assertEquals(1.0, TradeCalculator.getUsesMultiplier(20), 0.0001);
        assertEquals(0, TradeCalculator.getEnchantmentBonus(20));
    }

    @Test
    public void testUnleveledIsVanilla() {
        assertEquals(1.0, TradeCalculator.getPriceMultiplier(0), 0.0001);
        assertEquals(1.0, TradeCalculator.getResultMultiplier(0), 0.0001);
        assertEquals(1.0, TradeCalculator.getUsesMultiplier(0), 0.0001);
        assertEquals(0, TradeCalculator.getEnchantmentBonus(0));
    }

    @Test
    public void testCurveEnds() {
        assertEquals("Level 1 pays 1.5x", 1.5, TradeCalculator.getPriceMultiplier(1), 0.0001);
        assertEquals("Level 150 pays half", 0.5, TradeCalculator.getPriceMultiplier(150), 0.0001);
        assertEquals("Level 150 gives 2x", 2.0, TradeCalculator.getResultMultiplier(150), 0.0001);
        assertEquals("Level 1 has half the uses", 0.5, TradeCalculator.getUsesMultiplier(1), 0.0001);
        assertEquals("Level 150 has 3x the uses", 3.0, TradeCalculator.getUsesMultiplier(150), 0.0001);
        assertEquals(-1, TradeCalculator.getEnchantmentBonus(9));
        assertEquals(0, TradeCalculator.getEnchantmentBonus(10));
        assertEquals(0, TradeCalculator.getEnchantmentBonus(74));
        assertEquals(1, TradeCalculator.getEnchantmentBonus(75));
    }

    @Test
    public void testTradesOnlyGetBetterWithLevel() {
        for (int level = 1; level < 150; level++) {
            assertTrue(TradeCalculator.getPriceMultiplier(level + 1) < TradeCalculator.getPriceMultiplier(level));
            assertTrue(TradeCalculator.getResultMultiplier(level + 1) >= TradeCalculator.getResultMultiplier(level));
            assertTrue(TradeCalculator.getUsesMultiplier(level + 1) > TradeCalculator.getUsesMultiplier(level));
        }
    }

    @Test
    public void testScaleCountStaysInStack() {
        assertEquals("Never below 1", 1, TradeCalculator.scaleCount(1, 0.5, 64, 0.9));
        assertEquals("Capped at the stack size", 64, TradeCalculator.scaleCount(40, 2.0, 64, 0.0));
        assertEquals("Unstackable stays 1", 1, TradeCalculator.scaleCount(1, 2.0, 1, 0.0));
        assertEquals("Whole results ignore the roll", 10, TradeCalculator.scaleCount(20, 0.5, 64, 0.0));
    }

    @Test
    public void testScaleCountRoundsByProbability() {
        assertEquals("1 at 1.5x, low roll rounds up", 2, TradeCalculator.scaleCount(1, 1.5, 64, 0.49));
        assertEquals("1 at 1.5x, high roll rounds down", 1, TradeCalculator.scaleCount(1, 1.5, 64, 0.51));
        // Over evenly spread rolls the average matches count x multiplier.
        int n = 1000, sum = 0;
        for (int i = 0; i < n; i++) sum += TradeCalculator.scaleCount(3, 1.5, 64, (i + 0.5) / n);
        assertEquals(4.5, sum / (double) n, 0.01);
    }
}
