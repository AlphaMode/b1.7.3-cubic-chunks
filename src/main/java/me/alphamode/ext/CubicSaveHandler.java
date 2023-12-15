package me.alphamode.ext;

import net.minecraft.world.Chunk;
import net.minecraft.world.Level;

import java.io.IOException;

public interface CubicSaveHandler {
    Chunk getChunk(Level level, int x, int y, int z) throws IOException;
}
