package io.hailpurge.biosphere.anchor;

import io.hailpurge.HailPurge;
import io.hailpurge.biosphere.domain.AnchorPlacementRules;
import io.hailpurge.biosphere.domain.BiosphereConfig;
import io.hailpurge.biosphere.world.BiosphereSectorsData;
import io.hailpurge.biosphere.world.InitialBiosphereData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HailPurge.MOD_ID)
public final class AnchorPlacementEvents {
    private static final int MAXIMUM_ANCHORS = 5;

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD
                || !event.getPlacedBlock().is(BiosphereContent.ANCHOR.get())) return;

        var sectors = BiosphereSectorsData.get(level).sectors();
        var biosphere = InitialBiosphereData.get(level);
        boolean hasCapacity = AnchorPlacementRules.hasCapacity(sectors, event.getPos(), MAXIMUM_ANCHORS);
        boolean allowed = hasCapacity && AnchorPlacementRules.connectsToNetwork(event.getPos(), BiosphereConfig.ANCHOR_RADIUS.get(),
                level.getSharedSpawnPos(), biosphere.radius(), sectors);
        if (allowed) return;

        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable(hasCapacity
                    ? "message.hailpurge.anchor.disconnected" : "message.hailpurge.anchor.limit"), true);
        }
    }

    private AnchorPlacementEvents() {
    }
}
