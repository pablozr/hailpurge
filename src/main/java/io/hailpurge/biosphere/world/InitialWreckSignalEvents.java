package io.hailpurge.biosphere.world;

import io.hailpurge.HailPurge;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = HailPurge.MOD_ID)
public final class InitialWreckSignalEvents {
    private static final DustParticleOptions PALE_BLUE_SMOKE = new DustParticleOptions(new Vector3f(0.35F, 0.85F, 1.0F), 1.5F);

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || level.dimension() != ServerLevel.OVERWORLD) return;
        if (level.getGameTime() % 10 != 0) return;
        BlockPos wreck = InitialBiosphereData.get(level).initialWreck();
        if (wreck == null || !level.hasChunkAt(wreck)) return;
        BlockPos beacon = wreck.south().above(4);
        boolean night = !level.isDay();
        if (level.getBlockState(beacon).is(Blocks.REDSTONE_LAMP)) {
            boolean lit = night && level.getGameTime() % 40 < 20;
            level.setBlock(beacon, level.getBlockState(beacon).setValue(BlockStateProperties.LIT, lit), 3);
        }
        if (!night) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE, beacon.getX() + 0.5D, beacon.getY() + 1.0D, beacon.getZ() + 0.5D, 1, 0.15D, 0.2D, 0.15D, 0.02D);
            level.sendParticles(PALE_BLUE_SMOKE, beacon.getX() + 0.5D, beacon.getY() + 1.0D, beacon.getZ() + 0.5D, 3, 0.35D, 0.25D, 0.35D, 0.01D);
        }
    }

    private InitialWreckSignalEvents() {
    }
}
