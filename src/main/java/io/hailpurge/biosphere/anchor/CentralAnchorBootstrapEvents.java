package io.hailpurge.biosphere.anchor;

import io.hailpurge.HailPurge;
import io.hailpurge.biosphere.world.InitialBiosphereData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HailPurge.MOD_ID)
public final class CentralAnchorBootstrapEvents {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        InitialBiosphereData biosphere = InitialBiosphereData.get(overworld);
        if (!biosphere.centralAnchorInstalled()) {
            BlockState current = overworld.getBlockState(overworld.getSharedSpawnPos());
            if (!current.canBeReplaced()) return;
            overworld.setBlock(overworld.getSharedSpawnPos(), BiosphereContent.CENTRAL_ANCHOR.get().defaultBlockState(), 3);
            biosphere.installCentralAnchor();
        }
        installInitialWreck(overworld, biosphere);
    }

    private static void installInitialWreck(ServerLevel level, InitialBiosphereData biosphere) {
        BlockPos wreck = biosphere.initialWreck();
        if (wreck == null) {
            long seed = level.getSeed();
            double angle = Math.floorMod(seed, 360) * Math.PI / 180.0D;
            int distance = 180 + (int) Math.floorMod(seed >>> 9, 71);
            BlockPos spawn = level.getSharedSpawnPos();
            int x = spawn.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = spawn.getZ() + (int) Math.round(Math.sin(angle) * distance);
            if (biosphere.initialWreckInstalled()) {
                int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                for (int y = topY; y >= topY - 4 && wreck == null; y--) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    if (level.getBlockState(candidate).is(Blocks.CHEST)) wreck = candidate;
                }
                if (wreck == null) return;
                biosphere.locateInitialWreck(wreck);
            }
            for (int radius = 0; radius <= 32 && wreck == null; radius += 8) {
                for (int offsetX = -radius; offsetX <= radius && wreck == null; offsetX += Math.max(radius, 1)) {
                    for (int offsetZ = -radius; offsetZ <= radius; offsetZ += Math.max(radius, 1)) {
                        int candidateX = x + offsetX;
                        int candidateZ = z + offsetZ;
                        level.getChunk(candidateX >> 4, candidateZ >> 4);
                        BlockPos candidate = new BlockPos(candidateX, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidateX, candidateZ), candidateZ);
                        double distanceFromSpawn = Math.sqrt((double) (candidateX - spawn.getX()) * (candidateX - spawn.getX())
                                + (double) (candidateZ - spawn.getZ()) * (candidateZ - spawn.getZ()));
                        if (distanceFromSpawn >= 180.0D && distanceFromSpawn <= 250.0D
                                && distanceFromSpawn > biosphere.effectiveRadius() && canPlaceModule(level, candidate)) wreck = candidate;
                    }
                }
            }
            if (wreck == null) return;
            biosphere.locateInitialWreck(wreck);
        }
        if (biosphere.initialWreckModuleInstalled()) return;
        int x = wreck.getX();
        int z = wreck.getZ();
        level.getChunk(x >> 4, z >> 4);
        Item copperSpool = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("createaddition", "copper_spool"));
        if (copperSpool == null || copperSpool == Items.AIR) return;
        if (!biosphere.initialWreckInstalled() && !canPlaceModule(level, wreck)) return;
        if (biosphere.initialWreckInstalled() && (!canUpgradeModule(level, wreck) || !(level.getBlockEntity(wreck) instanceof ChestBlockEntity))) return;
        level.setBlock(wreck.south(), Blocks.WEATHERED_CUT_COPPER.defaultBlockState(), 3);
        level.setBlock(wreck.north(), Blocks.WEATHERED_CUT_COPPER.defaultBlockState(), 3);
        level.setBlock(wreck.east(), Blocks.IRON_BARS.defaultBlockState(), 3);
        level.setBlock(wreck.west(), Blocks.IRON_BARS.defaultBlockState(), 3);
        level.setBlock(wreck.north().east(), Blocks.OXIDIZED_CUT_COPPER.defaultBlockState(), 3);
        level.setBlock(wreck.south().west(), Blocks.OXIDIZED_CUT_COPPER.defaultBlockState(), 3);
        level.setBlock(wreck.south().above(), Blocks.WEATHERED_CUT_COPPER.defaultBlockState(), 3);
        level.setBlock(wreck.south().above(2), Blocks.WEATHERED_CUT_COPPER.defaultBlockState(), 3);
        level.setBlock(wreck.south().above(3), Blocks.IRON_BARS.defaultBlockState(), 3);
        level.setBlock(wreck.south().above(4), Blocks.REDSTONE_LAMP.defaultBlockState(), 3);
        if (!biosphere.initialWreckInstalled() && !level.setBlock(wreck, Blocks.CHEST.defaultBlockState(), 3)) return;
        if (level.getBlockEntity(wreck) instanceof ChestBlockEntity chest) {
            if (!biosphere.initialWreckInstalled()) {
                chest.setItem(0, new ItemStack(BiosphereContent.DAMAGED_ALTERNATOR.get()));
                chest.setItem(1, new ItemStack(BiosphereContent.INSULATING_COMPONENT.get()));
                chest.setItem(2, new ItemStack(copperSpool, 2));
                chest.setItem(3, new ItemStack(Items.REDSTONE, 2));
                chest.setItem(4, new ItemStack(Items.IRON_INGOT, 4));
            }
            biosphere.installInitialWreck();
            biosphere.installInitialWreckModule();
        }
    }

    private static boolean canPlaceModule(ServerLevel level, BlockPos wreck) {
        for (BlockPos pos : new BlockPos[] { wreck, wreck.north(), wreck.south(), wreck.east(), wreck.west(), wreck.north().east(), wreck.south().west(), wreck.south().above(), wreck.south().above(2), wreck.south().above(3), wreck.south().above(4) }) {
            if (!level.getBlockState(pos).canBeReplaced()) return false;
        }
        return true;
    }

    private static boolean canUpgradeModule(ServerLevel level, BlockPos wreck) {
        for (BlockPos pos : new BlockPos[] { wreck.south(), wreck.west(), wreck.north().east(), wreck.south().west(), wreck.south().above(), wreck.south().above(2), wreck.south().above(3), wreck.south().above(4) }) {
            if (!level.getBlockState(pos).canBeReplaced()) return false;
        }
        return true;
    }

    private CentralAnchorBootstrapEvents() {
    }
}
