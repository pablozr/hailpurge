package io.hailpurge.biosphere.anchor;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class EmergencyGeneratorBlock extends DirectionalKineticBlock implements IBE<EmergencyGeneratorBlockEntity> {
    public EmergencyGeneratorBlock(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EmergencyGeneratorBlockEntity(pos, state); }
    @Override public BlockEntityType<EmergencyGeneratorBlockEntity> getBlockEntityType() { return BiosphereContent.EMERGENCY_GENERATOR_ENTITY.get(); }
    @Override public Class<EmergencyGeneratorBlockEntity> getBlockEntityClass() { return EmergencyGeneratorBlockEntity.class; }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level, BlockState state, BlockEntityType<T> type) {
        return type == BiosphereContent.EMERGENCY_GENERATOR_ENTITY.get() ? (tickLevel, tickPos, tickState, entity) -> EmergencyGeneratorBlockEntity.tick(tickLevel, tickPos, tickState, (EmergencyGeneratorBlockEntity) entity) : null;
    }
    @Override public boolean hideStressImpact() { return true; }
    @Override public Direction.Axis getRotationAxis(BlockState state) { return state.getValue(FACING).getAxis(); }
}
