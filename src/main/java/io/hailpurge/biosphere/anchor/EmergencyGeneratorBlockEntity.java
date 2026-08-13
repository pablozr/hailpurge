package io.hailpurge.biosphere.anchor;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public final class EmergencyGeneratorBlockEntity extends KineticBlockEntity {
    private static final int CAPACITY = 2000;
    private static final int MAX_GENERATION_PER_TICK = 3;
    private final EnergyStorage energy = new EnergyStorage(CAPACITY, MAX_GENERATION_PER_TICK, MAX_GENERATION_PER_TICK);
    private final LazyOptional<IEnergyStorage> capability = LazyOptional.of(() -> energy);

    public EmergencyGeneratorBlockEntity(BlockPos pos, BlockState state) { super(BiosphereContent.EMERGENCY_GENERATOR_ENTITY.get(), pos, state); }
    @Override public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) return;
        if (Math.abs(getSpeed()) >= 16.0F) {
            energy.receiveEnergy(2, false);
            if (level.getGameTime() % 2 == 0) energy.receiveEnergy(1, false);
        }
        for (Direction side : Direction.values()) {
            var target = level.getBlockEntity(worldPosition.relative(side));
            if (target == null) continue;
            target.getCapability(ForgeCapabilities.ENERGY, side.getOpposite()).ifPresent(storage -> {
                int offered = energy.extractEnergy(100, true);
                energy.extractEnergy(storage.receiveEnergy(offered, false), false);
            });
        }
        setChanged();
    }
    @Override public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> type, Direction side) { return type == ForgeCapabilities.ENERGY ? capability.cast() : super.getCapability(type, side); }
    @Override public void invalidateCaps() { capability.invalidate(); super.invalidateCaps(); }
    @Override protected void write(CompoundTag tag, boolean clientPacket) { super.write(tag, clientPacket); tag.put("energy", energy.serializeNBT()); }
    @Override protected void read(CompoundTag tag, boolean clientPacket) { super.read(tag, clientPacket); if (tag.contains("energy")) energy.deserializeNBT(tag.get("energy")); }
}
