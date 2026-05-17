package com.moblevel;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MobLevel.MODID);

    public static final RegistryObject<CreativeModeTab> MOBLEVEL_TAB = CREATIVE_MODE_TABS.register("mob_level_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("MobLevel"))
                    .icon(() -> new ItemStack(ModItems.TOTEM_NECKLACE.get()))
                    .displayItems((features, output) -> output.accept(ModItems.TOTEM_NECKLACE.get()))
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
