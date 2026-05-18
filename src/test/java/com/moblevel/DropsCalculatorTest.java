package com.moblevel;

import org.junit.Test;
import static org.junit.Assert.*;

public class DropsCalculatorTest {

    @Test
    public void testCalculateDropCountBasic() {
        int originalCount = 1;
        int level = 50;
        int result = DropsCalculator.calculateDropCount(originalCount, level);
        assertEquals("Level 50 should give 1x + 1.0 (50*0.02) = 2.0x", 2, result);
    }

    @Test
    public void testCalculateDropCountLevel100() {
        int originalCount = 1;
        int level = 100;
        int result = DropsCalculator.calculateDropCount(originalCount, level);
        assertEquals("Level 100 should give 1x + 2.0 (100*0.02) = 3.0x", 3, result);
    }

    @Test
    public void testCalculateDropCountLevel150Bonus() {
        int originalCount = 1;
        int level = 150;
        int result = DropsCalculator.calculateDropCount(originalCount, level);
        assertEquals("Level 150 should give 1x + 3.0 (150*0.02) + 3.0 bonus = 7.0x", 7, result);
    }

    @Test
    public void testCalculateDropCountLevelZero() {
        int originalCount = 5;
        int result = DropsCalculator.calculateDropCount(originalCount, 0);
        assertEquals("Level 0 should not modify drops", originalCount, result);
    }

    @Test
    public void testCalculateDropCountNegativeLevel() {
        int originalCount = 5;
        int result = DropsCalculator.calculateDropCount(originalCount, -1);
        assertEquals("Negative level should not modify drops", originalCount, result);
    }

    @Test
    public void testCalculateExperienceDropBasic() {
        int originalXp = 10;
        int level = 50;
        int result = DropsCalculator.calculateExperienceDrop(originalXp, level);
        assertEquals("Level 50 should give 1.0 + 0.5 = 1.5x multiplier", 15, result);
    }

    @Test
    public void testCalculateExperienceDropLevel100() {
        int originalXp = 10;
        int level = 100;
        int result = DropsCalculator.calculateExperienceDrop(originalXp, level);
        assertEquals("Level 100 should give 1.0 + 1.0 = 2.0x multiplier", 20, result);
    }

    @Test
    public void testCalculateExperienceDropLevel150Bonus() {
        int originalXp = 10;
        int level = 150;
        int result = DropsCalculator.calculateExperienceDrop(originalXp, level);
        assertEquals("Level 150 should give 1.0 + 1.5 + 2.0 bonus = 4.5x multiplier", 45, result);
    }

    @Test
    public void testCalculateExperienceDropLevel1() {
        int originalXp = 100;
        int level = 1;
        int result = DropsCalculator.calculateExperienceDrop(originalXp, level);
        assertEquals("Level 1 should give 1.0 + 0.01 = 1.01x multiplier", 101, result);
    }

    @Test
    public void testCalculateExperienceDropLevelZero() {
        int originalXp = 10;
        int result = DropsCalculator.calculateExperienceDrop(originalXp, 0);
        assertEquals("Level 0 should not modify XP", originalXp, result);
    }

    @Test
    public void testCalculateExperienceDropNegativeLevel() {
        int originalXp = 10;
        int result = DropsCalculator.calculateExperienceDrop(originalXp, -1);
        assertEquals("Negative level should not modify XP", originalXp, result);
    }

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
