package com.moblevel.fabric;

import net.minecraft.world.entity.item.ItemEntity;

import java.util.List;

/**
 * Added to LivingEntity by LivingEntityMixin. While the entity drops its death loot,
 * spawned items are collected here instead of entering the world, so MobEvents.modifyDrops
 * can edit them first (what NeoForge's LivingDropsEvent gives for free).
 */
public interface DropCapture {
    /** The list collecting death drops, or null when not dropping death loot. */
    List<ItemEntity> moblevel$capturedDrops();
}
