package io.hailpurge.biosphere.anchor;

import io.hailpurge.biosphere.domain.AnchorCondition;
import io.hailpurge.biosphere.domain.SectorStatus;
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
                    case 0 -> anchor.energy();
                    case 1 -> anchor.capacity();
                    case 2 -> anchor.effectiveRadius();
                    case 3 -> anchor.conditionPercent();
                    case 4 -> anchor.status().ordinal();
                    case 5 -> anchor.conditionBand().ordinal();
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) { }
            @Override public int getCount() { return 6; }
        }, anchor.isCentral());
    }

    private BiosphereAnchorMenu(int containerId, ContainerData data, boolean central) {
        super(BiosphereContent.ANCHOR_MENU.get(), containerId);
        this.data = data;
        this.central = central;
        addDataSlots(data);
    }

    public static BiosphereAnchorMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new BiosphereAnchorMenu(containerId, new SimpleContainerData(6), buffer.readBoolean());
    }

    public int energy() { return data.get(0); }
    public int capacity() { return data.get(1); }
    public int radius() { return data.get(2); }
    public int condition() { return data.get(3); }
    public SectorStatus status() { return SectorStatus.values()[data.get(4)]; }
    public AnchorCondition conditionBand() { return AnchorCondition.values()[data.get(5)]; }
    public boolean central() { return central; }
    @Override public boolean stillValid(net.minecraft.world.entity.player.Player player) { return true; }
    @Override public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) { return ItemStack.EMPTY; }
}
