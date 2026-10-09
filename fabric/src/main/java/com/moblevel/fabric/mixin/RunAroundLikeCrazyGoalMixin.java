package com.moblevel.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.moblevel.MobEvents;

import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// Taming roll: vanilla tames when random(maxTemper) < temper, and temper never passes
// maxTemper. Multiplying the roll's bound divides the chance by the same factor, which
// is what NeoForge gets by vetoing AnimalTameEvent (MobEvents.vetoTame).
@Mixin(RunAroundLikeCrazyGoal.class)
abstract class RunAroundLikeCrazyGoalMixin {
    @Shadow
    @Final
    private AbstractHorse horse;

    @ModifyExpressionValue(method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;getMaxTemper()I"))
    private int moblevel$harderToTame(int maxTemper) {
        return maxTemper * MobEvents.tameDifficulty(horse);
    }
}
