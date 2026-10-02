package com.moblevel;

import org.junit.Test;
import static org.junit.Assert.*;

public class BossMobUtilTest {

    private static final int BOSS_MIN_LEVEL = 20;
    private static final int BOSS_MAX_LEVEL = 80;

    @Test
    public void testBossLevelClamping_BelowMin() {
        int result = BossMobUtil.clampLevel(5, BOSS_MIN_LEVEL, BOSS_MAX_LEVEL);
        assertEquals("Boss with level 5 should clamp to min 20", BOSS_MIN_LEVEL, result);
    }

    @Test
    public void testBossLevelClamping_AboveMax() {
        int result = BossMobUtil.clampLevel(130, BOSS_MIN_LEVEL, BOSS_MAX_LEVEL);
        assertEquals("Boss with level 130 should clamp to max 80", BOSS_MAX_LEVEL, result);
    }

    @Test
    public void testBossLevelClamping_WithinRange() {
        int result = BossMobUtil.clampLevel(50, BOSS_MIN_LEVEL, BOSS_MAX_LEVEL);
        assertEquals("Boss with level 50 should remain 50", 50, result);
    }

    @Test
    public void testBossLevelClamping_AtBoundaries() {
        assertEquals("Min boundary stays", BOSS_MIN_LEVEL,
            BossMobUtil.clampLevel(BOSS_MIN_LEVEL, BOSS_MIN_LEVEL, BOSS_MAX_LEVEL));
        assertEquals("Max boundary stays", BOSS_MAX_LEVEL,
            BossMobUtil.clampLevel(BOSS_MAX_LEVEL, BOSS_MIN_LEVEL, BOSS_MAX_LEVEL));
    }

    @Test
    public void testBossLevelClamping_EdgeCases() {
        assertEquals("Level 1 clamps to min", BOSS_MIN_LEVEL,
            BossMobUtil.clampLevel(1, BOSS_MIN_LEVEL, BOSS_MAX_LEVEL));
        assertEquals("Level 500 clamps to max", BOSS_MAX_LEVEL,
            BossMobUtil.clampLevel(500, BOSS_MIN_LEVEL, BOSS_MAX_LEVEL));
    }
}
