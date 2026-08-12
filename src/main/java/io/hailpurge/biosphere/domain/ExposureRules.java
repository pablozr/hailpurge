package io.hailpurge.biosphere.domain;

public final class ExposureRules {
    public static int nextExposure(int current, boolean protectedArea, boolean night, ProtectionTier protection,
                                   int dayGain, int nightGain, int basicDayGain, int recovery) {
        if (protectedArea || protection == ProtectionTier.FULL) {
            return Math.max(0, current - recovery);
        }
        int gain = night || protection == ProtectionTier.NONE ? (night ? nightGain : dayGain) : basicDayGain;
        return Math.min(100, current + gain);
    }

    public static ExposureBand bandFor(int exposure, int warningThreshold, int criticalThreshold) {
        int criticalStart = Math.max(warningThreshold + 1, criticalThreshold);
        if (exposure >= criticalStart) {
            return ExposureBand.CRITICAL;
        }
        if (exposure >= warningThreshold) {
            return ExposureBand.WARNING;
        }
        return ExposureBand.SAFE;
    }

    private ExposureRules() {
    }

    public enum ExposureBand {
        SAFE,
        WARNING,
        CRITICAL
    }
}
