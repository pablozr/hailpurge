package io.hailpurge.biosphere.network;

import io.hailpurge.biosphere.domain.SectorStatus;
import io.hailpurge.client.biosphere.ClientBiosphereState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import java.util.List;

public record SyncBiospherePayload(ResourceKey<Level> dimension, double centerX, double centerY, double centerZ, int radius, List<Sector> sectors) {
    public static void encode(SyncBiospherePayload payload, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(payload.dimension.location());
        buffer.writeDouble(payload.centerX);
        buffer.writeDouble(payload.centerY);
        buffer.writeDouble(payload.centerZ);
        buffer.writeVarInt(payload.radius);
        buffer.writeVarInt(payload.sectors.size());
        for (Sector sector : payload.sectors) {
            buffer.writeBlockPos(sector.center());
            buffer.writeVarInt(sector.radius());
            buffer.writeFloat(sector.stability());
            buffer.writeEnum(sector.status());
        }
    }

    public static SyncBiospherePayload decode(FriendlyByteBuf buffer) {
        return new SyncBiospherePayload(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                buffer.readResourceLocation()), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readVarInt(),
                java.util.stream.IntStream.range(0, buffer.readVarInt()).mapToObj(ignored -> new Sector(buffer.readBlockPos(), buffer.readVarInt(), buffer.readFloat(), buffer.readEnum(SectorStatus.class))).toList());
    }

    public static void handle(SyncBiospherePayload payload, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientBiosphereState.set(payload)));
        context.setPacketHandled(true);
    }

    public record Sector(net.minecraft.core.BlockPos center, int radius, float stability, SectorStatus status) {}
}
