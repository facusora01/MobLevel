package com.moblevel.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moblevel.fabric.DropCapture;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(Entity.class)
abstract class EntityMixin {
    // While a LivingEntity drops its death loot, hold the item back instead of adding it to the world.
    @WrapOperation(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean moblevel$captureDrop(ServerLevel level, Entity item, Operation<Boolean> original) {
        if (this instanceof DropCapture capture) {
            List<ItemEntity> drops = capture.moblevel$capturedDrops();
            if (drops != null && item instanceof ItemEntity itemEntity) {
                drops.add(itemEntity);
                return true;
            }
        }
        return original.call(level, item);
    }
}
