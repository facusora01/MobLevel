package com.moblevel;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.ModLoadingContext;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MobLevel.MODID);

    public static final RegistryObject<Item> TOTEM_NECKLACE = ITEMS.register("totem_necklace", () ->
        new Item(new Item.Properties().stacksTo(1))
    );

    private static boolean registered = false;

    static {
        // Auto-register when ModItems class is loaded
        tryAutoRegister();
    }

    private static void tryAutoRegister() {
        try {
            // Try to get the mod container's event bus if available
            Object container = ModLoadingContext.get().getActiveContainer();
            if (container != null) {
                // Get the mod event bus from the container via reflection
                java.lang.reflect.Method getEventBusMethod = container.getClass().getMethod("getEventBus");
                IEventBus eventBus = (IEventBus) getEventBusMethod.invoke(container);
                if (eventBus != null) {
                    ITEMS.register(eventBus);
                    registered = true;
                    MobLevel.LOGGER.info("ModItems auto-registered via static initializer");
                }
            }
        } catch (Exception e) {
            MobLevel.LOGGER.debug("Auto-register failed: {}", e.getMessage());
        }
    }

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
