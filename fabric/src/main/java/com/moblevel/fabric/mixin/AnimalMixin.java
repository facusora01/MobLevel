package com.moblevel.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.moblevel.MobEvents;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Breeding: the child gets its level from its parents before it joins the world.
@Mixin(Animal.class)
abstract class AnimalMixin {
    @ModifyExpressionValue(method = "spawnChildFromBreeding",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Animal;getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/AgeableMob;"))
    private AgeableMob moblevel$levelChild(AgeableMob child, @Local(argsOnly = true) Animal partner) {
        MobEvents.onBabySpawn((Animal) (Object) this, partner, child);
        return child;
    }
}
