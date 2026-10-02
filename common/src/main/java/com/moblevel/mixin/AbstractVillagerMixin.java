package com.moblevel.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moblevel.MobEvents;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import org.spongepowered.asm.mixin.Mixin;

// Every new villager and wandering trader offer comes through here: improve the ones it adds.
@Mixin(AbstractVillager.class)
abstract class AbstractVillagerMixin {
    @WrapMethod(method = "addOffersFromTradeSet")
    private void moblevel$improveNewOffers(ServerLevel level, MerchantOffers offers, ResourceKey<TradeSet> tradeSet,
                                           Operation<Void> original) {
        int before = offers.size();
        original.call(level, offers, tradeSet);
        MobEvents.improveOffers((AbstractVillager) (Object) this, offers, before);
    }
}
