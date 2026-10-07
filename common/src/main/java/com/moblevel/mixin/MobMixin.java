package com.moblevel.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// A converted mob (cured zombie villager, villager struck by lightning, drowned zombie...)
// keeps its level and improved trades. 1.21.2+ copies entity tags on conversion; 1.21 does
// not, so copy them here, before the new mob joins the world and would roll a level of its own.
@Mixin(Mob.class)
abstract class MobMixin {
    @WrapOperation(method = "convertTo",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean moblevel$keepTags(Level level, Entity converted, Operation<Boolean> original) {
        for (String tag : ((Mob) (Object) this).getTags()) converted.addTag(tag);
        return original.call(level, converted);
    }
}
