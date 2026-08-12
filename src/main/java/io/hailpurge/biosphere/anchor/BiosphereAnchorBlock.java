package io.hailpurge.biosphere.anchor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public final class BiosphereAnchorBlock extends BaseEntityBlock {
    public BiosphereAnchorBlock(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new BiosphereAnchorBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BiosphereContent.ANCHOR_ENTITY.get() ? (tickLevel, tickPos, tickState, entity) -> BiosphereAnchorBlockEntity.tick(tickLevel, tickPos, tickState, (BiosphereAnchorBlockEntity) entity) : null;
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BiosphereAnchorBlockEntity anchor) {
            ItemStack held = player.getItemInHand(hand);
            if (held.is(BiosphereContent.FIELD_KIT.get())) {
                if (anchor.applyFieldService()) {
                    held.shrink(1);
                    player.displayClientMessage(Component.translatable("message.hailpurge.anchor.serviced"), true);
                } else player.displayClientMessage(Component.translatable("message.hailpurge.anchor.service_unavailable"), true);
            } else NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player,
                    new net.minecraft.world.MenuProvider() {
                        @Override public Component getDisplayName() { return Component.translatable("screen.hailpurge.anchor.title"); }
                        @Override public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inventory, Player openedBy) {
                            return new BiosphereAnchorMenu(id, anchor);
                        }
                    }, buffer -> new BiosphereAnchorMenu(0, anchor).writeData(buffer));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof BiosphereAnchorBlockEntity anchor) anchor.removeSector();
        super.onRemove(state, level, pos, replacement, moved);
    }
}
