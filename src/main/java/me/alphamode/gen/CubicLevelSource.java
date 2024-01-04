package me.alphamode.gen;

import me.alphamode.world.chunk.CubicChunk;
import net.minecraft.world.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.levelgen.BiomeProvider;
import net.minecraft.world.level.levelgen.LevelSource;

import java.util.concurrent.CompletableFuture;

public interface CubicLevelSource {
    boolean hasChunk(int x, int y, int z);
    Chunk getChunk(int x, int y, int z);

    Chunk loadChunk(int x, int y, int z);

    default void putCache(CubicChunk chunk) {}

    default CompletableFuture<CubicChunk[]> getChunksFuture(int x, int y, int z) {
        return CompletableFuture.failedFuture(new Throwable("Implementation does not support threaded generation!"));
    }

    void generate(LevelSource chunkGenerator, int x, int y, int z);

    default boolean isCubic() {
        return false;
    }

    BiomeProvider getBiomeProvider(int y);

    Dimension getDimension(int y);
}
