package com.moblevel.fabric;

import com.moblevel.LevelSyncPayload;
import com.moblevel.MobEvents;
import com.moblevel.MobLevel;
import com.moblevel.ModCommands;
import com.moblevel.TotemAnimationPayload;
import com.moblevel.platform.Services;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// Fabric entry point. Events Fabric API has are wired here; the rest (drops, XP, damage,
// breeding, mob tick, attributes, name tags) are mixins in the mixin package.
public class MobLevelFabric implements ModInitializer {
    static final ResourceKey<Item> TOTEM_NECKLACE_KEY =
        ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MobLevel.MODID, "totem_necklace"));
    static Item totemNecklace;

    @Override
    public void onInitialize() {
        // Resolve the platform now, so a packaging mistake fails at startup and not on first use.
        Services.PLATFORM.loaderName();
        FabricConfig.load();

        totemNecklace = Registry.register(BuiltInRegistries.ITEM, TOTEM_NECKLACE_KEY,
            new Item(new Item.Properties().setId(TOTEM_NECKLACE_KEY).stacksTo(1)));
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(MobLevel.MODID, "mob_level_tab"),
            FabricCreativeModeTab.builder()
                .title(Component.literal("MobLevel"))
                .icon(() -> new ItemStack(totemNecklace))
                .displayItems((features, output) -> output.accept(totemNecklace))
                .build());

        PayloadTypeRegistry.clientboundPlay().register(TotemAnimationPayload.TYPE, TotemAnimationPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(LevelSyncPayload.TYPE, LevelSyncPayload.STREAM_CODEC);

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Mob mob) MobEvents.onEntityJoinLevel(mob);
        });
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            if (entity instanceof Mob mob) MobEvents.onStartTracking(mob, player);
        });
        // Returning false cancels the death; tryTotemSave has already healed the entity.
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> !MobEvents.tryTotemSave(entity));
        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> ModCommands.register(dispatcher));
    }
}
