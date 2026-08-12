package io.hailpurge.biosphere.world;

import io.hailpurge.HailPurge;
import io.hailpurge.biosphere.domain.BiosphereConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class InitialBiosphereData extends SavedData {
    private static final String DATA_ID = HailPurge.MOD_ID + "_initial_biosphere";
    private final BlockPos center;
    private final int radius;

    private InitialBiosphereData(BlockPos center, int radius) {
        this.center = center;
        this.radius = radius;
    }

    public static InitialBiosphereData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(InitialBiosphereData::load,
                () -> create(level), DATA_ID);
    }

    private static InitialBiosphereData create(ServerLevel level) {
        InitialBiosphereData data = new InitialBiosphereData(level.getSharedSpawnPos(), BiosphereConfig.INITIAL_RADIUS.get());
        data.setDirty();
        return data;
    }

    private static InitialBiosphereData load(CompoundTag tag) {
        return new InitialBiosphereData(BlockPos.of(tag.getLong("center")), tag.getInt("radius"));
    }

    public boolean contains(double x, double y, double z) {
        double dx = x - center.getX();
        double dy = y - centerY();
        double dz = z - center.getZ();
        return dx * dx + dy * dy + dz * dz <= (double) radius * radius;
    }

    public double centerX() { return center.getX() + 0.5D; }
    public double centerY() { return center.getY() + radius / 2.0D; }
    public double centerZ() { return center.getZ() + 0.5D; }
    public int radius() { return radius; }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("center", center.asLong());
        tag.putInt("radius", radius);
        return tag;
    }
}
