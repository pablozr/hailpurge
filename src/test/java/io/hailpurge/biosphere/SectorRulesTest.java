package io.hailpurge.biosphere;

import io.hailpurge.biosphere.domain.SectorRules;
import io.hailpurge.biosphere.domain.SectorStatus;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SectorRulesTest {
    @Test void degradesWithoutEnergy() {
        var result = SectorRules.next(1.0D, 1.0D, false, 0, 200, 0.04D, 0.08D, 30);
        assertEquals(0.96D, result.stability());
        assertEquals(SectorStatus.DEGRADING, result.status());
    }
    @Test void recoversWhenPowered() {
        var result = SectorRules.next(0.5D, 0.5D, false, 400, 200, 0.04D, 0.08D, 30);
        assertEquals(0.58D, result.stability());
        assertEquals(200, result.energy());
        assertEquals(SectorStatus.RECOVERING, result.status());
    }

    @Test void zeroConditionKeepsEmergencyDomeWhilePowered() {
        var result = SectorRules.next(0.8D, 0.0D, false, 1000, 200, 0.04D, 0.08D, 30);
        assertEquals(0.12D, result.stability());
        assertEquals(SectorStatus.DEGRADING, result.status());
    }

    @Test void zeroConditionFailsAfterPowerStarvation() {
        var result = SectorRules.next(0.12D, 0.0D, false, 0, 200, 0.04D, 0.08D, 30);
        assertEquals(0.12D, result.stability());
        assertEquals(SectorStatus.OFFLINE, result.status());
    }
}
