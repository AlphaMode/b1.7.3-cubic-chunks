package me.alphamode;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import me.alphamode.gen.CubicLevelSource;
import me.alphamode.world.chunk.CubicChunk;
import me.alphamode.world.chunk.SectionPos;
import me.alphamode.world.chunk.SectionTracker;
import net.minecraft.class_441;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.levelgen.BiomeProvider;
import net.minecraft.world.level.levelgen.LevelSource;
import net.minecraft.world.tile.Tile;

public class FlatLevelSource implements LevelSource, CubicLevelSource {

    private final Level level;

    public FlatLevelSource(Level level) {
        this.level = level;
    }

    @Override
    public boolean hasChunk(int x, int z) {
        return false;
    }

    @Override
    public Chunk getChunk(int x, int z) {
        return null;
    }

    @Override
    public Chunk loadChunk(int x, int z) {
        return null;
    }

    @Override
    public void generate(LevelSource levelSource, int x, int z) {

    }

    @Override
    public boolean save(boolean bl, class_441 arg) {
        return true;
    }

    @Override
    public boolean method_71() {
        return false;
    }

    @Override
    public boolean supportsSaving() {
        return true;
    }

    @Override
    public String getDebugInfo() {
        return "FlatLevelSource";
    }

    @Override
    public boolean hasChunk(int x, int y, int z) {
        return true;
    }

    @Override
    public CubicChunk getChunk(int x, int y, int z) {
        byte[] tiles = new byte[32768];
        CubicChunk chunk = new CubicChunk(this.level, tiles, x, y, z);
//        if (x % 2 == 0 && y % 2 == 0 && z % 2 == 0) {
//            for (int tileX = 0; tileX < 16; tileX++) {
//                for (int tileZ = 0; tileZ < 16; tileZ++) {
//                    tiles[CubicChunk.getIndex(tileX, y > 8 ? tileZ : tileX, tileZ)] = (byte) Tile.STONE.id;
//                }
//            }
//        }
        for (int tileX = 0; tileX < 16; tileX++) {
            for (int tileY = 0; tileY < 16; tileY++) {
                for (int tileZ = 0; tileZ < 16; tileZ++) {
                    if (tileX % 4 == 0 && tileY % 4 == 0 && tileZ % 4 == 0) {
                        tiles[CubicChunk.getIndex(tileX, tileY, tileZ)] = (byte) Tile.STONE.id;
                    }
                }
            }
        }
//        chunk.method_637();
        return chunk;
    }

    @Override
    public CubicChunk loadChunk(int x, int y, int z) {
        return getChunk(x, y, z);
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
