package io.hailpurge.biosphere.anchor;

import io.hailpurge.HailPurge;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BiosphereContent {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HailPurge.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HailPurge.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HailPurge.MOD_ID);
    public static final RegistryObject<Block> ANCHOR = BLOCKS.register("biosphere_anchor", () -> new BiosphereAnchorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(4.0F).lightLevel(state -> 10)));
    public static final RegistryObject<Item> ANCHOR_ITEM = ITEMS.register("biosphere_anchor", () -> new BlockItem(ANCHOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> FIELD_KIT = ITEMS.register("field_maintenance_kit", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<BlockEntityType<BiosphereAnchorBlockEntity>> ANCHOR_ENTITY = BLOCK_ENTITIES.register("biosphere_anchor", () -> BlockEntityType.Builder.of(BiosphereAnchorBlockEntity::new, ANCHOR.get()).build(null));
    private BiosphereContent() {}
}
