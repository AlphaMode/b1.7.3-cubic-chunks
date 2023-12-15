package me.alphamode;

import me.alphamode.gen.SpikeFeature;
import me.alphamode.noise.ImprovedNoise;
import me.alphamode.noise.PerlinNoise;
import me.alphamode.noise.WorldgenRandom;
import me.alphamode.tile.TheEndTiles;
import me.alphamode.world.CubicChunk;
import net.minecraft.*;
import net.minecraft.util.Mth;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.gen.feature.NetherSpringFeature;
import net.minecraft.world.gen.feature.PlantPatchFeature;
import net.minecraft.world.level.levelgen.HellCaveWorldCarver;
import net.minecraft.world.level.levelgen.LevelSource;
import net.minecraft.world.level.levelgen.WorldCarver;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.class_469;
import net.minecraft.world.tile.SandTile;
import net.minecraft.world.tile.Tile;

public class TheEndLevelSource implements LevelSource {
    private WorldgenRandom random;
    private final class_469 islandNoise; // SimplexNoise
    private final PerlinNoise minLimitPerlinNoise;
    private final PerlinNoise maxLimitPerlinNoise;
    private final PerlinNoise mainPerlinNoise;
    private final int chunkHeight;
    private final int chunkWidth;
    private final int chunkCountX;
    private final int chunkCountY;
    private final int chunkCountZ;
    private PerlinSimplexNoise field_2397;
    private PerlinSimplexNoise field_2398;
    private PerlinSimplexNoise field_2399;
    private PerlinSimplexNoise field_2400;
    private PerlinSimplexNoise field_2401;
    public PerlinSimplexNoise field_2389;
    public PerlinSimplexNoise field_2390;
    private final Level level;
    private double[] field_2403;
    private double[] field_2404 = new double[256];
    private double[] field_2405 = new double[256];
    private double[] field_2406 = new double[256];
    private WorldCarver field_2407 = new HellCaveWorldCarver();
    double[] field_2391;
    double[] field_2392;
    double[] field_2393;
    double[] field_2394;
    double[] field_2395;

    public TheEndLevelSource(Level level, long l) {
        this.level = level;
        this.random = new WorldgenRandom(l);
        this.field_2397 = new PerlinSimplexNoise(this.random, 16);
        this.field_2398 = new PerlinSimplexNoise(this.random, 16);
        this.field_2399 = new PerlinSimplexNoise(this.random, 8);
        this.field_2400 = new PerlinSimplexNoise(this.random, 4);
        this.field_2401 = new PerlinSimplexNoise(this.random, 4);
        this.field_2389 = new PerlinSimplexNoise(this.random, 10);
        this.field_2390 = new PerlinSimplexNoise(this.random, 16);
        this.minLimitPerlinNoise = new PerlinNoise(this.random, 15, 0);
        this.maxLimitPerlinNoise = new PerlinNoise(this.random, 15, 0);
        this.mainPerlinNoise = new PerlinNoise(this.random, 7, 0);
        this.islandNoise = new class_469(this.random);

        this.chunkHeight = 4;
        this.chunkWidth = 8;
        this.chunkCountX = 16 / this.chunkWidth;
        this.chunkCountY = 128 / this.chunkHeight;
        this.chunkCountZ = 16 / this.chunkWidth;
    }

    private double sampleAndClampNoise(int i, int j, int k, double d, double e, double f, double g) {
        double h = 0.0;
        double l = 0.0;
        double m = 0.0;
        double n = 1.0;

        for(int o = 0; o < 16; ++o) {
            double p = (double)i * d * n;
            double q = (double)j * e * n;
            double r = (double)k * d * n;
            double s = e * n;
            ImprovedNoise improvedNoise = this.minLimitPerlinNoise.getOctaveNoise(o);
            if (improvedNoise != null) {
                h += improvedNoise.noise(p, q, r, s, (double)j * s) / n;
            }

            ImprovedNoise improvedNoise2 = this.maxLimitPerlinNoise.getOctaveNoise(o);
            if (improvedNoise2 != null) {
                l += improvedNoise2.noise(p, q, r, s, (double)j * s) / n;
            }

            if (o < 8) {
                ImprovedNoise improvedNoise3 = this.mainPerlinNoise.getOctaveNoise(o);
                if (improvedNoise3 != null) {
                    m += improvedNoise3.noise(
                            (double)i * f * n, (double)j * g * n, (double)k * f * n, g * n, (double)j * g * n
                    )
                            / n;
                }
            }

            n /= 2.0;
        }

        return MathHelper.clampedLerp(h / 512.0, l / 512.0, (m / 10.0 + 1.0) / 2.0);
    }

