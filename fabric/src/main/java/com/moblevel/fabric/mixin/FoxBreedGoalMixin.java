package com.moblevel.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.moblevel.MobEvents;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Foxes breed through their own goal instead of Animal.spawnChildFromBreeding.
@Mixin(targets = "net.minecraft.world.entity.animal.Fox$FoxBreedGoal")
abstract class FoxBreedGoalMixin extends BreedGoal {
    private FoxBreedGoalMixin(Animal animal, double speedModifier) {
        super(animal, speedModifier);
    }

    @ModifyExpressionValue(method = "breed",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Animal;getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/AgeableMob;"))
    private AgeableMob moblevel$levelChild(AgeableMob child) {
        MobEvents.onBabySpawn(this.animal, this.partner, child);
        return child;
    }
}
