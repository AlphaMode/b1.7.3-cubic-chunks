package me.alphamode.noise;

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import javax.annotation.Nullable;

import java.util.stream.IntStream;

public class PerlinNoise {
    private final ImprovedNoise[] noiseLevels;
    private final double highestFreqValueFactor;
    private final double highestFreqInputFactor;

    public PerlinNoise(WorldgenRandom worldgenRandom, int i, int j) {
        this(worldgenRandom, new IntRBTreeSet(IntStream.rangeClosed(-i, j).toArray()));
    }

    public PerlinNoise(WorldgenRandom worldgenRandom, IntSortedSet intSortedSet) {
        if (intSortedSet.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        } else {
            int i = -intSortedSet.firstInt();
            int j = intSortedSet.lastInt();
            int k = i + j + 1;
            if (k < 1) {
                throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
            } else {
                ImprovedNoise improvedNoise = new ImprovedNoise(worldgenRandom);
                int l = j;
                this.noiseLevels = new ImprovedNoise[k];
                if (j >= 0 && j < k && intSortedSet.contains(0)) {
                    this.noiseLevels[j] = improvedNoise;
                }

                for(int m = j + 1; m < k; ++m) {
                    if (m >= 0 && intSortedSet.contains(l - m)) {
                        this.noiseLevels[m] = new ImprovedNoise(worldgenRandom);
                    } else {
                        worldgenRandom.consumeCount(262);
                    }
                }

                if (j > 0) {
                    long n = (long)(improvedNoise.noise(0.0, 0.0, 0.0, 0.0, 0.0) * 9.223372E18F);
                    WorldgenRandom worldgenRandom2 = new WorldgenRandom(n);

                    for(int o = l - 1; o >= 0; --o) {
                        if (o < k && intSortedSet.contains(l - o)) {
                            this.noiseLevels[o] = new ImprovedNoise(worldgenRandom2);
                        } else {
                            worldgenRandom2.consumeCount(262);
                        }
                    }
                }

                this.highestFreqInputFactor = Math.pow(2.0, (double)j);
                this.highestFreqValueFactor = 1.0 / (Math.pow(2.0, (double)k) - 1.0);
            }
        }
    }

    public double getValue(double d, double e, double f) {
        return this.getValue(d, e, f, 0.0, 0.0, false);
    }

    public double getValue(double d, double e, double f, double g, double h, boolean bl) {
        double i = 0.0;
        double j = this.highestFreqInputFactor;
        double k = this.highestFreqValueFactor;

        for(ImprovedNoise improvedNoise : this.noiseLevels) {
            if (improvedNoise != null) {
                i += improvedNoise.noise(d * j, bl ? -improvedNoise.yo :e * j, f * j, g * j, h * j) * k;
            }

            j /= 2.0;
            k *= 2.0;
        }

        return i;
    }

    @Nullable
    public ImprovedNoise getOctaveNoise(int i) {
        return this.noiseLevels[i];
    }

    public double getSurfaceNoiseValue(double d, double e, double f, double g) {
        return this.getValue(d, e, 0.0, f, g, false);
    }
}