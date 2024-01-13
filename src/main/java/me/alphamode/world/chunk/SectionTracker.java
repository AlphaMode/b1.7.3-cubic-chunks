package me.alphamode.world.chunk;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.alphamode.world.ServerHeightMap;

import java.util.List;

public class SectionTracker {
    public static final Long2ObjectMap<SectionData> CHUNKS = new Long2ObjectOpenHashMap<>();

    public static ServerHeightMap getHeightmap(int x, int z) {
        return getHeightmap(SectionPos.toLong(x, z));
    }

    public static ServerHeightMap getHeightmap(long packedPos) {
        return CHUNKS.get(packedPos).heightMap();
    }

    public record SectionData(ServerHeightMap heightMap, Int2ObjectMap<CubicChunk> chunks) {
    }

    public static void removeSection(int x, int y, int z) {
        long pos = SectionPos.toLong(x, z);
        if (CHUNKS.containsKey(pos)) {
            var section = CHUNKS.get(pos);
            section.chunks().remove(y);
            if (section.chunks().isEmpty())
                CHUNKS.remove(pos);
        }
    }
}
