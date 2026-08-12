package io.hailpurge.biosphere.world;

import io.hailpurge.HailPurge;
import io.hailpurge.biosphere.domain.SectorStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class BiosphereSectorsData extends SavedData {
    private static final String DATA_ID = HailPurge.MOD_ID + "_biosphere_sectors";
    private final Map<BlockPos, BiosphereSector> sectors = new HashMap<>();

    public static BiosphereSectorsData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BiosphereSectorsData::load, BiosphereSectorsData::new, DATA_ID);
    }

    public void update(BiosphereSector sector) {
        sectors.put(sector.center(), sector);
        setDirty();
    }

    public void remove(BlockPos center) {
        if (sectors.remove(center) != null) setDirty();
    }

    public Collection<BiosphereSector> sectors() { return sectors.values(); }

    private static BiosphereSectorsData load(CompoundTag tag) {
        BiosphereSectorsData data = new BiosphereSectorsData();
        for (var element : tag.getList("sectors", 10)) {
            CompoundTag entry = (CompoundTag) element;
            BlockPos position = BlockPos.of(entry.getLong("pos"));
            data.sectors.put(position, new BiosphereSector(position, entry.getInt("radius"), entry.getDouble("stability"),
                    parseStatus(entry.getString("status"))));
        }
        return data;
    }

    private static SectorStatus parseStatus(String value) {
        try { return SectorStatus.valueOf(value); }
        catch (IllegalArgumentException ignored) { return SectorStatus.OFFLINE; }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        for (BiosphereSector sector : sectors.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("pos", sector.center().asLong());
            entry.putInt("radius", sector.radius());
            entry.putDouble("stability", sector.stability());
            entry.putString("status", sector.status().name());
            entries.add(entry);
        }
        tag.put("sectors", entries);
        return tag;
    }
}
