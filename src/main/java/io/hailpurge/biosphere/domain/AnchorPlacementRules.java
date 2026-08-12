package io.hailpurge.biosphere.domain;

import io.hailpurge.biosphere.world.BiosphereSector;
import net.minecraft.core.BlockPos;

import java.util.Collection;

public final class AnchorPlacementRules {
    private AnchorPlacementRules() {
    }

    public static boolean hasCapacity(Collection<BiosphereSector> sectors, BlockPos candidate, int maximumAnchors) {
        return sectors.stream().filter(sector -> !sector.center().equals(candidate)).count() < maximumAnchors;
    }

    public static boolean connectsToNetwork(BlockPos candidate, int candidateRadius, BlockPos initialCenter, int initialRadius,
                                            Collection<BiosphereSector> sectors) {
        if (touches(candidate, candidateRadius, initialCenter, initialRadius)) return true;
        return sectors.stream().filter(sector -> !sector.center().equals(candidate))
                .anyMatch(sector -> touches(candidate, candidateRadius, sector.center(), sector.radius()));
    }

    private static boolean touches(BlockPos first, int firstRadius, BlockPos second, int secondRadius) {
        double dx = first.getX() - second.getX();
        double dy = first.getY() + firstRadius / 2.0D - (second.getY() + secondRadius / 2.0D);
        double dz = first.getZ() - second.getZ();
        double combinedRadius = firstRadius + secondRadius;
        return dx * dx + dy * dy + dz * dz <= combinedRadius * combinedRadius;
    }
}
