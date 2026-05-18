package com.moblevel;

import static org.junit.Assert.*;
import org.junit.Test;

public class ColorAndLootTest {

    @Test
    public void testGreenLevel() {
        assertEquals("GREEN", getColorForLevel(1));
        assertEquals("GREEN", getColorForLevel(20));
        assertEquals("GREEN", getColorForLevel(49));
    }

    @Test
    public void testAquaLevel() {
        assertEquals("AQUA", getColorForLevel(50));
        assertEquals("AQUA", getColorForLevel(75));
        assertEquals("AQUA", getColorForLevel(99));
    }

    @Test
    public void testYellowLevel() {
        assertEquals("YELLOW", getColorForLevel(100));
        assertEquals("YELLOW", getColorForLevel(125));
        assertEquals("YELLOW", getColorForLevel(129));
    }

    @Test
    public void testRedLevel() {
        assertEquals("RED", getColorForLevel(130));
        assertEquals("RED", getColorForLevel(145));
        assertEquals("RED", getColorForLevel(149));
    }

    @Test
    public void testDarkPurpleLevel() {
        assertEquals("DARK_PURPLE", getColorForLevel(150));
        assertEquals("DARK_PURPLE", getColorForLevel(200));
    }

    @Test
    public void testLootMultiplierLevel1() {
        int result = calculateLootMultiplier(1, 1);
        assertEquals(1, result);
    }

    @Test
    public void testLootMultiplierLevel50() {
        int result = calculateLootMultiplier(50, 1);
        assertEquals(2, result);
    }

    @Test
    public void testLootMultiplierLevel100() {
        int result = calculateLootMultiplier(100, 1);
        assertEquals(3, result);
    }

    @Test
    public void testLootMultiplierLevel150WithBonus() {
        int result = calculateLootMultiplier(150, 1);
        assertEquals(7, result);
    }

    @Test
    public void testLootMultiplierWithStack() {
        int result = calculateLootMultiplier(100, 64);
        assertEquals(192, result);
    }

    @Test
    public void testLevel149vs150Boundary() {
        int level149 = calculateLootMultiplier(149, 1);
        int level150 = calculateLootMultiplier(150, 1);

        assertEquals(4, level149);
        assertEquals(7, level150);
        assertTrue(level150 > level149);
    }

    private String getColorForLevel(int level) {
        if (level >= 150) return "DARK_PURPLE";
        if (level >= 130) return "RED";
        if (level >= 100) return "YELLOW";
        if (level >= 50) return "AQUA";
        return "GREEN";
    }

    private int calculateLootMultiplier(int level, int originalCount) {
        double lootMultiplierPerLevel = 0.02;
        float multiplier = 1.0f + (level * (float) lootMultiplierPerLevel);

        if (level >= 150) {
            multiplier += 3.0f;
        }

        return Math.round(originalCount * multiplier);
    }
}
