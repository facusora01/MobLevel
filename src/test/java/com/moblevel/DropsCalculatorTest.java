package com.moblevel;

import org.junit.Test;
import static org.junit.Assert.*;

public class DropsCalculatorTest {

    // ---------- getStatMultiplier (baseline lvl 20 = vanilla) ----------

    @Test
    public void testStatMultiplierLevel20IsVanilla() {
        assertEquals("Level 20 = vanilla (1.0x)", 1.0, DropsCalculator.getStatMultiplier(20), 0.0001);
    }

    @Test
    public void testStatMultiplierLevel1IsHalf() {
        assertEquals("Level 1 = 0.5x vanilla", 0.5, DropsCalculator.getStatMultiplier(1), 0.0001);
    }

    @Test
    public void testStatMultiplierLevel80() {
        // 1.0 + (80-20)*0.05 = 4.0
        assertEquals("Level 80 = 4.0x (boss cap)", 4.0, DropsCalculator.getStatMultiplier(80), 0.0001);
    }

    @Test
    public void testStatMultiplierLevel150() {
        // 1.0 + (150-20)*0.05 = 7.5
        assertEquals("Level 150 = 7.5x", 7.5, DropsCalculator.getStatMultiplier(150), 0.0001);
    }

    @Test
    public void testStatMultiplierLevelZeroIsVanilla() {
        assertEquals("Level 0 = vanilla baseline", 1.0, DropsCalculator.getStatMultiplier(0), 0.0001);
    }

    // ---------- getDropChance (sub-vanilla scarcity) ----------

    @Test
    public void testDropChanceLevel1Is25Percent() {
        assertEquals("Level 1 = 25% drop chance", 0.25, DropsCalculator.getDropChance(1), 0.0001);
    }

    @Test
    public void testDropChanceLevel20IsFull() {
        assertEquals("Level 20 = 100% drop chance", 1.0, DropsCalculator.getDropChance(20), 0.0001);
    }

    @Test
    public void testDropChanceAboveVanillaIsFull() {
        assertEquals("Level 50 = 100% drop chance", 1.0, DropsCalculator.getDropChance(50), 0.0001);
    }

    // ---------- calculateDropCount ----------

    @Test
    public void testDropCountSubVanillaForcedToOne() {
        assertEquals("Level 1 forces count to 1", 1, DropsCalculator.calculateDropCount(3, 1));
        assertEquals("Level 19 forces count to 1", 1, DropsCalculator.calculateDropCount(5, 19));
    }

    @Test
    public void testCalculateDropCountLevel50() {
        // 1.0 + (50-20)*0.02 = 1.6 -> round(1*1.6) = 2
        assertEquals("Level 50 -> 1.6x -> 2", 2, DropsCalculator.calculateDropCount(1, 50));
    }

    @Test
    public void testCalculateDropCountLevel100() {
        // 1.0 + (100-20)*0.02 = 2.6 -> round(1*2.6) = 3
        assertEquals("Level 100 -> 2.6x -> 3", 3, DropsCalculator.calculateDropCount(1, 100));
    }

    @Test
    public void testCalculateDropCountLevel150Bonus() {
        // 1.0 + (150-20)*0.02 = 3.6 + 3.0 bonus = 6.6 -> round(1*6.6) = 7
        assertEquals("Level 150 -> 6.6x -> 7", 7, DropsCalculator.calculateDropCount(1, 150));
    }

    @Test
    public void testCalculateDropCountLevelZero() {
        assertEquals("Level 0 should not modify drops", 5, DropsCalculator.calculateDropCount(5, 0));
    }

    @Test
    public void testCalculateDropCountNegativeLevel() {
        assertEquals("Negative level should not modify drops", 5, DropsCalculator.calculateDropCount(5, -1));
    }

    // ---------- calculateExperienceDrop ----------

    @Test
    public void testExperienceLevel20IsVanilla() {
        // 1.0 + (20-20)*0.01 = 1.0
        assertEquals("Level 20 = vanilla XP", 10, DropsCalculator.calculateExperienceDrop(10, 20));
    }

