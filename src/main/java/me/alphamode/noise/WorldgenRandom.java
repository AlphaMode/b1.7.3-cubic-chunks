package me.alphamode.noise;

import java.util.Random;

public class WorldgenRandom extends Random {
    private int count;

    public WorldgenRandom() {
    }

    public WorldgenRandom(long l) {
        super(l);
    }

    public void consumeCount(int i) {
        for(int j = 0; j < i; ++j) {
            this.next(1);
        }
    }

    protected int next(int i) {
        ++this.count;
        return super.next(i);
    }

    public long setBaseChunkSeed(int i, int j) {
        long l = (long)i * 341873128712L + (long)j * 132897987541L;
        this.setSeed(l);
        return l;
    }

    public long setDecorationSeed(long l, int i, int j) {
        this.setSeed(l);
        long m = this.nextLong() | 1L;
        long n = this.nextLong() | 1L;
        long o = (long)i * m + (long)j * n ^ l;
        this.setSeed(o);
        return o;
    }

    public long setFeatureSeed(long l, int i, int j) {
        long m = l + (long)i + (long)(10000 * j);
        this.setSeed(m);
        return m;
    }

    public long setLargeFeatureSeed(long l, int i, int j) {
        this.setSeed(l);
        long m = this.nextLong();
        long n = this.nextLong();
        long o = (long)i * m ^ (long)j * n ^ l;
        this.setSeed(o);
        return o;
    }

    public long setLargeFeatureWithSalt(long l, int i, int j, int k) {
        long m = (long)i * 341873128712L + (long)j * 132897987541L + l + (long)k;
        this.setSeed(m);
        return m;
    }

    public static Random seedSlimeChunk(int i, int j, long l, long m) {
        return new Random(l + (long)(i * i * 4987142) + (long)(i * 5947611) + (long)(j * j) * 4392871L + (long)(j * 389711) ^ m);
    }
}