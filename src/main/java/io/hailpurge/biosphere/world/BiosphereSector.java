package io.hailpurge.biosphere.world;

import io.hailpurge.biosphere.domain.SectorStatus;
import net.minecraft.core.BlockPos;

public record BiosphereSector(BlockPos center, int radius, double stability, SectorStatus status) {
    public boolean contains(double x, double y, double z) {
        if (stability <= 0.0D) return false;
        double dx = x - (center.getX() + 0.5D);
        double dy = y - (center.getY() + radius / 2.0D);
        double dz = z - (center.getZ() + 0.5D);
        double effectiveRadius = radius * stability;
        return dx * dx + dy * dy + dz * dz <= effectiveRadius * effectiveRadius;
    }

    public double centerX() { return center.getX() + 0.5D; }
    public double centerY() { return center.getY() + radius / 2.0D; }
    public double centerZ() { return center.getZ() + 0.5D; }
    public int effectiveRadius() { return (int) Math.round(radius * stability); }
}
