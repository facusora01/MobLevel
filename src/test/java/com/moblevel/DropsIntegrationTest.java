package com.moblevel;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class DropsIntegrationTest {

    private Set<String> mobTags;

    @Before
    public void setUp() {
        mobTags = new HashSet<>();
    }

    /**
     * Simulate the full drop multiplication flow:
     * 1. Mob has lvl:X tag
     * 2. Extract level from tags
     * 3. Calculate new drop count
     * 4. Verify drop was multiplied
     */
    @Test
    public void testFullDropMultiplicationFlow_Level50() {
        // Setup: mob with level 50
        mobTags.add("lvl:50");

        // Step 1: Extract level from tags
        int level = DropsCalculator.getLevelFromTags(mobTags);
        assertEquals(50, level);

        // Step 2: Calculate new count
        int originalCount = 1;
        int newCount = DropsCalculator.calculateDropCount(originalCount, level);
        assertEquals(2, newCount);

        // Step 3: Check if should increase
        assertTrue(DropsCalculator.shouldIncreaseDrops(originalCount, newCount));
    }

    @Test
    public void testFullDropMultiplicationFlow_Level150() {
        // Setup: mob with level 150
        mobTags.add("lvl:150");

        // Extract level
        int level = DropsCalculator.getLevelFromTags(mobTags);
        assertEquals(150, level);

        // Calculate new count (with bonus)
        int originalCount = 1;
        int newCount = DropsCalculator.calculateDropCount(originalCount, level);
        assertEquals(7, newCount);

        // Check increase
        assertTrue(DropsCalculator.shouldIncreaseDrops(originalCount, newCount));
    }

    @Test
    public void testNoLevelTag_NoMultiplication() {
        // Empty tags - no level
        int level = DropsCalculator.getLevelFromTags(mobTags);
        assertEquals(0, level);

        // Should not multiply
        int originalCount = 10;
        int newCount = DropsCalculator.calculateDropCount(originalCount, level);
        assertEquals(10, newCount);

        // Should NOT increase
        assertFalse(DropsCalculator.shouldIncreaseDrops(originalCount, newCount));
    }

    @Test
    public void testMultipleTagsButOnlyLvlMatters() {
        // Many tags, only lvl:75 matters
        mobTags.add("elite");
        mobTags.add("lvl:75");
        mobTags.add("boss");

        int level = DropsCalculator.getLevelFromTags(mobTags);
        assertEquals(75, level);

        int originalCount = 1;
        int newCount = DropsCalculator.calculateDropCount(originalCount, level);
        assertEquals(3, newCount);
    }

    @Test
    public void testInvalidLvlTag() {
        // Invalid lvl tag (not a number)
        mobTags.add("lvl:abc");

        int level = DropsCalculator.getLevelFromTags(mobTags);
        assertEquals(0, level);

        int originalCount = 10;
        int newCount = DropsCalculator.calculateDropCount(originalCount, level);
        assertEquals(10, newCount);
    }

    @Test
    public void testStackDropMultiplication() {
        // Mob level 100, stack of 64
        mobTags.add("lvl:100");

        int level = DropsCalculator.getLevelFromTags(mobTags);
        assertEquals(100, level);

        int originalCount = 64;
        int newCount = DropsCalculator.calculateDropCount(originalCount, level);
        assertEquals(192, newCount);

        // Verify significant increase
        assertTrue(newCount > originalCount);
        assertEquals(192 - 64, 128);
    }

    @Test
    public void testLevel149VsLevel150Bonus() {
        // Level 149: no bonus
        mobTags.add("lvl:149");
        int level149 = DropsCalculator.getLevelFromTags(mobTags);
        int count149 = DropsCalculator.calculateDropCount(1, level149);
        assertEquals(4, count149); // 1 * (1 + 149*0.02) = 3.98 -> 4

        // Level 150: has bonus
        mobTags.clear();
        mobTags.add("lvl:150");
        int level150 = DropsCalculator.getLevelFromTags(mobTags);
        int count150 = DropsCalculator.calculateDropCount(1, level150);
        assertEquals(7, count150); // 1 * (1 + 150*0.02 + 3.0) = 7

        // Verify bonus kicks in at 150
        assertTrue(count150 > count149);
        assertEquals(7 - 4, 3);
    }

    @Test
    public void testRealisticScenario_MobSpawn() {
        System.out.println("\n=== Realistic Scenario: Mob Spawns ===");

        // Scenario: Zombie spawns with level 75
        String mobType = "Zombie";
        mobTags.add("lvl:75");

        int level = DropsCalculator.getLevelFromTags(mobTags);
        System.out.println(mobType + " spawned with level " + level);
        assertEquals(75, level);

        // Zombie drops: iron sword (1), rotten flesh (2)
        int swordCount = DropsCalculator.calculateDropCount(1, level);
        int fleshCount = DropsCalculator.calculateDropCount(2, level);

        System.out.println("  Iron Sword: 1 -> " + swordCount);
        System.out.println("  Rotten Flesh: 2 -> " + fleshCount);

        assertEquals(3, swordCount); // 1 * 2.5 = 2.5 -> 3
        assertEquals(5, fleshCount); // 2 * 2.5 = 5

        assertTrue(DropsCalculator.shouldIncreaseDrops(1, swordCount));
        assertTrue(DropsCalculator.shouldIncreaseDrops(2, fleshCount));
    }
}
