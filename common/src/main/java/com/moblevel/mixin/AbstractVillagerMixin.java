package com.moblevel.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moblevel.MobEvents;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;

// Every new villager and wandering trader offer comes through here: improve the ones it adds.
@Mixin(AbstractVillager.class)
abstract class AbstractVillagerMixin {
    @WrapMethod(method = "addOffersFromItemListings")
    private void moblevel$improveNewOffers(MerchantOffers offers, VillagerTrades.ItemListing[] listings, int count,
                                           Operation<Void> original) {
        int before = offers.size();
        original.call(offers, listings, count);
        MobEvents.improveOffers((AbstractVillager) (Object) this, offers, before);
    }
}
