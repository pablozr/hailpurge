package io.hailpurge.biosphere.domain;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BiosphereConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue INITIAL_RADIUS;
    public static final ForgeConfigSpec.IntValue DAY_EXPOSURE;
    public static final ForgeConfigSpec.IntValue NIGHT_EXPOSURE;
    public static final ForgeConfigSpec.IntValue BASIC_DAY_EXPOSURE;
    public static final ForgeConfigSpec.IntValue RECOVERY;
    public static final ForgeConfigSpec.IntValue WARNING_THRESHOLD;
    public static final ForgeConfigSpec.IntValue CRITICAL_THRESHOLD;
    public static final ForgeConfigSpec.IntValue ANCHOR_RADIUS;
    public static final ForgeConfigSpec.IntValue ANCHOR_CAPACITY;
    public static final ForgeConfigSpec.IntValue ANCHOR_CONSUMPTION;
    public static final ForgeConfigSpec.IntValue ANCHOR_LOW_POWER_SECONDS;
    public static final ForgeConfigSpec.DoubleValue ANCHOR_DEGRADATION;
    public static final ForgeConfigSpec.DoubleValue ANCHOR_RECOVERY;
    public static final ForgeConfigSpec.IntValue CENTRAL_CONSUMPTION;
    public static final ForgeConfigSpec.IntValue CENTRAL_CAPACITY;
    public static final ForgeConfigSpec.IntValue CENTRAL_INITIAL_ENERGY;
    public static final ForgeConfigSpec.IntValue CENTRAL_EMERGENCY_RADIUS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("biosphere");
        INITIAL_RADIUS = builder.comment("Radius of the initial spherical Biosphere around the first Overworld spawn.")
                .defineInRange("initialRadius", 48, 8, 512);
        builder.pop();
        builder.push("centralAnchor");
        CENTRAL_CONSUMPTION = builder.defineInRange("energyPerSecond", 100, 1, 100000);
        CENTRAL_CAPACITY = builder.defineInRange("energyCapacity", 100000, 1000, 10000000);
        CENTRAL_INITIAL_ENERGY = builder.defineInRange("initialEnergy", 60000, 0, 10000000);
        CENTRAL_EMERGENCY_RADIUS = builder.defineInRange("emergencyRadius", 12, 1, 512);
        builder.pop();
        builder.push("anchors");
        ANCHOR_RADIUS = builder.defineInRange("radius", 24, 4, 128);
        ANCHOR_CAPACITY = builder.defineInRange("energyCapacity", 100000, 1000, 10000000);
        ANCHOR_CONSUMPTION = builder.defineInRange("energyPerSecond", 200, 1, 100000);
        ANCHOR_LOW_POWER_SECONDS = builder.defineInRange("lowPowerSeconds", 30, 1, 600);
        ANCHOR_DEGRADATION = builder.comment("Stability lost per second when an Anchor cannot pay its energy cost.")
                .defineInRange("degradationPerSecond", 0.04D, 0.001D, 1.0D);
        ANCHOR_RECOVERY = builder.comment("Stability restored per second when an Anchor is powered.")
                .defineInRange("recoveryPerSecond", 0.08D, 0.001D, 1.0D);
        builder.pop();
        builder.push("atmosphere");
        DAY_EXPOSURE = builder.comment("Exposure gained every second outside protected areas during the day.")
                .defineInRange("dayExposure", 4, 0, 100);
        NIGHT_EXPOSURE = builder.comment("Exposure gained every second outside protected areas at night.")
                .defineInRange("nightExposure", 10, 0, 100);
        BASIC_DAY_EXPOSURE = builder.comment("Exposure gained by basic protection during the day. Basic protection does not work at night.")
                .defineInRange("basicDayExposure", 1, 0, 100);
        RECOVERY = builder.comment("Exposure recovered every second inside a protected area or with full protection.")
                .defineInRange("recovery", 12, 1, 100);
        WARNING_THRESHOLD = builder.defineInRange("warningThreshold", 35, 1, 99);
        CRITICAL_THRESHOLD = builder.defineInRange("criticalThreshold", 75, 2, 100);
        builder.pop();
        SPEC = builder.build();
    }

    private BiosphereConfig() {
    }
}
