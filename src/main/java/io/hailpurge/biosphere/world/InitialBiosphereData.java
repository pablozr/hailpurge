package io.hailpurge.biosphere.world;

import io.hailpurge.HailPurge;
import io.hailpurge.biosphere.domain.BiosphereConfig;
import io.hailpurge.biosphere.domain.SectorStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class InitialBiosphereData extends SavedData {
    private static final String DATA_ID = HailPurge.MOD_ID + "_initial_biosphere";
    private final BlockPos center;
    private final int radius;
    private int effectiveRadius;
    private SectorStatus status = SectorStatus.ACTIVE;
    private boolean centralAnchorInstalled;
    private boolean initialWreckInstalled;
    private boolean initialWreckModuleInstalled;
    private BlockPos initialWreck;

    private InitialBiosphereData(BlockPos center, int radius) {
        this.center = center;
        this.radius = radius;
        this.effectiveRadius = radius;
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
        InitialBiosphereData data = new InitialBiosphereData(BlockPos.of(tag.getLong("center")), tag.getInt("radius"));
        data.effectiveRadius = tag.contains("effectiveRadius") ? tag.getInt("effectiveRadius") : data.radius;
        try { data.status = tag.contains("status") ? SectorStatus.valueOf(tag.getString("status")) : SectorStatus.ACTIVE; }
        catch (IllegalArgumentException ignored) { data.status = SectorStatus.ACTIVE; }
        data.centralAnchorInstalled = tag.getBoolean("centralAnchorInstalled");
        data.initialWreckInstalled = tag.getBoolean("initialWreckInstalled");
        data.initialWreckModuleInstalled = tag.getBoolean("initialWreckModuleInstalled");
        if (tag.contains("initialWreck")) data.initialWreck = BlockPos.of(tag.getLong("initialWreck"));
        return data;
    }

    public boolean contains(double x, double y, double z) {
        double dx = x - center.getX();
        double dy = y - centerY();
        double dz = z - center.getZ();
        return dx * dx + dy * dy + dz * dz <= (double) effectiveRadius * effectiveRadius;
    }

    public double centerX() { return center.getX() + 0.5D; }
    public double centerY() { return center.getY() + radius / 2.0D; }
    public double centerZ() { return center.getZ() + 0.5D; }
    public int radius() { return radius; }
    public int effectiveRadius() { return effectiveRadius; }
    public SectorStatus status() { return status; }
    public boolean centralAnchorInstalled() { return centralAnchorInstalled; }
    public void installCentralAnchor() { centralAnchorInstalled = true; setDirty(); }
    public boolean initialWreckInstalled() { return initialWreckInstalled; }
    public BlockPos initialWreck() { return initialWreck; }
    public void locateInitialWreck(BlockPos position) { initialWreck = position; setDirty(); }
    public void installInitialWreck() { initialWreckInstalled = true; setDirty(); }
    public boolean initialWreckModuleInstalled() { return initialWreckModuleInstalled; }
    public void installInitialWreckModule() { initialWreckModuleInstalled = true; setDirty(); }
    public void updateCentralField(int radius, SectorStatus status) {
        effectiveRadius = radius;
        this.status = status;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("center", center.asLong());
        tag.putInt("radius", radius);
        tag.putInt("effectiveRadius", effectiveRadius);
        tag.putString("status", status.name());
        tag.putBoolean("centralAnchorInstalled", centralAnchorInstalled);
        tag.putBoolean("initialWreckInstalled", initialWreckInstalled);
        tag.putBoolean("initialWreckModuleInstalled", initialWreckModuleInstalled);
        if (initialWreck != null) tag.putLong("initialWreck", initialWreck.asLong());
        return tag;
    }
}