    @Test
    public void testCalculateExperienceDropLevel50() {
        // 1.0 + (50-20)*0.01 = 1.3 -> round(10*1.3) = 13
        assertEquals("Level 50 -> 1.3x -> 13", 13, DropsCalculator.calculateExperienceDrop(10, 50));
    }

    @Test
    public void testCalculateExperienceDropLevel100() {
        // 1.0 + (100-20)*0.01 = 1.8 -> round(10*1.8) = 18
        assertEquals("Level 100 -> 1.8x -> 18", 18, DropsCalculator.calculateExperienceDrop(10, 100));
    }

    @Test
    public void testCalculateExperienceDropLevel150Bonus() {
        // 1.0 + (150-20)*0.01 = 2.3 + 2.0 bonus = 4.3 -> round(10*4.3) = 43
        assertEquals("Level 150 -> 4.3x -> 43", 43, DropsCalculator.calculateExperienceDrop(10, 150));
    }

    @Test
    public void testCalculateExperienceDropSubVanilla() {
        // Level 1: statMultiplier 0.5 -> round(100*0.5) = 50
        assertEquals("Level 1 -> 0.5x -> 50", 50, DropsCalculator.calculateExperienceDrop(100, 1));
    }

    @Test
    public void testCalculateExperienceDropSubVanillaMinOne() {
        // Tiny XP still drops at least 1
        assertEquals("Sub-vanilla XP floors at 1", 1, DropsCalculator.calculateExperienceDrop(1, 1));
    }

    @Test
    public void testCalculateExperienceDropLevelZero() {
        assertEquals("Level 0 should not modify XP", 10, DropsCalculator.calculateExperienceDrop(10, 0));
    }

    @Test
    public void testCalculateExperienceDropNegativeLevel() {
        assertEquals("Negative level should not modify XP", 10, DropsCalculator.calculateExperienceDrop(10, -1));
    }

    // ---------- getDamageMultiplier ----------

    @Test
    public void testDamageMultiplierLevel20IsVanilla() {
        assertEquals("Level 20 = vanilla damage", 1.0f, DropsCalculator.getDamageMultiplier(20), 0.0001f);
    }

    @Test
    public void testDamageMultiplierLevel50() {
        // 1.0 + (50-20)*0.02 = 1.6
        assertEquals("Level 50 = 1.6x damage", 1.6f, DropsCalculator.getDamageMultiplier(50), 0.0001f);
    }

    @Test
    public void testDamageMultiplierSubVanilla() {
        // Level 1 uses stat curve = 0.5
        assertEquals("Level 1 = 0.5x damage", 0.5f, DropsCalculator.getDamageMultiplier(1), 0.0001f);
    }

    // ---------- helpers ----------

    @Test
    public void testShouldIncreaseDrops() {
        assertTrue("Should increase when new > original", DropsCalculator.shouldIncreaseDrops(1, 2));
        assertFalse("Should not increase when new == original", DropsCalculator.shouldIncreaseDrops(1, 1));
        assertFalse("Should not increase when new < original", DropsCalculator.shouldIncreaseDrops(2, 1));
    }

    @Test
    public void testGetLevelFromTagsExists() {
        java.util.Set<String> tags = new java.util.HashSet<>();
        tags.add("lvl:75");
        tags.add("otherTag");
        assertEquals("Should extract level 75", 75, DropsCalculator.getLevelFromTags(tags));
    }

    @Test
    public void testGetLevelFromTagsNoLevel() {
        java.util.Set<String> tags = new java.util.HashSet<>();
        tags.add("otherTag1");
        tags.add("otherTag2");
        assertEquals("Should return 0 when no level tag", 0, DropsCalculator.getLevelFromTags(tags));
    }

    @Test
    public void testGetLevelFromTagsEmptySet() {
        java.util.Set<String> tags = new java.util.HashSet<>();
        assertEquals("Should return 0 for empty tags", 0, DropsCalculator.getLevelFromTags(tags));
    }

    @Test
    public void testGetLevelFromTagsInvalidFormat() {
        java.util.Set<String> tags = new java.util.HashSet<>();
        tags.add("lvl:notanumber");
        assertEquals("Should return 0 for invalid level format", 0, DropsCalculator.getLevelFromTags(tags));
    }
}
