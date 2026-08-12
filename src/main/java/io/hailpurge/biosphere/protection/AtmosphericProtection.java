package io.hailpurge.biosphere.protection;

import io.hailpurge.HailPurge;
import io.hailpurge.biosphere.domain.ProtectionTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public final class AtmosphericProtection {
    public static final TagKey<Item> BASIC_ITEMS = TagKey.create(ForgeRegistries.Keys.ITEMS,
            new ResourceLocation(HailPurge.MOD_ID, "atmosphere/basic"));
    public static final TagKey<Item> FULL_ITEMS = TagKey.create(ForgeRegistries.Keys.ITEMS,
            new ResourceLocation(HailPurge.MOD_ID, "atmosphere/full"));

    public static ProtectionTier forPlayer(ServerPlayer player) {
        boolean basic = false;
        for (var stack : player.getArmorSlots()) {
            if (stack.is(FULL_ITEMS)) return ProtectionTier.FULL;
            basic |= stack.is(BASIC_ITEMS);
        }
        return basic ? ProtectionTier.BASIC : ProtectionTier.NONE;
    }

    private AtmosphericProtection() {
    }
}
