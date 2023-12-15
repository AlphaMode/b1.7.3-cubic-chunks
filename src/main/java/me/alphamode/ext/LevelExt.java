package me.alphamode.ext;

import me.alphamode.boss.EnderDragon;
import net.minecraft.world.Chunk;

import java.util.List;

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

    List<EnderDragon> getDragons();
}
