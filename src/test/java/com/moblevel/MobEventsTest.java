package com.moblevel;

import org.junit.Test;
import static org.junit.Assert.*;

public class MobEventsTest {

    @Test
    public void testExperienceDropWithLevel50() {
        int originalXp = 10;
        int level = 50;
        int result = DropsCalculator.calculateExperienceDrop(originalXp, level);
        assertEquals("Level 50 should multiply XP by 1.5x", 15, result);
    }

    @Test
    public void testExperienceDropWithLevel100() {
        int originalXp = 10;
        int level = 100;
        int result = DropsCalculator.calculateExperienceDrop(originalXp, level);
        assertEquals("Level 100 should multiply XP by 2.0x", 20, result);
    }

    @Test
    public void testExperienceDropWithLevel150() {
        int originalXp = 10;
        int level = 150;
        int result = DropsCalculator.calculateExperienceDrop(originalXp, level);
        assertEquals("Level 150 should multiply XP by 4.5x (1.0 + 1.5 + 2.0)", 45, result);
    }

    @Test
    public void testExperienceDropNoLevel() {
        int originalXp = 10;
        int result = DropsCalculator.calculateExperienceDrop(originalXp, 0);
        assertEquals("No level should return original XP", originalXp, result);
    }

    @Test
    public void testExperienceDropProportional() {
        int originalXp = 100;
        int level10Xp = DropsCalculator.calculateExperienceDrop(originalXp, 10);
        int level30Xp = DropsCalculator.calculateExperienceDrop(originalXp, 30);
        int level50Xp = DropsCalculator.calculateExperienceDrop(originalXp, 50);

        assertTrue("Level 30 XP should be greater than level 10", level30Xp > level10Xp);
        assertTrue("Level 50 XP should be greater than level 30", level50Xp > level30Xp);
    }

    @Test
    public void testExperienceDropLinearScaling() {
        int originalXp = 100;

        for (int level = 10; level <= 140; level += 10) {
            int xp = DropsCalculator.calculateExperienceDrop(originalXp, level);
            double multiplier = (double) xp / originalXp;
            double expectedMultiplier = 1.0 + (level * 0.01);

            assertEquals("Level " + level + " multiplier should be " + expectedMultiplier,
                    expectedMultiplier, multiplier, 0.01);
        }
    }

    @Test
    public void testExperienceDropLevel1() {
        int originalXp = 100;
        int result = DropsCalculator.calculateExperienceDrop(originalXp, 1);
        assertEquals("Level 1 should give 1.01x multiplier", 101, result);
    }

    @Test
    public void testExperienceDropLargeXpAmount() {
        int originalXp = 1000;
        int level = 50;
        int result = DropsCalculator.calculateExperienceDrop(originalXp, level);
        assertEquals("Should handle large XP amounts correctly", 1500, result);
    }
}
