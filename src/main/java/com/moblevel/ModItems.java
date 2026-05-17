package com.moblevel;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MobLevel.MODID);

    public static final RegistryObject<Item> TOTEM_NECKLACE = ITEMS.register("totem_necklace", () ->
        new Item(new Item.Properties().stacksTo(1))
    );
}
