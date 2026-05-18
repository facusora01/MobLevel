package com.moblevel;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class MobDeathSimulationTest {

    @Test
    public void testChickenDeathLevel25() {
        // Simulate: Chicken level 25 dies, drops raw chicken
        Set<String> mobTags = new HashSet<>();
        mobTags.add("lvl:25");

        int level = DropsCalculator.getLevelFromTags(mobTags);
        assertEquals(25, level);

        // Normal chicken drops 0-1 raw chicken, let's say it dropped 1
        int originalDrop = 1;
        int multipliedDrop = DropsCalculator.calculateDropCount(originalDrop, level);

        // Expected: 1 * (1 + 25*0.02) = 1 * 1.5 = 1.5 -> 2
        assertEquals(2, multipliedDrop);
        assertTrue(DropsCalculator.shouldIncreaseDrops(originalDrop, multipliedDrop));

        System.out.println("✓ Chicken Lvl 25: " + originalDrop + " -> " + multipliedDrop + " raw chicken");
    }

    @Test
    public void testChickenDeathLevel50() {
        // Simulate: Chicken level 50 dies
        Set<String> mobTags = new HashSet<>();
        mobTags.add("lvl:50");

        int level = DropsCalculator.getLevelFromTags(mobTags);
        assertEquals(50, level);

        // Chicken drops 1 raw chicken
        int originalDrop = 1;
        int multipliedDrop = DropsCalculator.calculateDropCount(originalDrop, level);

        // Expected: 1 * (1 + 50*0.02) = 1 * 2.0 = 2.0 -> 2
        assertEquals(2, multipliedDrop);
        assertTrue(DropsCalculator.shouldIncreaseDrops(originalDrop, multipliedDrop));

        System.out.println("✓ Chicken Lvl 50: " + originalDrop + " -> " + multipliedDrop + " raw chicken");
    }

    @Test
    public void testChickenDeathLevel100() {
        // Simulate: Chicken level 100 dies
        Set<String> mobTags = new HashSet<>();
        mobTags.add("lvl:100");

        int level = DropsCalculator.getLevelFromTags(mobTags);
        assertEquals(100, level);

        int originalDrop = 1;
        int multipliedDrop = DropsCalculator.calculateDropCount(originalDrop, level);

        // Expected: 1 * (1 + 100*0.02) = 1 * 3.0 = 3.0 -> 3
        assertEquals(3, multipliedDrop);
        assertTrue(DropsCalculator.shouldIncreaseDrops(originalDrop, multipliedDrop));

        System.out.println("✓ Chicken Lvl 100: " + originalDrop + " -> " + multipliedDrop + " raw chicken");
    }

    @Test
    public void testChickenDeathLevel150() {
        // Simulate: Chicken level 150 dies (max bonus level)
        Set<String> mobTags = new HashSet<>();
        mobTags.add("lvl:150");

        int level = DropsCalculator.getLevelFromTags(mobTags);
        assertEquals(150, level);

        int originalDrop = 1;
        int multipliedDrop = DropsCalculator.calculateDropCount(originalDrop, level);

        // Expected: 1 * (1 + 150*0.02 + 3.0) = 1 * (1 + 3.0 + 3.0) = 7.0 -> 7
        assertEquals(7, multipliedDrop);
        assertTrue(DropsCalculator.shouldIncreaseDrops(originalDrop, multipliedDrop));

        System.out.println("✓ Chicken Lvl 150: " + originalDrop + " -> " + multipliedDrop + " raw chicken (with bonus!)");
    }

    @Test
    public void testChickenNoLevel() {
        // Simulate: Chicken without level tag dies (vanilla mob)
        Set<String> mobTags = new HashSet<>();
        // No level tag

        int level = DropsCalculator.getLevelFromTags(mobTags);
        assertEquals(0, level);

        int originalDrop = 1;
        int multipliedDrop = DropsCalculator.calculateDropCount(originalDrop, level);

        // Expected: no multiplier, stays 1
        assertEquals(1, multipliedDrop);
        assertFalse(DropsCalculator.shouldIncreaseDrops(originalDrop, multipliedDrop));

        System.out.println("✓ Chicken (no level): " + originalDrop + " -> " + multipliedDrop + " raw chicken (no bonus)");
    }

    @Test
    public void testChickenWithExperienceDrop() {
        // Simulate: Chicken level 75 dies, also drops XP
        Set<String> mobTags = new HashSet<>();
        mobTags.add("lvl:75");

        int level = DropsCalculator.getLevelFromTags(mobTags);
        assertEquals(75, level);

        // Minecraft chickens normally drop 1-3 XP, let's say 2
        int originalXP = 2;
        int multipliedXP = DropsCalculator.calculateDropCount(originalXP, level);

        // Expected: 2 * (1 + 75*0.02) = 2 * 2.5 = 5.0 -> 5
        assertEquals(5, multipliedXP);
        assertTrue(DropsCalculator.shouldIncreaseDrops(originalXP, multipliedXP));

        System.out.println("✓ Chicken Lvl 75 XP: " + originalXP + " -> " + multipliedXP + " experience");
    }

    @Test
    public void testMultipleChickenKills() {
        // Simulate: 3 chickens at level 50 killed in sequence
        System.out.println("\n=== Multiple Chicken Kills (Level 50) ===");

        Set<String> chickenTags = new HashSet<>();
        chickenTags.add("lvl:50");

        int level = DropsCalculator.getLevelFromTags(chickenTags);
        int totalOriginal = 0;
        int totalMultiplied = 0;

        for (int i = 0; i < 3; i++) {
            int originalDrop = 1;
            int multipliedDrop = DropsCalculator.calculateDropCount(originalDrop, level);

            totalOriginal += originalDrop;
            totalMultiplied += multipliedDrop;

            System.out.println("  Chicken " + (i + 1) + ": " + originalDrop + " -> " + multipliedDrop);
        }

        // Total: 3 original -> 6 multiplied
        assertEquals(3, totalOriginal);
        assertEquals(6, totalMultiplied);
        assertTrue(totalMultiplied > totalOriginal);

        System.out.println("  Total: " + totalOriginal + " -> " + totalMultiplied);
    }
}
