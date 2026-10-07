package com.moblevel;

import org.junit.Test;
import static org.junit.Assert.*;

public class MobEventsTest {

    @Test
    public void testExperienceDropWithLevel50() {
        // 1.0 + (50-20)*0.01 = 1.3 -> round(10*1.3) = 13
        int result = DropsCalculator.calculateExperienceDrop(10, 50);
        assertEquals("Level 50 should multiply XP by 1.3x", 13, result);
    }

    @Test
    public void testExperienceDropWithLevel100() {
        // 1.0 + (100-20)*0.01 = 1.8 -> round(10*1.8) = 18
        int result = DropsCalculator.calculateExperienceDrop(10, 100);
        assertEquals("Level 100 should multiply XP by 1.8x", 18, result);
    }

    @Test
    public void testExperienceDropWithLevel150() {
        // 1.0 + (150-20)*0.01 = 2.3 + 2.0 bonus = 4.3 -> round(10*4.3) = 43
        int result = DropsCalculator.calculateExperienceDrop(10, 150);
        assertEquals("Level 150 should multiply XP by 4.3x (1.0 + 1.3 + 2.0)", 43, result);
    }

    @Test
    public void testExperienceDropNoLevel() {
        int originalXp = 10;
        int result = DropsCalculator.calculateExperienceDrop(originalXp, 0);
        assertEquals("No level should return original XP", originalXp, result);
    }

    @Test
    public void testExperienceDropLevel20IsVanilla() {
        // Baseline: level 20 = vanilla XP
        int result = DropsCalculator.calculateExperienceDrop(10, 20);
        assertEquals("Level 20 should return vanilla XP", 10, result);
    }

    @Test
    public void testExperienceDropProportional() {
        int originalXp = 100;
        int level10Xp = DropsCalculator.calculateExperienceDrop(originalXp, 10);  // sub-vanilla
        int level30Xp = DropsCalculator.calculateExperienceDrop(originalXp, 30);
        int level50Xp = DropsCalculator.calculateExperienceDrop(originalXp, 50);

        assertTrue("Level 30 XP should be greater than level 10", level30Xp > level10Xp);
        assertTrue("Level 50 XP should be greater than level 30", level50Xp > level30Xp);
    }

    @Test
    public void testExperienceDropLinearScalingAboveVanilla() {
        int originalXp = 100;

        for (int level = 20; level <= 140; level += 10) {
            int xp = DropsCalculator.calculateExperienceDrop(originalXp, level);
            double multiplier = (double) xp / originalXp;
            double expectedMultiplier = 1.0 + ((level - DropsCalculator.VANILLA_LEVEL) * 0.01);

            assertEquals("Level " + level + " multiplier should be " + expectedMultiplier,
                    expectedMultiplier, multiplier, 0.01);
        }
    }

    @Test
    public void testExperienceDropLevel1SubVanilla() {
        // Level 1 = 0.5x stat curve -> round(100*0.5) = 50
        int result = DropsCalculator.calculateExperienceDrop(100, 1);
        assertEquals("Level 1 should give 0.5x multiplier", 50, result);
    }

    @Test
    public void testExperienceDropLargeXpAmount() {
        // 1.0 + (50-20)*0.01 = 1.3 -> round(1000*1.3) = 1300
        int result = DropsCalculator.calculateExperienceDrop(1000, 50);
        assertEquals("Should handle large XP amounts correctly", 1300, result);
    }
}
