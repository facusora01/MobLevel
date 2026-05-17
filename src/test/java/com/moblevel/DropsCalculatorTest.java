package com.moblevel;

import static org.junit.Assert.*;
import org.junit.Test;

public class DropsCalculatorTest {

    @Test
    public void testNoLevelNoMultiplier() {
        int result = DropsCalculator.calculateDropCount(10, 0);
        assertEquals(10, result);
    }

    @Test
    public void testLevel1() {
        int result = DropsCalculator.calculateDropCount(1, 1);
        assertEquals(1, result);
    }

    @Test
    public void testLevel50() {
        int result = DropsCalculator.calculateDropCount(1, 50);
        assertEquals(2, result);
    }

    @Test
    public void testLevel100() {
        int result = DropsCalculator.calculateDropCount(1, 100);
        assertEquals(3, result);
    }

    @Test
    public void testLevel130() {
        int result = DropsCalculator.calculateDropCount(1, 130);
        assertEquals(4, result);
    }

    @Test
    public void testLevel150WithBonus() {
        int result = DropsCalculator.calculateDropCount(1, 150);
        assertEquals(7, result);
    }

    @Test
    public void testLevel150Stack64() {
        int result = DropsCalculator.calculateDropCount(64, 150);
        assertEquals(448, result);
    }

    @Test
    public void testStack10Level50() {
        int result = DropsCalculator.calculateDropCount(10, 50);
        assertEquals(20, result);
    }

    @Test
    public void testShouldIncreaseDrops() {
        assertTrue(DropsCalculator.shouldIncreaseDrops(1, 2));
        assertTrue(DropsCalculator.shouldIncreaseDrops(10, 20));
        assertFalse(DropsCalculator.shouldIncreaseDrops(10, 10));
        assertFalse(DropsCalculator.shouldIncreaseDrops(20, 10));
    }

    @Test
    public void testLevel75() {
        int result = DropsCalculator.calculateDropCount(1, 75);
        assertEquals(3, result);
    }

    @Test
    public void testLevel150Boundary() {
        int result149 = DropsCalculator.calculateDropCount(1, 149);
        assertEquals(4, result149);

        int result150 = DropsCalculator.calculateDropCount(1, 150);
        assertEquals(7, result150);
    }
}