    public void fillFromNoise(int chunkX, int chunkZ, byte[] tiles) {
        int j = chunkX;
        int k = chunkZ;
        int l = j << 4;
        int m = k << 4;
        byte var4 = 8;
        int var6 = var4 + 1;
        byte var7 = 17;
        int var8 = var4 + 1;
//        this.field_2403 = this.fillNoiseColumn(this.field_2403, x * var4, z * var4, var6, var7, var8);

        double[][][] ds = new double[2][this.chunkCountZ + 1][this.chunkCountY + 1];

        for(int q = 0; q < this.chunkCountZ + 1; ++q) {
            ds[0][q] = new double[this.chunkCountY + 1];
            this.fillNoiseColumn(ds[0][q], j * this.chunkCountX, k * this.chunkCountZ + q);
            ds[1][q] = new double[this.chunkCountY + 1];
        }

        for(int r = 0; r < this.chunkCountX; ++r) {
            for(int s = 0; s < this.chunkCountZ + 1; ++s) {
                this.fillNoiseColumn(ds[1][s], j * this.chunkCountX + r + 1, k * this.chunkCountZ + s);
            }

            for(int s = 0; s < this.chunkCountZ; ++s) {

                for(int t = this.chunkCountY - 1; t >= 0; --t) {
                    double d = ds[0][s][t];
                    double e = ds[0][s + 1][t];
                    double f = ds[1][s][t];
                    double g = ds[1][s + 1][t];
                    double h = ds[0][s][t + 1];
                    double u = ds[0][s + 1][t + 1];
                    double v = ds[1][s][t + 1];
                    double w = ds[1][s + 1][t + 1];

                    for(int x = this.chunkHeight - 1; x >= 0; --x) {
                        int y = t * this.chunkHeight + x;
                        int z = y;
                        int aa = y >> 4;

                        double ab = (double)x / (double)this.chunkHeight;
                        double ac = MathHelper.lerp(ab, d, h);
                        double ad = MathHelper.lerp(ab, f, v);
                        double ae = MathHelper.lerp(ab, e, u);
                        double af = MathHelper.lerp(ab, g, w);

                        for(int ag = 0; ag < this.chunkWidth; ++ag) {
                            int ah = l + r * this.chunkWidth + ag;
                            int ai = ah & 15;
                            double aj = (double)ag / (double)this.chunkWidth;
                            double ak = MathHelper.lerp(aj, ac, ad);
                            double al = MathHelper.lerp(aj, ae, af);

                            for(int am = 0; am < this.chunkWidth; ++am) {
                                int an = m + s * this.chunkWidth + am;
                                int ao = an & 15;
                                double ap = (double)am / (double)this.chunkWidth;
                                double aq = MathHelper.lerp(ap, ak, al);
                                double ar = MathHelper.clamp(aq / 200.0, -1.0, 1.0);
                                if (ar > 0.0) {
                                    tiles[CubicChunk.getIndex(ai, z, ao)] = (byte) TheEndTiles.END_STONE.id;
                                }
//                                this.level.setTileInBounds(ai, z, ao, blockState, false);
                            }
                        }
                    }
                }
            }

            double[][] es = ds[0];
            ds[0] = ds[1];
            ds[1] = es;
        }
    }

    public void method_1490(int i, int j, byte[] tiles) {
        byte var4 = 64;
        double var5 = 0.03125;
        this.field_2404 = this.field_2400.method_1091(this.field_2404, (double)(i * 16), (double)(j * 16), 0.0, 16, 16, 1, var5, var5, 1.0);
        this.field_2405 = this.field_2400.method_1091(this.field_2405, (double)(i * 16), 109.0134, (double)(j * 16), 16, 1, 16, var5, 1.0, var5);
        this.field_2406 = this.field_2401.method_1091(this.field_2406, (double)(i * 16), (double)(j * 16), 0.0, 16, 16, 1, var5 * 2.0, var5 * 2.0, var5 * 2.0);

        for(int var7 = 0; var7 < 16; ++var7) {
            for(int var8 = 0; var8 < 16; ++var8) {
                boolean var10 = this.field_2405[var7 + var8 * 16] + this.random.nextDouble() * 0.2 > 0.0;
                int var11 = (int)(this.field_2406[var7 + var8 * 16] / 3.0 + 3.0 + this.random.nextDouble() * 0.25);
                int var12 = -1;
                byte var13 = (byte)TheEndTiles.END_STONE.id;
                byte var14 = (byte)TheEndTiles.END_STONE.id;

                for(int var15 = 127; var15 >= 0; --var15) {
                    int var16 = (var8 * 16 + var7) * 128 + var15;
                    byte var17 = tiles[var16];
                    if (var17 == 0) {
                        var12 = -1;
                    } else if (var17 == TheEndTiles.END_STONE.id) {
                        if (var12 == -1) {
                            if (var11 <= 0) {
                                var13 = 0;
                                var14 = (byte)TheEndTiles.END_STONE.id;
                            } else if (var15 >= var4 - 4 && var15 <= var4 + 1) {
                                var13 = (byte)TheEndTiles.END_STONE.id;
                                var14 = (byte)TheEndTiles.END_STONE.id;

                                if (var10) {
                                    var14 = (byte)TheEndTiles.END_STONE.id;
                                }
                            }

                            var12 = var11;
                            if (var15 >= var4 - 1) {
                                tiles[var16] = var13;
                            } else {
                                tiles[var16] = var14;
                            }
                        } else if (var12 > 0) {
                            --var12;
                            tiles[var16] = var14;
                        }
                    }
                }
            }
        }
    }

