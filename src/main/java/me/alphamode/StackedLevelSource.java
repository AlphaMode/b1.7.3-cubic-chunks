package me.alphamode;

import me.alphamode.world.CubicChunk;
import me.alphamode.world.CubicEmptyChunk;
import net.minecraft.class_441;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.level.levelgen.LevelSource;

public class StackedLevelSource implements LevelSource {

    private final LevelSource[] dimensions;
    private final Level level;

    public StackedLevelSource(Level level, LevelSource[] dimensions) {
        this.level = level;
        this.dimensions = dimensions;
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
    public boolean method_74(boolean bl, class_441 arg) {
        return dimensions[0].method_74(bl, arg);
    }

    @Override
    public boolean method_71() {
        return dimensions[0].method_71();
    }

    @Override
    public boolean supportsSaving() {
        return dimensions[0].supportsSaving();
    }

    @Override
    public String getDebugInfo() {
        return dimensions[0].getDebugInfo();
    }

    @Override
    public boolean hasChunk(int x, int y, int z) {
        if (y < 0 || y / 8 > dimensions.length - 1)
            return false;
        return dimensions[y / 8].hasChunk(x, z);
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        if (y < 0 || y / 8 > dimensions.length - 1)
            return new CubicEmptyChunk(this.level, new byte[32768], x, y, z);
        return CubicChunk.convert(dimensions[y / 8].getChunk(x, z), y & 7);
    }

    @Override
    public Chunk loadChunk(int x, int y, int z) {
        if (y < 0 || y / 8 > dimensions.length - 1)
            return new CubicEmptyChunk(this.level, new byte[32768], x, y, z);
        return CubicChunk.convert(dimensions[y / 8].loadChunk(x, z), y & 7);
    }

    @Override
    public void generate(LevelSource chunkGenerator, int x, int y, int z) {
        if (y < 0 || y / 8 > dimensions.length - 1)
            return;
        dimensions[y / 8].generate(chunkGenerator, x, z);
    }

    @Override
    public boolean isCubic() {
        return true;
    }
}
