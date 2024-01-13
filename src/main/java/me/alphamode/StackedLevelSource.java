package me.alphamode;

import com.google.common.collect.ImmutableMap;
import me.alphamode.world.chunk.CubicChunk;
import net.minecraft.class_441;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.levelgen.BiomeProvider;
import net.minecraft.world.level.levelgen.LevelSource;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class StackedLevelSource implements LevelSource {
    private static final ForkJoinPool POOL = new ForkJoinPool();
    private final Map<Integer, Dimension> dimensions;
    private final Map<Integer, LevelSource> levelSources;
    private final Level level;

    public StackedLevelSource(Level level, Map<Integer, Dimension> dimensions) {
        this.level = level;
        this.dimensions = dimensions;
        ImmutableMap.Builder<Integer, LevelSource> sources = new ImmutableMap.Builder<>();
        dimensions.forEach((id, dimension) -> {
            dimension.setLevel(level);
            sources.put(id, dimension.getLevelSource());
        });
        this.levelSources = sources.build();
    }

    @Override
    public boolean hasChunk(int x, int y, int z) {
        if (!levelSources.containsKey(y / 8))
            return false;
        return levelSources.get(y / 8).hasChunk(x, z);
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        if (!levelSources.containsKey(y / 8))
            return new CubicChunk(this.level, new byte[CubicChunk.CHUNK_SIZE], x, y, z);
        var chunk = CubicChunk.convertLocal(levelSources.get(y / 8).getChunk(x, z), y);
        chunk.method_637();
        return chunk;
    }

    @Override
    public Chunk loadChunk(int x, int y, int z) {
        if (!levelSources.containsKey(y / 8))
            return new CubicChunk(this.level, new byte[CubicChunk.CHUNK_SIZE], x, y, z);
        var chunk =  CubicChunk.convertLocal(levelSources.get(y / 8).loadChunk(x, z), y);
        chunk.method_637();
        return chunk;
    }

    @Override
    public CompletableFuture<CubicChunk[]> getChunksFuture(int x, int y, int z) {
        if (!levelSources.containsKey(y / 8))
            return CompletableFuture.completedFuture(new CubicChunk[]{new CubicChunk(this.level, new byte[CubicChunk.CHUNK_SIZE], x, y, z), new CubicChunk(this.level, new byte[CubicChunk.CHUNK_SIZE], x, y, z), new CubicChunk(this.level, new byte[32768], x, y, z), new CubicChunk(this.level, new byte[32768], x, y, z), new CubicChunk(this.level, new byte[32768], x, y, z), new CubicChunk(this.level, new byte[32768], x, y, z), new CubicChunk(this.level, new byte[32768], x, y, z), new CubicChunk(this.level, new byte[32768], x, y, z)});
        return CompletableFuture.supplyAsync(() -> {
            Chunk legacyChunk = levelSources.get(y / 8).getChunk(x, z);
            CubicChunk[] chunks = new CubicChunk[8];
            for (int i = 0; i < chunks.length; i++) {
                chunks[i] = CubicChunk.convertLocal(legacyChunk, y);
            }
            return chunks;
        }, POOL);
    }

    @Override
    public void generate(LevelSource chunkGenerator, int x, int y, int z) {
        if (!levelSources.containsKey(y / 8))
            return;
        levelSources.get(y / 8).generate(chunkGenerator, x, z);
    }

    @Override
    public boolean save(boolean bl, class_441 arg) {
        return levelSources.get(0).save(bl, arg);
    }

    @Override
    public boolean method_71() {
        return levelSources.get(0).method_71();
    }

    @Override
    public boolean supportsSaving() {
        return levelSources.get(0).supportsSaving();
    }

    @Override
    public String getDebugInfo() {
        return "StackedLevelSource: " + levelSources.get(0).getDebugInfo();
    }

    @Override
    public boolean isCubic() {
        return true;
    }

    @Override
    public boolean hasChunk(int x, int z) {
        throw new RuntimeException();
    }

    @Override
    public Chunk getChunk(int x, int z) {
        throw new RuntimeException();
    }

    @Override
    public Chunk loadChunk(int x, int z) {
        throw new RuntimeException();
    }

    @Override
    public void generate(LevelSource levelSource, int x, int z) {
        throw new RuntimeException();
    }

    @Override
    public BiomeProvider getBiomeProvider(int y) {
        if (!levelSources.containsKey(y / 8))
            return this.level.getBiomeProvider();
        return dimensions.get(y / 8).biomeProvider;
    }

    @Override
    public Dimension getDimension(int y) {
        if (!levelSources.containsKey(y / 8))
            return dimensions.get(0);
        return dimensions.get(y / 8);
    }
}
