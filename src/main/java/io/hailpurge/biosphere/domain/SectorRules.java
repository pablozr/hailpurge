package io.hailpurge.biosphere.domain;

public final class SectorRules {
    public static Result next(double stability, double condition, boolean majorRepair, int energy, int cost, double degradation, double recovery, int lowPowerSeconds) {
        if (majorRepair) return new Result(Math.min(stability, 0.12D), condition, energy, SectorStatus.OFFLINE);
        if (condition == 0.0D) {
            if (energy < cost) return new Result(Math.min(stability, 0.12D), condition, energy, SectorStatus.OFFLINE);
            return new Result(0.12D, condition, energy - cost, SectorStatus.DEGRADING);
        }
        if (energy >= cost) {
            double next = Math.min(1.0D, stability + recovery);
            SectorStatus status = stability < 1.0D ? SectorStatus.RECOVERING
                    : energy - cost < cost * lowPowerSeconds ? SectorStatus.LOW_POWER : SectorStatus.ACTIVE;
            return new Result(next, Math.max(0.0D, condition - 0.0008D), energy - cost, status);
        }
        double next = Math.max(0.0D, stability - degradation);
        double nextCondition = Math.max(0.0D, condition - 0.035D);
        return new Result(next, nextCondition, energy, next == 0.0D ? SectorStatus.OFFLINE : SectorStatus.DEGRADING);
    }

    public static AnchorCondition conditionBand(double condition, boolean majorRepair) {
        if (majorRepair) return AnchorCondition.MAJOR_REPAIR;
        if (condition <= 0.25D) return AnchorCondition.CRITICAL;
        if (condition <= 0.60D) return AnchorCondition.SERVICE_DUE;
        return AnchorCondition.HEALTHY;
    }

    public record Result(double stability, double condition, int energy, SectorStatus status) {}
    private SectorRules() {}
}
