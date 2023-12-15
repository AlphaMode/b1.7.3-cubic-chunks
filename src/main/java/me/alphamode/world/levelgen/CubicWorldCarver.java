package me.alphamode.world.levelgen;

import net.minecraft.world.Level;
import net.minecraft.world.level.levelgen.LevelSource;

import java.util.Random;

public class CubicWorldCarver {
    protected int radius = 8;
    protected Random random = new Random();

    public void place(LevelSource levelSource, Level level, int x, int y, int z, byte[] tiles) {
        int rad = this.radius;
        this.random.setSeed(level.getSeed());
        long seedX = this.random.nextLong() / 2L * 2L + 1L;
        long seedY = this.random.nextLong() / 2L * 2L + 1L;
        long seedZ = this.random.nextLong() / 2L * 2L + 1L;

        for(int chunkX = x - rad; chunkX <= x + rad; ++chunkX) {
            for(int chunkY = y - rad; chunkY <= y + rad; ++chunkY) {
                for (int chunkZ = z - rad; chunkZ <= z + rad; ++chunkZ) {
                    this.random.setSeed((long) chunkX * seedX + (long) chunkY * seedY + (long) chunkZ * seedZ ^ level.getSeed());
                    this.carve(level, chunkX, chunkY, chunkZ, x, y, z, tiles);
                }
            }
        }
    }

    protected void carve(Level level, int chunkX, int chunkY, int chunkZ, int x, int y, int z, byte[] tiles) {
    }
}
