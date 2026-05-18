package com.moblevel;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MobLevel.MODID);

    public static final RegistryObject<Item> TOTEM_NECKLACE = ITEMS.register("totem_necklace", () ->
        new Item(new Item.Properties().stacksTo(1))
    );

    private static boolean registered = false;

    public static void register(IEventBus modEventBus) {
        if (registered) return;
        ITEMS.register(modEventBus);
        registered = true;
        MobLevel.LOGGER.info("ModItems.register() -> items registered");
    }

    public static boolean isRegistered() {
        return registered;
    }

    public static Item getTotemNecklace() {
        if (TOTEM_NECKLACE.isPresent()) {
            return TOTEM_NECKLACE.get();
        }
        return new Item(new Item.Properties().stacksTo(1));
    }
}
