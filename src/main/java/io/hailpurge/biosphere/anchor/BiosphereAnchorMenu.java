package io.hailpurge.biosphere.anchor;

import io.hailpurge.biosphere.domain.AnchorCondition;
import io.hailpurge.biosphere.domain.SectorStatus;
import io.hailpurge.biosphere.world.InitialBiosphereData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public final class BiosphereAnchorMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final boolean central;

    public BiosphereAnchorMenu(int containerId, BiosphereAnchorBlockEntity anchor) {
        this(containerId, new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> anchor.energy() & 0xFFFF;
                    case 1 -> anchor.energy() >>> 16;
                    case 2 -> anchor.capacity() & 0xFFFF;
                    case 3 -> anchor.capacity() >>> 16;
                    case 4 -> anchor.effectiveRadius();
                    case 5 -> anchor.conditionPercent();
                    case 6 -> anchor.status().ordinal();
                    case 7 -> anchor.conditionBand().ordinal();
                    case 8 -> locatorDirection(anchor);
                    case 9 -> locatorDistance(anchor);
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) { }
            @Override public int getCount() { return 10; }
        }, anchor.isCentral());
    }

    private BiosphereAnchorMenu(int containerId, ContainerData data, boolean central) {
        super(BiosphereContent.ANCHOR_MENU.get(), containerId);
        this.data = data;
        this.central = central;
        addDataSlots(data);
    }

    public static BiosphereAnchorMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new BiosphereAnchorMenu(containerId, new SimpleContainerData(10), buffer.readBoolean());
    }

    public int energy() { return (data.get(0) & 0xFFFF) | (data.get(1) & 0xFFFF) << 16; }
    public int capacity() { return (data.get(2) & 0xFFFF) | (data.get(3) & 0xFFFF) << 16; }
    public int radius() { return data.get(4); }
    public int condition() { return data.get(5); }
    public SectorStatus status() { return SectorStatus.values()[data.get(6)]; }
    public AnchorCondition conditionBand() { return AnchorCondition.values()[data.get(7)]; }
    public boolean central() { return central; }
    public int locatorDirection() { return data.get(8); }
    public int locatorDistance() { return data.get(9); }
    @Override public boolean stillValid(net.minecraft.world.entity.player.Player player) { return true; }
    @Override public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) { return ItemStack.EMPTY; }
    private static int locatorDirection(BiosphereAnchorBlockEntity anchor) {
        if (!anchor.isCentral() || !(anchor.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) return -1;
        BlockPos wreck = InitialBiosphereData.get(level).initialWreck();
        if (wreck == null) return -1;
        int dx = wreck.getX() - anchor.getBlockPos().getX();
        int dz = wreck.getZ() - anchor.getBlockPos().getZ();
        if (Math.abs(dx) > Math.abs(dz)) return dx >= 0 ? 0 : 1;
        return dz >= 0 ? 2 : 3;
    }
    private static int locatorDistance(BiosphereAnchorBlockEntity anchor) {
        if (!anchor.isCentral() || !(anchor.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) return 0;
        BlockPos wreck = InitialBiosphereData.get(level).initialWreck();
        if (wreck == null) return 0;
        int dx = wreck.getX() - anchor.getBlockPos().getX();
        int dz = wreck.getZ() - anchor.getBlockPos().getZ();
        return Math.round((float) Math.sqrt(dx * dx + dz * dz) / 10.0F) * 10;
    }
}