    @Override
    public Chunk loadChunk(int i, int j) {
        return this.getChunk(i, j);
    }

    @Override
    public Chunk getChunk(int x, int z) {
        this.random.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        byte[] tiles = new byte[32768];
        this.fillFromNoise(x, z, tiles);
        this.method_1490(x, z, tiles);
        this.field_2407.place(this, this.level, x, z, tiles);
        return new Chunk(this.level, tiles, x, z);
    }

    private double[] fillNoiseColumn(double[] ds, int x, int z) {
        double d = getHeightValue(x, z);

        int noiseSizeY = this.chunkCountY + 1;
        for(int p = 0; p < noiseSizeY; ++p) {
            double q = sampleAndClampNoise(x, p, z, 1368.824, 684.412, 17.110300000000002, 4.277575000000001);

            q -= 8.0 - d;
            double topSlide = ((double)((int)(noiseSizeY - 4) / 2));

            if ((double)p > topSlide) {
                q = MathHelper.clampedLerp(q, -3000, ((double)p - topSlide) / (double)64);
            } else if ((double)p < 8.0) {
                q = MathHelper.clampedLerp(q, -30.0, (8.0 - (double)p) / (8.0 - 1.0));
            }

            ds[p] = q;
        }

        return ds;
    }

    public float getHeightValue(int i, int j) {
        int k = i / 2;
        int l = j / 2;
        int m = i % 2;
        int n = j % 2;
        float f = 100.0F - Mth.sqrt((float)(i * i + j * j)) * 8.0F;
        f = MathHelper.clamp(f, -100.0F, 80.0F);

        for(int o = -12; o <= 12; ++o) {
            for(int p = -12; p <= 12; ++p) {
                long q = (long)(k + o);
                long r = (long)(l + p);
                if (q * q + r * r > 4096L && this.islandNoise.method_1268((double)q, (double)r) < -0.9F) {
                    float g = (Mth.abs((float)q) * 3439.0F + Mth.abs((float)r) * 147.0F) % 13.0F + 9.0F;
                    float h = (float)(m - o * 2);
                    float s = (float)(n - p * 2);
                    float t = 100.0F - Mth.sqrt(h * h + s * s) * g;
                    t = MathHelper.clamp(t, -100.0F, 80.0F);
                    f = Math.max(f, t);
                }
            }
        }

        return f;
    }

    @Override
    public boolean hasChunk(int i, int j) {
        return true;
    }

    @Override
    public void generate(LevelSource levelSource, int i, int j) {
        SandTile.generating = true;
        int x = i * 16;
        int z = j * 16;

        new SpikeFeature().generateFeature(this.level, this.random, x + this.random.nextInt(16), 0, z + this.random.nextInt(16));

        SandTile.generating = false;
    }

    @Override
    public boolean method_74(boolean bl, class_441 arg) {
        return true;
    }

    @Override
    public boolean method_71() {
        return false;
    }

    @Override
    public boolean supportsSaving() {
        return true;
    }

    @Override
    public String getDebugInfo() {
        return "TheEndLevelSource";
    }

    @Override
    public boolean hasChunk(int x, int y, int z) {
        return true;
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        this.random.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        byte[] tiles = new byte[32768];
        this.fillFromNoise(x, z, tiles);
        this.method_1490(x, z, tiles);
        this.field_2407.place(this, this.level, x, z, tiles);
        return new CubicChunk(this.level, tiles, x, y, z);
    }

    @Override
    public Chunk loadChunk(int x, int y, int z) {
        return getChunk(x, y, z);
    }

    @Override
    public void generate(LevelSource chunkGenerator, int x, int y, int z) {

    }
}
