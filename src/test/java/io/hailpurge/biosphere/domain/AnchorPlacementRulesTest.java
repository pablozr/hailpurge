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

    @Test void doesNotTreatTheCandidateSectorAsAConnection() {
        BlockPos candidate = new BlockPos(200, 64, 0);
        var candidateSector = new BiosphereSector(candidate, 24, 1.0D, SectorStatus.ACTIVE);
        assertFalse(AnchorPlacementRules.connectsToNetwork(candidate, 24, new BlockPos(0, 64, 0), 48,
                List.of(candidateSector)));
    }

    @Test void acceptsAnchorsThatTouchAnExistingSector() {
        var sector = new BiosphereSector(new BlockPos(72, 64, 0), 24, 1.0D, SectorStatus.ACTIVE);
        assertTrue(AnchorPlacementRules.connectsToNetwork(new BlockPos(120, 64, 0), 24,
                new BlockPos(0, 64, 0), 48, List.of(sector)));
    }

    @Test void limitsTheNetworkToFiveAnchors() {
        BlockPos candidate = new BlockPos(100, 64, 0);
        var sector = new BiosphereSector(BlockPos.ZERO, 24, 1.0D, SectorStatus.ACTIVE);
        var sectors = List.of(sector,
                new BiosphereSector(new BlockPos(20, 64, 0), 24, 1.0D, SectorStatus.ACTIVE),
                new BiosphereSector(new BlockPos(40, 64, 0), 24, 1.0D, SectorStatus.ACTIVE),
                new BiosphereSector(new BlockPos(60, 64, 0), 24, 1.0D, SectorStatus.ACTIVE),
                new BiosphereSector(new BlockPos(80, 64, 0), 24, 1.0D, SectorStatus.ACTIVE));
        assertTrue(AnchorPlacementRules.hasCapacity(sectors.subList(0, 4), candidate, 5));
        assertFalse(AnchorPlacementRules.hasCapacity(List.of(sector, sectors.get(1), sectors.get(2), sectors.get(3), sectors.get(4),
                new BiosphereSector(new BlockPos(120, 64, 0), 24, 1.0D, SectorStatus.ACTIVE)), candidate, 5));
    }
}
