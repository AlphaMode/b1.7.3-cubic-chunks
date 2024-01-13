package me.alphamode.world.chunk;

public record SectionPos(int x, int z) {
    public long toLong() {
        return toLong(x, z);
    }

    public static long toLong(int x, int z) {
        return (long)x & 4294967295L | ((long)z & 4294967295L) << 32;
    }
}
