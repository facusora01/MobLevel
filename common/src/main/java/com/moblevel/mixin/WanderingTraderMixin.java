package com.moblevel.mixin;

import com.moblevel.MobEvents;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// Trading screen title: "[Lv87] Wandering Trader".
@Mixin(WanderingTrader.class)
abstract class WanderingTraderMixin {
    @ModifyArg(method = "mobInteract",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/WanderingTrader;openTradingScreen(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/network/chat/Component;I)V"))
    private Component moblevel$levelInTitle(Component title) {
        return MobEvents.tradingTitle((AbstractVillager) (Object) this, title);
    }
}
