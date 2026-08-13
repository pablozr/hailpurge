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
        if (biosphere.initialWreckInstalled()) return;
        long seed = level.getSeed();
        double angle = Math.floorMod(seed, 360) * Math.PI / 180.0D;
        int distance = 180 + (int) Math.floorMod(seed >>> 9, 71);
        BlockPos spawn = level.getSharedSpawnPos();
        int x = spawn.getX() + (int) Math.round(Math.cos(angle) * distance);
        int z = spawn.getZ() + (int) Math.round(Math.sin(angle) * distance);
        level.getChunk(x >> 4, z >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos wreck = new BlockPos(x, y, z);
        BlockPos north = wreck.relative(net.minecraft.core.Direction.NORTH);
        BlockPos east = wreck.relative(net.minecraft.core.Direction.EAST);
        if (!level.getBlockState(wreck).canBeReplaced() || !level.getBlockState(north).canBeReplaced() || !level.getBlockState(east).canBeReplaced()) return;
        Item copperSpool = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("createaddition", "copper_spool"));
        if (copperSpool == null || copperSpool == Items.AIR) return;
        level.setBlock(north, Blocks.CUT_COPPER.defaultBlockState(), 3);
        level.setBlock(east, Blocks.IRON_BARS.defaultBlockState(), 3);
        if (!level.setBlock(wreck, Blocks.CHEST.defaultBlockState(), 3)) return;
        if (level.getBlockEntity(wreck) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(BiosphereContent.DAMAGED_ALTERNATOR.get()));
            chest.setItem(1, new ItemStack(BiosphereContent.INSULATING_COMPONENT.get()));
            chest.setItem(2, new ItemStack(copperSpool, 2));
            chest.setItem(3, new ItemStack(Items.REDSTONE, 2));
            chest.setItem(4, new ItemStack(Items.IRON_INGOT, 4));
            biosphere.installInitialWreck();
        }
    }

    private CentralAnchorBootstrapEvents() {
    }
}
