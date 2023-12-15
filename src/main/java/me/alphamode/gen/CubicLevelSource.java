package me.alphamode.gen;

import net.minecraft.world.Chunk;
import net.minecraft.world.level.levelgen.LevelSource;

public interface CubicLevelSource {
    boolean hasChunk(int x, int y, int z);
    Chunk getChunk(int x, int y, int z);

    Chunk loadChunk(int x, int y, int z);

    void generate(LevelSource chunkGenerator, int x, int y, int z);

    default boolean isCubic() {
        return false;
    }
}
