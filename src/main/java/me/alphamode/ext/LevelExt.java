package me.alphamode.ext;

import net.minecraft.Vec3;
import net.minecraft.world.Chunk;
import net.minecraft.world.level.levelgen.BiomeProvider;

public interface LevelExt {
    int cubic_getTopY(int x, int y, int z);

    default Chunk getChunk(int x, int y, int z) {
        throw new RuntimeException();
    }

    default Chunk getChunkFromPos(int x, int y, int z) {
        throw new RuntimeException();
    }

//    int cubic_method_282(int x, int y, int z);

    int getYHeight(int x, int y, int z);

    BiomeProvider getBiomeProvider(int y);

    Vec3 getFogColor(float partialTick, int y);

    float getTimeOfDay(float partialTick, int y);

    Vec3 getCloudColor(float partialTick, int y);
}
