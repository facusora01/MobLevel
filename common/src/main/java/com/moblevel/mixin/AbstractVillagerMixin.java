package com.moblevel.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.moblevel.MobEvents;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Every new villager and wandering trader offer comes through here: improve the ones it adds.
// NeoForge 21.0 bundles MixinExtras 0.3, which has no @WrapMethod, so HEAD and RETURN share the size.
@Mixin(AbstractVillager.class)
abstract class AbstractVillagerMixin {
    @Inject(method = "addOffersFromItemListings", at = @At("HEAD"))
    private void moblevel$countOffers(MerchantOffers offers, VillagerTrades.ItemListing[] listings, int count,
                                      CallbackInfo ci, @Share("before") LocalIntRef before) {
        before.set(offers.size());
    }

    @Inject(method = "addOffersFromItemListings", at = @At("RETURN"))
    private void moblevel$improveNewOffers(MerchantOffers offers, VillagerTrades.ItemListing[] listings, int count,
                                           CallbackInfo ci, @Share("before") LocalIntRef before) {
        MobEvents.improveOffers((AbstractVillager) (Object) this, offers, before.get());
    }
}
