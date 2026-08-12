package io.hailpurge.client.biosphere;

import io.hailpurge.HailPurge;
import io.hailpurge.biosphere.anchor.BiosphereContent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HailPurge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BiosphereClientEvents {
    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(BiosphereContent.ANCHOR_MENU.get(), BiosphereAnchorScreen::new));
    }

    private BiosphereClientEvents() {
    }
}
