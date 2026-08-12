package io.hailpurge.biosphere;

import io.hailpurge.HailPurge;
import io.hailpurge.biosphere.domain.ExposureRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

final class ExposureState {
    private static final String ROOT = HailPurge.MOD_ID + ":atmosphere";
    private int exposure;
    private ExposureRules.ExposureBand notifiedBand;

    private ExposureState(int exposure, ExposureRules.ExposureBand notifiedBand) {
        this.exposure = exposure;
        this.notifiedBand = notifiedBand;
    }

    static ExposureState read(ServerPlayer player) {
        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        int exposure = root.getInt("exposure");
        int band = root.contains("band") ? root.getInt("band") : ExposureRules.ExposureBand.SAFE.ordinal();
        int validBand = Math.max(0, Math.min(band, ExposureRules.ExposureBand.values().length - 1));
        return new ExposureState(exposure, ExposureRules.ExposureBand.values()[validBand]);
    }

    int exposure() {
        return exposure;
    }

    ExposureRules.ExposureBand notifiedBand() {
        return notifiedBand;
    }

    void update(int exposure, ExposureRules.ExposureBand band) {
        this.exposure = exposure;
        this.notifiedBand = band;
    }

    void save(ServerPlayer player) {
        CompoundTag root = new CompoundTag();
        root.putInt("exposure", exposure);
        root.putInt("band", notifiedBand.ordinal());
        player.getPersistentData().put(ROOT, root);
    }
}
