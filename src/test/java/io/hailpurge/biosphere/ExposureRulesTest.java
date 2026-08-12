package io.hailpurge.biosphere;

import io.hailpurge.biosphere.domain.ExposureRules;
import io.hailpurge.biosphere.domain.ProtectionTier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExposureRulesTest {
    @Test
    void recoversInsideBiosphere() {
        assertEquals(28, ExposureRules.nextExposure(40, true, false, ProtectionTier.NONE, 4, 10, 1, 12));
    }

    @Test
    void basicProtectionOnlyReducesDayExposure() {
        assertEquals(21, ExposureRules.nextExposure(20, false, false, ProtectionTier.BASIC, 4, 10, 1, 12));
        assertEquals(30, ExposureRules.nextExposure(20, false, true, ProtectionTier.BASIC, 4, 10, 1, 12));
    }

    @Test
    void fullProtectionRecoversAtNight() {
        assertEquals(8, ExposureRules.nextExposure(20, false, true, ProtectionTier.FULL, 4, 10, 1, 12));
    }

    @Test
    void assignsBandsAtBoundaries() {
        assertEquals(ExposureRules.ExposureBand.SAFE, ExposureRules.bandFor(34, 35, 75));
        assertEquals(ExposureRules.ExposureBand.WARNING, ExposureRules.bandFor(35, 35, 75));
        assertEquals(ExposureRules.ExposureBand.CRITICAL, ExposureRules.bandFor(75, 35, 75));
    }

    @Test
    void keepsAWarningBandWhenThresholdsAreConfiguredInReverse() {
        assertEquals(ExposureRules.ExposureBand.WARNING, ExposureRules.bandFor(90, 90, 20));
        assertEquals(ExposureRules.ExposureBand.CRITICAL, ExposureRules.bandFor(91, 90, 20));
    }
}
