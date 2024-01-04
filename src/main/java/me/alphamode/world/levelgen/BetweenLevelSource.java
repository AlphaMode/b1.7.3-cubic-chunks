package me.alphamode.world.levelgen;

import me.alphamode.world.chunk.CubicChunk;
import net.minecraft.class_441;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.levelgen.BiomeProvider;
import net.minecraft.world.level.levelgen.CaveWorldCarver;
import net.minecraft.world.level.levelgen.LevelSource;
import net.minecraft.world.level.levelgen.WorldCarver;

public class BetweenLevelSource implements LevelSource {
    private final Level level;
    private WorldCarver carver = new CaveWorldCarver();
    private CubicWorldCarver cubicCarver = new CubicCaveWorldCarver();
    private final int tile;
    private final boolean generateCaves;

    public BetweenLevelSource(Level level, long seed, int tile, boolean generateCaves) {
        this.level = level;
        this.tile = tile;
        this.generateCaves = generateCaves;
    }

    @Override
    public boolean hasChunk(int x, int z) {
        return true;
    }

    @Override
    public Chunk getChunk(int x, int z) {
        return loadChunk(x, z);
    }

    @Override
    public Chunk loadChunk(int x, int z) {
        byte[] tiles = new byte[32768];
        Chunk chunk = new Chunk(this.level, tiles, x, z);
        for (int sectionX = 0; sectionX < 16; sectionX++) {
            for (int sectionY = 0; sectionY < 128; sectionY++) {
                for (int sectionZ = 0; sectionZ < 16; sectionZ++) {
                    tiles[CubicChunk.getIndex(sectionX, sectionY, sectionZ)] = (byte) tile;
                }
            }
        }
//        if (generateCaves)
//            this.carver.place(this, this.level, x, z, tiles);

        chunk.method_637();
        return chunk;
    }

    @Override
    public void generate(LevelSource levelSource, int x, int z) {

    }

    @Override
    public boolean save(boolean bl, class_441 arg) {
        return false;
    }

    @Override
    public boolean method_71() {
        return false;
    }

    @Override
    public boolean supportsSaving() {
        return false;
    }

    @Override
    public String getDebugInfo() {
        return null;
    }

    @Override
    public boolean hasChunk(int x, int y, int z) {
        return true;
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        return loadChunk(x, y, z);
    }

    @Override
    public Chunk loadChunk(int x, int y, int z) {
        byte[] tiles = new byte[32768];
        Chunk chunk = new CubicChunk(this.level, tiles, x, y, z);
        for (int sectionX = 0; sectionX < 16; sectionX++) {
            for (int sectionY = 0; sectionY < 16; sectionY++) {
                for (int sectionZ = 0; sectionZ < 16; sectionZ++) {
                    tiles[CubicChunk.getIndex(sectionX, sectionY, sectionZ)] = (byte) tile;
                }
            }
        }
        if (generateCaves)
            this.cubicCarver.place(this, this.level, x, y, z, tiles);

        chunk.method_637();
        return chunk;
    }

    @Override
    public void generate(LevelSource chunkGenerator, int x, int y, int z) {

    }

    @Override
    public boolean isCubic() {
        return true;
    }

    @Override
    public BiomeProvider getBiomeProvider(int y) {
        return this.level.getBiomeProvider();
    }

    @Override
    public Dimension getDimension(int y) {
        return this.level.dimension;
    }
}
