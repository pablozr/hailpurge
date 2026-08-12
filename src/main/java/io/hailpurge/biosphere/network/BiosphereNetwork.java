package io.hailpurge.biosphere.network;

import io.hailpurge.HailPurge;
import io.hailpurge.biosphere.BiosphereEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class BiosphereNetwork {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(HailPurge.MOD_ID, "main"), () -> "1", "1"::equals, "1"::equals);

    public static void register() {
        CHANNEL.registerMessage(0, SyncBiospherePayload.class, SyncBiospherePayload::encode,
                SyncBiospherePayload::decode, SyncBiospherePayload::handle);
    }

    private BiosphereNetwork() {}
}
