package io.hailpurge.biosphere;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.network.PacketDistributor;
import io.hailpurge.biosphere.domain.BiosphereConfig;
import io.hailpurge.biosphere.domain.ExposureRules;
import io.hailpurge.biosphere.domain.ProtectionTier;
import io.hailpurge.biosphere.network.BiosphereNetwork;
import io.hailpurge.biosphere.network.SyncBiospherePayload;
import io.hailpurge.biosphere.protection.AtmosphericProtection;
import io.hailpurge.biosphere.protection.BiosphereProtection;
import io.hailpurge.biosphere.world.BiosphereSectorsData;
import io.hailpurge.biosphere.world.InitialBiosphereData;
import java.util.List;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public final class BiosphereEvents {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level) || level.dimension() != ServerLevel.OVERWORLD) {
            return;
        }

        boolean insideBiosphere = BiosphereProtection.isProtected(level, player.getX(), player.getY(), player.getZ());
        boolean night = level.isNight();
        ProtectionTier protection = AtmosphericProtection.forPlayer(player);
        ExposureState state = ExposureState.read(player);
        int exposure = ExposureRules.nextExposure(state.exposure(), insideBiosphere, night, protection,
                BiosphereConfig.DAY_EXPOSURE.get(), BiosphereConfig.NIGHT_EXPOSURE.get(),
                BiosphereConfig.BASIC_DAY_EXPOSURE.get(), BiosphereConfig.RECOVERY.get());
        ExposureRules.ExposureBand band = ExposureRules.bandFor(exposure, BiosphereConfig.WARNING_THRESHOLD.get(),
                BiosphereConfig.CRITICAL_THRESHOLD.get());

        if (band != state.notifiedBand()) {
            player.displayClientMessage(Component.translatable("message.hailpurge.atmosphere." + band.name().toLowerCase()), true);
        }
        applyConsequences(player, band);
        state.update(exposure, band);
        state.save(player);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getEntity().getPersistentData().remove("hailpurge:atmosphere");
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    static void sync(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        InitialBiosphereData biosphere = InitialBiosphereData.get(overworld);
        BiosphereNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncBiospherePayload(ServerLevel.OVERWORLD, biosphere.centerX(), biosphere.centerY(), biosphere.centerZ(), biosphere.radius(),
                        BiosphereSectorsData.get(overworld).sectors().stream().map(sector -> new SyncBiospherePayload.Sector(sector.center(), sector.radius(), (float) sector.stability(), sector.status())).toList()));
    }

    public static void syncOverworld(ServerLevel overworld) {
        for (ServerPlayer player : overworld.players()) sync(player);
    }

    private static void applyConsequences(ServerPlayer player, ExposureRules.ExposureBand band) {
        if (band == ExposureRules.ExposureBand.WARNING) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, true, false));
        }
        if (band == ExposureRules.ExposureBand.CRITICAL) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, false));
            player.hurt(player.damageSources().magic(), 1.0F);
        }
    }

    private BiosphereEvents() {
    }
}
