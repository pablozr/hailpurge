package io.hailpurge.client.biosphere;

public final class ClientAtmosphereState {
    private static float contamination;

    public static void tick() {
        if (ClientBiosphereState.current() == null) return;
        float target = Math.max(0.0F, (ClientBiosphereState.current().exposure() - 35) / 65.0F);
        contamination += (target - contamination) * 0.12F;
    }

    public static float contamination() { return contamination; }
    public static void clear() { contamination = 0.0F; }

    private ClientAtmosphereState() {
    }
}
