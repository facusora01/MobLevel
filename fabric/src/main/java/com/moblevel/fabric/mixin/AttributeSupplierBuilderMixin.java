package com.moblevel.fabric.mixin;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Give every living entity ATTACK_DAMAGE so passive mobs can fight back when made aggressive.
// Same as the NeoForge side's EntityAttributeModificationEvent.
@Mixin(AttributeSupplier.Builder.class)
abstract class AttributeSupplierBuilderMixin {
    @Shadow
    @Final
    private ImmutableMap.Builder<Holder<Attribute>, AttributeInstance> builder;

    @Shadow
    public abstract AttributeSupplier.Builder add(Holder<Attribute> attribute, double baseValue);

    @Inject(method = "build", at = @At("HEAD"))
    private void moblevel$addAttackDamage(CallbackInfoReturnable<AttributeSupplier> cir) {
        if (!builder.buildKeepingLast().containsKey(Attributes.ATTACK_DAMAGE)) {
            add(Attributes.ATTACK_DAMAGE, 2.0);
        }
    }
}
