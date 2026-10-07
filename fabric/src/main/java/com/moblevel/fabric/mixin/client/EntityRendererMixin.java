package com.moblevel.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moblevel.client.NameTags;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// The [LvN] name tag. Overrides vanilla's decision the way NeoForge's RenderNameTagEvent does:
// TRUE shows the tag, FALSE hides it, null keeps vanilla's result.
// Before 1.21.2 there are no render states: render() asks shouldShowName and draws the tag itself.
@Mixin(EntityRenderer.class)
abstract class EntityRendererMixin<T extends Entity> {
    @WrapOperation(method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldShowName(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean moblevel$showLevelTag(EntityRenderer<T> renderer, T entity, Operation<Boolean> original) {
        if (entity instanceof Mob mob) {
            Boolean visibility = NameTags.visibility(mob);
            if (visibility != null) return visibility;
        }
        return original.call(renderer, entity);
    }

    @WrapOperation(method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;renderNameTag(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V"))
    private void moblevel$levelLabel(EntityRenderer<T> renderer, T entity, Component name, PoseStack poseStack,
                                     MultiBufferSource buffer, int light, float partialTick, Operation<Void> original) {
        Component label = entity instanceof Mob mob ? NameTags.label(mob) : null;
        original.call(renderer, entity, label != null ? label : name, poseStack, buffer, light, partialTick);
    }
}
