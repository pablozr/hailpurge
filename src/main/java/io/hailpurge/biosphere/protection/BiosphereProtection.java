package io.hailpurge.biosphere.protection;

import io.hailpurge.biosphere.world.BiosphereSectorsData;
import io.hailpurge.biosphere.world.InitialBiosphereData;
import net.minecraft.server.level.ServerLevel;

public final class BiosphereProtection {
    public static boolean isProtected(ServerLevel level, double x, double y, double z) {
        if (InitialBiosphereData.get(level).contains(x, y, z)) return true;
        return BiosphereSectorsData.get(level).sectors().stream().anyMatch(sector -> sector.contains(x, y, z));
    }

    private BiosphereProtection() {}
}
