package io.hailpurge.biosphere.anchor;

import io.hailpurge.HailPurge;
import io.hailpurge.biosphere.world.InitialBiosphereData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HailPurge.MOD_ID)
public final class CentralAnchorBootstrapEvents {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        InitialBiosphereData biosphere = InitialBiosphereData.get(overworld);
        if (biosphere.centralAnchorInstalled()) return;
        BlockState current = overworld.getBlockState(overworld.getSharedSpawnPos());
        if (!current.canBeReplaced()) return;
        overworld.setBlock(overworld.getSharedSpawnPos(), BiosphereContent.CENTRAL_ANCHOR.get().defaultBlockState(), 3);
        biosphere.installCentralAnchor();
    }

    private CentralAnchorBootstrapEvents() {
    }
}
