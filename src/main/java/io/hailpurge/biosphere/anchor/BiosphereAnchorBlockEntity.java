package io.hailpurge.biosphere.anchor;

import io.hailpurge.biosphere.domain.BiosphereConfig;
import io.hailpurge.biosphere.domain.AnchorCondition;
import io.hailpurge.biosphere.domain.SectorRules;
import io.hailpurge.biosphere.domain.SectorStatus;
import io.hailpurge.biosphere.BiosphereEvents;
import io.hailpurge.biosphere.world.BiosphereSector;
import io.hailpurge.biosphere.world.BiosphereSectorsData;
import io.hailpurge.biosphere.world.InitialBiosphereData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public final class BiosphereAnchorBlockEntity extends BlockEntity {
    private final EnergyStorage energy;
    private final LazyOptional<IEnergyStorage> capability;
    private double stability = 1.0D;
    private double condition = 1.0D;
    private boolean majorRepair;
    private SectorStatus status = SectorStatus.OFFLINE;
    private int syncedRadius = -1;
    private SectorStatus syncedStatus;
    private boolean initialized;

    public BiosphereAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(BiosphereContent.ANCHOR_ENTITY.get(), pos, state);
        int capacity = state.is(BiosphereContent.CENTRAL_ANCHOR.get()) ? BiosphereConfig.CENTRAL_CAPACITY.get()
                : BiosphereConfig.ANCHOR_CAPACITY.get();
        energy = new EnergyStorage(capacity, Integer.MAX_VALUE, 0);
        capability = LazyOptional.of(() -> energy);
    }
    public static void tick(Level level, BlockPos pos, BlockState state, BiosphereAnchorBlockEntity anchor) {
        if (level.isClientSide() || level.getGameTime() % 20 != 0) return;
        if (anchor.isCentral()) {
            anchor.tickCentral((ServerLevel) level);
            return;
        }
        boolean wasAtZeroCondition = anchor.condition == 0.0D;
        int energyCost = anchor.condition <= 0.0D ? BiosphereConfig.ANCHOR_CONSUMPTION.get() * 5
                : anchor.condition <= 0.25D ? BiosphereConfig.ANCHOR_CONSUMPTION.get() * 2 : BiosphereConfig.ANCHOR_CONSUMPTION.get();
        boolean couldPayEnergy = anchor.energy.getEnergyStored() >= energyCost;
        var result = SectorRules.next(anchor.stability, anchor.condition, anchor.majorRepair, anchor.energy.getEnergyStored(), energyCost,
                BiosphereConfig.ANCHOR_DEGRADATION.get(), BiosphereConfig.ANCHOR_RECOVERY.get(), BiosphereConfig.ANCHOR_LOW_POWER_SECONDS.get());
        anchor.energy.extractEnergy(anchor.energy.getEnergyStored() - result.energy(), false);
        anchor.stability = result.stability();
        anchor.condition = result.condition();
        anchor.status = result.status();
        if (wasAtZeroCondition && !couldPayEnergy) anchor.majorRepair = true;
        BiosphereSectorsData.get((ServerLevel) level).update(new BiosphereSector(pos, BiosphereConfig.ANCHOR_RADIUS.get(), anchor.stability, anchor.status));
        if (anchor.syncedRadius != anchor.effectiveRadius() || anchor.syncedStatus != anchor.status) {
            anchor.syncedRadius = anchor.effectiveRadius();
            anchor.syncedStatus = anchor.status;
            alert((ServerLevel) level, anchor.status);
            BiosphereEvents.syncOverworld((ServerLevel) level);
        }
        anchor.setChanged();
    }
    @Override public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            if (isCentral()) {
                if (!initialized) {
                    energy.receiveEnergy(BiosphereConfig.CENTRAL_INITIAL_ENERGY.get(), false);
                    initialized = true;
                    setChanged();
                }
                InitialBiosphereData.get(server).updateCentralField(effectiveRadius(), status);
            } else BiosphereSectorsData.get(server).update(new BiosphereSector(worldPosition, BiosphereConfig.ANCHOR_RADIUS.get(), stability, status));
        }
    }
    public int energy() { return energy.getEnergyStored(); }
    public int capacity() { return energy.getMaxEnergyStored(); }
    public SectorStatus status() { return status; }
    public int effectiveRadius() { return isCentral() ? Math.max(BiosphereConfig.CENTRAL_EMERGENCY_RADIUS.get(), (int) Math.round(BiosphereConfig.INITIAL_RADIUS.get() * stability)) : (int) Math.round(BiosphereConfig.ANCHOR_RADIUS.get() * stability); }
    public int conditionPercent() { return (int) Math.round(condition * 100.0D); }
    public AnchorCondition conditionBand() { return SectorRules.conditionBand(condition, majorRepair); }
    public boolean applyFieldService() {
        if (majorRepair || condition <= 0.25D || energy.getEnergyStored() > energy.getMaxEnergyStored() / 4) return false;
        condition = Math.min(1.0D, condition + 0.20D);
        setChanged();
        return true;
    }
    public void removeSector() {
        if (level instanceof ServerLevel server) {
            if (!isCentral()) BiosphereSectorsData.get(server).remove(worldPosition);
            BiosphereEvents.syncOverworld(server);
        }
    }
    private static void alert(ServerLevel level, SectorStatus status) {
        String key = switch (status) {
            case LOW_POWER -> "message.hailpurge.anchor.low_power";
            case DEGRADING -> "message.hailpurge.anchor.degrading";
            case OFFLINE -> "message.hailpurge.anchor.offline";
            case RECOVERING -> "message.hailpurge.anchor.recovering";
            case ACTIVE -> "message.hailpurge.anchor.active";
        };
        for (var player : level.players()) player.displayClientMessage(net.minecraft.network.chat.Component.translatable(key), true);
    }
    @Override public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> type, Direction side) { return type == ForgeCapabilities.ENERGY ? capability.cast() : super.getCapability(type, side); }
    @Override public void invalidateCaps() { capability.invalidate(); super.invalidateCaps(); }
    private boolean isCentral() { return getBlockState().is(BiosphereContent.CENTRAL_ANCHOR.get()); }
    private void tickCentral(ServerLevel level) {
        int cost = BiosphereConfig.CENTRAL_CONSUMPTION.get();
        if (energy.getEnergyStored() >= cost) {
            energy.extractEnergy(cost, false);
            stability = Math.min(1.0D, stability + 0.02D);
            condition = Math.max(0.0D, condition - 0.0002D);
            status = stability < 1.0D ? SectorStatus.RECOVERING : SectorStatus.ACTIVE;
        } else {
            stability = Math.max((double) BiosphereConfig.CENTRAL_EMERGENCY_RADIUS.get() / BiosphereConfig.INITIAL_RADIUS.get(), stability - 0.005D);
            condition = Math.max(0.0D, condition - 0.002D);
            status = SectorStatus.DEGRADING;
        }
        InitialBiosphereData.get(level).updateCentralField(effectiveRadius(), status);
        BiosphereEvents.syncOverworld(level);
        setChanged();
    }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.put("energy", energy.serializeNBT()); tag.putDouble("stability", stability); tag.putDouble("condition", condition); tag.putBoolean("majorRepair", majorRepair); tag.putBoolean("initialized", initialized); tag.putString("status", status.name()); }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("energy")) energy.deserializeNBT(tag.get("energy"));
        stability = tag.contains("stability") ? Math.max(0.0D, Math.min(1.0D, tag.getDouble("stability"))) : 1.0D;
        condition = tag.contains("condition") ? Math.max(0.0D, Math.min(1.0D, tag.getDouble("condition"))) : 1.0D;
        majorRepair = tag.getBoolean("majorRepair");
        initialized = tag.getBoolean("initialized");
        try { status = tag.contains("status") ? SectorStatus.valueOf(tag.getString("status")) : SectorStatus.OFFLINE; }
        catch (IllegalArgumentException ignored) { status = SectorStatus.OFFLINE; }
    }
}
