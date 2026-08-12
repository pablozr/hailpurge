package io.hailpurge.biosphere.anchor;

import io.hailpurge.biosphere.domain.AnchorCondition;
import io.hailpurge.biosphere.domain.SectorStatus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class BiosphereAnchorMenu extends AbstractContainerMenu {
    private final int energy;
    private final int capacity;
    private final int radius;
    private final int condition;
    private final SectorStatus status;
    private final AnchorCondition conditionBand;

    public BiosphereAnchorMenu(int containerId, BiosphereAnchorBlockEntity anchor) {
        this(containerId, anchor.energy(), anchor.capacity(), anchor.effectiveRadius(), anchor.conditionPercent(),
                anchor.status(), anchor.conditionBand());
    }

    private BiosphereAnchorMenu(int containerId, int energy, int capacity, int radius, int condition,
                                SectorStatus status, AnchorCondition conditionBand) {
        super(BiosphereContent.ANCHOR_MENU.get(), containerId);
        this.energy = energy;
        this.capacity = capacity;
        this.radius = radius;
        this.condition = condition;
        this.status = status;
        this.conditionBand = conditionBand;
    }

    public static BiosphereAnchorMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new BiosphereAnchorMenu(containerId, buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readEnum(SectorStatus.class), buffer.readEnum(AnchorCondition.class));
    }

    public void writeData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(energy);
        buffer.writeVarInt(capacity);
        buffer.writeVarInt(radius);
        buffer.writeVarInt(condition);
        buffer.writeEnum(status);
        buffer.writeEnum(conditionBand);
    }

    public int energy() { return energy; }
    public int capacity() { return capacity; }
    public int radius() { return radius; }
    public int condition() { return condition; }
    public SectorStatus status() { return status; }
    public AnchorCondition conditionBand() { return conditionBand; }
    @Override public boolean stillValid(net.minecraft.world.entity.player.Player player) { return true; }
    @Override public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) { return ItemStack.EMPTY; }
}
