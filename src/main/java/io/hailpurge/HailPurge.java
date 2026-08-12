package io.hailpurge;

import io.hailpurge.biosphere.anchor.BiosphereContent;
import io.hailpurge.biosphere.domain.BiosphereConfig;
import io.hailpurge.biosphere.network.BiosphereNetwork;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HailPurge.MOD_ID)
public final class HailPurge {
    public static final String MOD_ID = "hailpurge";

    public HailPurge() {
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER, BiosphereConfig.SPEC);
        BiosphereNetwork.register();
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        BiosphereContent.BLOCKS.register(bus);
        BiosphereContent.ITEMS.register(bus);
        BiosphereContent.CREATIVE_TABS.register(bus);
        BiosphereContent.BLOCK_ENTITIES.register(bus);
    }
}
