package io.hailpurge.client.biosphere;

import net.minecraft.client.Minecraft;

public final class ClientAtmosphereState {
    private static float contamination;

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !ClientBiosphereState.matches(minecraft.level)) return;
        boolean protectedArea = ClientBiosphereState.inside(minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ());
        float change = protectedArea ? -0.018F : minecraft.level.isNight() ? 0.012F : 0.005F;
        contamination = Math.max(0.0F, Math.min(1.0F, contamination + change));
    }

    public static float contamination() { return contamination; }
    public static void clear() { contamination = 0.0F; }

    private ClientAtmosphereState() {
    }
}
