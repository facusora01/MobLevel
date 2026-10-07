package com.moblevel.neoforge;

import com.moblevel.MobLevel;
import com.moblevel.platform.Services;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(MobLevel.MODID)
public class MobLevelNeoForge {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MobLevel.MODID);
    static final DeferredItem<Item> TOTEM_NECKLACE =
        ITEMS.registerSimpleItem("totem_necklace", props -> props.stacksTo(1));

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MobLevel.MODID);

    static {
        CREATIVE_MODE_TABS.register("mob_level_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("MobLevel"))
                    .icon(() -> new ItemStack(TOTEM_NECKLACE.get()))
                    .displayItems((features, output) -> output.accept(TOTEM_NECKLACE.get()))
                    .build());
    }

    public MobLevelNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Resolve the platform now, so a packaging mistake fails at startup and not on first use.
        Services.PLATFORM.loaderName();

        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeConfig.SPEC);
        modEventBus.addListener(NeoForgeConfig::onLoad);
        modEventBus.addListener(NeoForgeConfig::onReload);

        modEventBus.addListener(MobLevelNeoForge::onAttributeModify);
    }

    // Give every living entity ATTACK_DAMAGE so passive mobs can fight back when made aggressive.
    private static void onAttributeModify(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            if (!event.has(type, Attributes.ATTACK_DAMAGE)) {
                event.add(type, Attributes.ATTACK_DAMAGE, 2.0);
            }
        }
    }
}
