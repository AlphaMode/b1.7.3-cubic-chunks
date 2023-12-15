package me.alphamode.gen;

import net.minecraft.world.Chunk;
import net.minecraft.world.Level;

public interface CubicChunkSaver {
    Chunk getChunk(Level level, int x, int y, int z);
}
