package com.moblevel.fabric.mixin;

import com.moblevel.MobEvents;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
abstract class MobMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void moblevel$tick(CallbackInfo ci) {
        MobEvents.onMobTick((Mob) (Object) this);
    }
}
