package io.hailpurge.client.biosphere;

import io.hailpurge.biosphere.network.SyncBiospherePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class ClientBiosphereState {
    private static SyncBiospherePayload field;
    private static boolean wasInside;
    private static long nextCrossingSound;

    public static void set(SyncBiospherePayload payload) { field = payload; }
    public static void clear() { field = null; wasInside = false; }
    public static SyncBiospherePayload current() { return field; }

    public static boolean matches(Level level) { return field != null && field.dimension().equals(level.dimension()); }
    public static boolean inside(double x, double y, double z) {
        if (field == null) return false;
        double dx = x - field.centerX();
        double dy = y - field.centerY();
        double dz = z - field.centerZ();
        if (dx * dx + dy * dy + dz * dz <= (double) field.radius() * field.radius()) return true;
        return field.sectors().stream().anyMatch(sector -> {
            double sectorDx = x - (sector.center().getX() + 0.5D);
            double sectorDy = y - (sector.center().getY() + sector.radius() / 2.0D);
            double sectorDz = z - (sector.center().getZ() + 0.5D);
            double effectiveRadius = sector.radius() * sector.stability();
            return effectiveRadius > 0.0D && sectorDx * sectorDx + sectorDy * sectorDy + sectorDz * sectorDz <= effectiveRadius * effectiveRadius;
        });
    }

    public static void updateCrossing() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !matches(minecraft.level)) return;
        boolean inside = inside(minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ());
        long now = minecraft.level.getGameTime();
        if (inside != wasInside && now >= nextCrossingSound) {
            minecraft.player.playSound(inside ? net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE : net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE,
                    0.45F, inside ? 1.35F : 0.75F);
            minecraft.player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    inside ? "message.hailpurge.biosphere.enter" : "message.hailpurge.biosphere.exit"), true);
            nextCrossingSound = now + 30;
        }
        wasInside = inside;
    }

    private ClientBiosphereState() {}
}
