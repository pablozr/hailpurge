package io.hailpurge.biosphere.world;

import io.hailpurge.biosphere.domain.SectorStatus;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BiosphereSectorTest {
    @Test void usesEffectiveRadiusFromStability() {
        var sector = new BiosphereSector(new BlockPos(0, 64, 0), 20, 0.5D, SectorStatus.DEGRADING);
        assertTrue(sector.contains(0.5D, 84.0D, 0.5D));
        assertFalse(sector.contains(0.5D, 84.1D, 0.5D));
    }
}
