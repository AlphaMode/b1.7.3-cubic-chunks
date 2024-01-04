package me.alphamode.mixin;

import me.alphamode.gen.CubicLevelSource;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.HellRandomLevelSource;
import net.minecraft.world.level.levelgen.BiomeProvider;
import net.minecraft.world.level.levelgen.LevelSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HellRandomLevelSource.class)
public abstract class HellRandomLevelSourceMixin implements CubicLevelSource {
    @Shadow public abstract Chunk getChunk(int i, int j);

    @Shadow public abstract Chunk loadChunk(int i, int j);

    @Shadow public abstract void generate(LevelSource levelSource, int i, int j);

    @Shadow private Level field_2402;

    @Override
    public boolean hasChunk(int x, int y, int z) {
        return false;
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        return getChunk(x, z);
    }

    @Override
    public Chunk loadChunk(int x, int y, int z) {
        return loadChunk(x, z);
    }

    @Override
    public void generate(LevelSource chunkGenerator, int x, int y, int z) {
//        generate(chunkGenerator, x, z);
    }

    @Override
    public BiomeProvider getBiomeProvider(int y) {
        return this.field_2402.getBiomeProvider();
    }

    @Override
    public Dimension getDimension(int y) {
        return this.field_2402.dimension;
    }
}
