package com.moblevel.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.moblevel.MobEvents;

import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Villager babies inherit their parents' level like animals do, so a breeding farm can't
// reroll its way to a high-level villager.
@Mixin(VillagerMakeLove.class)
abstract class VillagerMakeLoveMixin {
    @ModifyExpressionValue(method = "breed",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/villager/Villager;getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/npc/villager/Villager;"))
    private Villager moblevel$levelChild(Villager child,
                                         @Local(argsOnly = true, ordinal = 0) Villager source,
                                         @Local(argsOnly = true, ordinal = 1) Villager target) {
        MobEvents.onBabySpawn(source, target, child);
        return child;
    }
}
