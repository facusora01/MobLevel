package com.moblevel;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MobLevel.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOBLEVEL_TAB =
        CREATIVE_MODE_TABS.register("mob_level_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("MobLevel"))
                    .icon(() -> new ItemStack(ModItems.getTotemNecklace()))
                    .displayItems((features, output) -> output.accept(ModItems.getTotemNecklace()))
                    .build());

    public static void register(IEventBus modEventBus) {
        // Ensure ModItems is registered first
        if (!ModItems.isRegistered()) {
            ModItems.register(modEventBus);
            MobLevel.LOGGER.info("ModItems auto-registered via ModCreativeModeTabs");
        }
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
