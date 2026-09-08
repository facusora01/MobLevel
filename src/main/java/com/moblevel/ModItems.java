package com.moblevel;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MobLevel.MODID);

    public static final DeferredItem<Item> TOTEM_NECKLACE =
        ITEMS.registerSimpleItem("totem_necklace", new Item.Properties().stacksTo(1));

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
        return TOTEM_NECKLACE.get();
    }
}
