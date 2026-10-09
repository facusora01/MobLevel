package com.moblevel.fabric.client;

import com.moblevel.ClientLevelCache;
import com.moblevel.LevelSyncPayload;
import com.moblevel.TotemAnimationPayload;
import com.moblevel.UpdateChecker;
import com.moblevel.client.ClientPayloadHandler;
import com.moblevel.client.NameTags;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class MobLevelFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(TotemAnimationPayload.TYPE,
            (payload, context) -> ClientPayloadHandler.handleTotemAnimation(payload));
        ClientPlayNetworking.registerGlobalReceiver(LevelSyncPayload.TYPE,
            (payload, context) -> ClientLevelCache.put(payload.entityId(), payload.level()));

        ClientTickEvents.END_CLIENT_TICK.register(client -> NameTags.onClientTick());

        // Keep the cache bounded: entries die with their entity and on disconnect.
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> ClientLevelCache.remove(entity.getId()));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> UpdateChecker.check());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientLevelCache.clear());
    }
}
