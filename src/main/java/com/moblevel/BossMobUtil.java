package com.moblevel;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;

public class BossMobUtil {
    public static boolean isBossMob(Mob mob) {
        return mob instanceof EnderDragon || mob instanceof WitherBoss;
    }

    public static int getLevelForBossMob(int baseLevel) {
        if (!Config.BOSS_MOBS_HAVE_LEVEL_LIMITS.get()) {
            return baseLevel;
        }
        return clampLevel(baseLevel, Config.BOSS_MIN_LEVEL.get(), Config.BOSS_MAX_LEVEL.get());
    }

    /** Pure clamp logic, testable without ForgeConfigSpec. */
    public static int clampLevel(int baseLevel, int minLevel, int maxLevel) {
        if (baseLevel < minLevel) return minLevel;
        if (baseLevel > maxLevel) return maxLevel;
        return baseLevel;
    }
}
