package com.moblevel.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.moblevel.MobEvents;
import com.moblevel.fabric.DropCapture;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin implements DropCapture {
    @Unique
    private List<ItemEntity> moblevel$drops;

    @Override
    public List<ItemEntity> moblevel$capturedDrops() {
        return moblevel$drops;
    }

    // Death drops: collect them (see EntityMixin), let MobLevel edit them, then spawn them.
    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void moblevel$startCapture(ServerLevel level, DamageSource source, CallbackInfo ci) {
        moblevel$drops = new ArrayList<>();
    }

    @Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
    private void moblevel$releaseDrops(ServerLevel level, DamageSource source, CallbackInfo ci) {
        List<ItemEntity> drops = moblevel$drops;
        moblevel$drops = null;
        MobEvents.modifyDrops((LivingEntity) (Object) this, drops);
        drops.forEach(level::addFreshEntity);
    }

    @ModifyExpressionValue(method = "dropExperience",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getExperienceReward(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)I"))
    private int moblevel$scaleExperience(int xp) {
        return MobEvents.modifyExperience((LivingEntity) (Object) this, xp);
    }

    // Damage after armor and enchantments, before absorption: where NeoForge's LivingDamageEvent.Pre
    // sits. Both LivingEntity.actuallyHurt and Player.actuallyHurt go through here.
    @ModifyReturnValue(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"))
    private float moblevel$scaleDamage(float damage, DamageSource source) {
        return MobEvents.modifyDamage(source, damage);
    }
}
