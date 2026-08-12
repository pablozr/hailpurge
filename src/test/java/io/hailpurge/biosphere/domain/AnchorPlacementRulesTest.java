package io.hailpurge.biosphere.domain;

import io.hailpurge.biosphere.world.BiosphereSector;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnchorPlacementRulesTest {
    @Test void acceptsAnchorsThatTouchTheInitialBiosphere() {
        assertTrue(AnchorPlacementRules.connectsToNetwork(new BlockPos(70, 64, 0), 24,
                new BlockPos(0, 64, 0), 48, List.of()));
    }

    @Test void rejectsAnchorsOutsideTheProtectedNetwork() {
        assertFalse(AnchorPlacementRules.connectsToNetwork(new BlockPos(71, 64, 0), 24,
                new BlockPos(0, 64, 0), 48, List.of()));
    }

    @Test void acceptsAnchorsThatTouchAnExistingSector() {
        var sector = new BiosphereSector(new BlockPos(72, 64, 0), 24, 1.0D, SectorStatus.ACTIVE);
        assertTrue(AnchorPlacementRules.connectsToNetwork(new BlockPos(120, 64, 0), 24,
                new BlockPos(0, 64, 0), 48, List.of(sector)));
    }

    @Test void limitsTheNetworkToFourAnchors() {
        var sector = new BiosphereSector(BlockPos.ZERO, 24, 1.0D, SectorStatus.ACTIVE);
        assertTrue(AnchorPlacementRules.hasCapacity(List.of(sector, sector, sector), 4));
        assertFalse(AnchorPlacementRules.hasCapacity(List.of(sector, sector, sector, sector), 4));
    }
}
