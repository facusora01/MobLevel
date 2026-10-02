package com.moblevel;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;

public class BossMobUtil {
    public static boolean isBossMob(Mob mob) {
        return mob instanceof EnderDragon || mob instanceof WitherBoss;
    }

    public static int getLevelForBossMob(int baseLevel) {
        if (!Config.bossMobsHaveLevelLimits) {
            return baseLevel;
        }
        return clampLevel(baseLevel, Config.bossMinLevel, Config.bossMaxLevel);
    }

    /** Pure clamp logic, testable without a loaded config. */
    public static int clampLevel(int baseLevel, int minLevel, int maxLevel) {
        if (baseLevel < minLevel) return minLevel;
        if (baseLevel > maxLevel) return maxLevel;
        return baseLevel;
    }
}
