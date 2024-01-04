/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package me.alphamode;

import java.util.Random;
import net.minecraft.CactusFeature;
import net.minecraft.DungeonFeature;
import net.minecraft.PondFeature;
import net.minecraft.PumpkinPatchFeature;
import net.minecraft.class_441;
import net.minecraft.class_554;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Feature;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.PatchFeature;
import net.minecraft.world.gen.feature.PlantPatchFeature;
import net.minecraft.world.gen.feature.ReedsFeature;
import net.minecraft.world.gen.feature.TallPatchFeature;
import net.minecraft.world.gen.feature.class_227;
import net.minecraft.world.level.levelgen.BiomeProvider;
import net.minecraft.world.level.levelgen.CaveWorldCarver;
import net.minecraft.world.level.levelgen.LevelSource;
import net.minecraft.world.level.levelgen.WorldCarver;
import net.minecraft.world.level.levelgen.biome.Biome;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.tile.SandTile;
import net.minecraft.world.tile.Tile;

public class AmplifiedRandomLevelSource
        implements LevelSource {
    private Random random;
    private PerlinNoise field_2483;
    private PerlinNoise field_2484;
    private PerlinNoise field_2485;
    private PerlinNoise surface;
    private PerlinNoise field_2487;
    public PerlinNoise field_2473;
    public PerlinNoise field_2474;
    public PerlinNoise field_2475;
    private Level level;
    private double[] field_2489;
    private double[] beachNoise = new double[256];
    private double[] gravelNoise = new double[256];
    private double[] field_2492 = new double[256];
    private WorldCarver carver = new CaveWorldCarver();
    private Biome[] biomes;
    double[] field_2476;
    double[] field_2477;
    double[] field_2478;
    double[] field_2479;
    double[] field_2480;
    int[][] field_2481 = new int[32][32];
    private double[] field_2495;

    public AmplifiedRandomLevelSource(Level level, long seed) {
        this.level = level;
        this.random = new Random(seed);
        this.field_2483 = new PerlinNoise(this.random, 16);
        this.field_2484 = new PerlinNoise(this.random, 16);
        this.field_2485 = new PerlinNoise(this.random, 8);
        this.surface = new PerlinNoise(this.random, 4);
        this.field_2487 = new PerlinNoise(this.random, 4);
        this.field_2473 = new PerlinNoise(this.random, 10);
        this.field_2474 = new PerlinNoise(this.random, 16);
        this.field_2475 = new PerlinNoise(this.random, 8);
    }

    public void fillFromNoise(int i, int j, byte[] bs, Biome[] biomes, double[] ds) {
        int n = 2;
        int n2 = n + 1;
        int n3 = 33;
        int n4 = n + 1;
        this.field_2489 = this.method_1533(this.field_2489, i * n, 0, j * n, n2, n3, n4);
        for (int k = 0; k < n; ++k) {
            for (int i2 = 0; i2 < n; ++i2) {
                for (int i3 = 0; i3 < 32; ++i3) {
                    double d = 0.25;
                    double d2 = this.field_2489[((k + 0) * n4 + (i2 + 0)) * n3 + (i3 + 0)];
                    double d3 = this.field_2489[((k + 0) * n4 + (i2 + 1)) * n3 + (i3 + 0)];
                    double d4 = this.field_2489[((k + 1) * n4 + (i2 + 0)) * n3 + (i3 + 0)];
                    double d5 = this.field_2489[((k + 1) * n4 + (i2 + 1)) * n3 + (i3 + 0)];
                    double d6 = (this.field_2489[((k + 0) * n4 + (i2 + 0)) * n3 + (i3 + 1)] - d2) * d;
                    double d7 = (this.field_2489[((k + 0) * n4 + (i2 + 1)) * n3 + (i3 + 1)] - d3) * d;
                    double d8 = (this.field_2489[((k + 1) * n4 + (i2 + 0)) * n3 + (i3 + 1)] - d4) * d;
                    double d9 = (this.field_2489[((k + 1) * n4 + (i2 + 1)) * n3 + (i3 + 1)] - d5) * d;
                    for (int i4 = 0; i4 < 4; ++i4) {
                        double d10 = 0.125;
                        double d11 = d2;
                        double d12 = d3;
                        double d13 = (d4 - d2) * d10;
                        double d14 = (d5 - d3) * d10;
                        for (int i5 = 0; i5 < 8; ++i5) {
                            int n5 = i5 + k * 8 << 11 | 0 + i2 * 8 << 7 | i3 * 4 + i4;
                            int n6 = 128;
                            double d15 = 0.125;
                            double d16 = d11;
                            double d17 = (d12 - d11) * d15;
                            for (int i6 = 0; i6 < 8; ++i6) {
                                int n7 = 0;
                                if (d16 > 0.0) {
                                    n7 = Tile.STONE.id;
                                }
                                bs[n5] = (byte)n7;
                                n5 += n6;
                                d16 += d17;
                            }
                            d11 += d13;
                            d12 += d14;
                        }
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                        d5 += d9;
                    }
                }
            }
        }
    }

    public void decorate(int i, int j, byte[] bs, Biome[] biomes) {
        int n = 64;
        double d = 0.03125;
        this.beachNoise = this.surface.method_1091(this.beachNoise, i * 16, j * 16, 0.0, 16, 16, 1, d, d, 1.0);
        this.gravelNoise = this.surface.method_1091(this.gravelNoise, i * 16, 109.0134, j * 16, 16, 1, 16, d, 1.0, d);
        this.field_2492 = this.field_2487.method_1091(this.field_2492, i * 16, j * 16, 0.0, 16, 16, 1, d * 2.0, d * 2.0, d * 2.0);
        for (int k = 0; k < 16; ++k) {
            for (int i2 = 0; i2 < 16; ++i2) {
                Biome biome = biomes[k + i2 * 16];
                boolean bl = this.beachNoise[k + i2 * 16] + this.random.nextDouble() * 0.2 > 0.0;
                boolean bl2 = this.gravelNoise[k + i2 * 16] + this.random.nextDouble() * 0.2 > 3.0;
                int n2 = (int)(this.field_2492[k + i2 * 16] / 3.0 + 3.0 + this.random.nextDouble() * 0.25);
                int n3 = -1;
                byte by = biome.topMaterial;
                byte by2 = biome.underMaterial;
                for (int i3 = 127; i3 >= 0; --i3) {
                    int n4 = (i2 * 16 + k) * 128 + i3;
                    if (i3 <= 0 + this.random.nextInt(5)) {
                        bs[n4] = (byte)Tile.BEDROCK.id;
                        continue;
                    }
                    byte by3 = bs[n4];
                    if (by3 == 0) {
                        n3 = -1;
                        continue;
                    }
                    if (by3 != Tile.STONE.id) continue;
                    if (n3 == -1) {
                        if (n2 <= 0) {
                            by = 0;
                            by2 = (byte)Tile.STONE.id;
                        } else if (i3 >= n - 4 && i3 <= n + 1) {
                            by = biome.topMaterial;
                            by2 = biome.underMaterial;
                            if (bl2) {
                                by = 0;
                            }
                            if (bl2) {
                                by2 = (byte)Tile.GRAVEL.id;
                            }
                            if (bl) {
                                by = (byte)Tile.SAND.id;
                            }
                            if (bl) {
                                by2 = (byte)Tile.SAND.id;
                            }
                        }
                        if (i3 < n && by == 0) {
                            by = (byte)Tile.WATER.id;
                        }
                        n3 = n2;
                        if (i3 >= n - 1) {
                            bs[n4] = by;
                            continue;
                        }
                        bs[n4] = by2;
                        continue;
                    }
                    if (n3 <= 0) continue;
                    bs[n4] = by2;
                    if (--n3 != 0 || by2 != Tile.SAND.id) continue;
                    n3 = this.random.nextInt(4);
                    by2 = (byte)Tile.SANDSTONE.id;
                }
            }
        }
    }

    public Chunk loadChunk(int i, int j) {
        return this.getChunk(i, j);
    }

    public Chunk getChunk(int i, int j) {
        this.random.setSeed((long)i * 341873128712L + (long)j * 132897987541L);
        byte[] byArray = new byte[32768];
        Chunk chunk = new Chunk(this.level, byArray, i, j);
        this.biomes = this.level.getBiomeProvider().method_1227(this.biomes, i * 16, j * 16, 16, 16);
        double[] dArray = this.level.getBiomeProvider().temperature;
        this.fillFromNoise(i, j, byArray, this.biomes, dArray);
        this.decorate(i, j, byArray, this.biomes);
        this.carver.place(this, this.level, i, j, byArray);
        chunk.method_637();
        return chunk;
    }

    private double[] method_1533(double[] ds, int i, int j, int k, int l, int m, int n) {
        if (ds == null) {
            ds = new double[l * m * n];
        }
        double d = 684.412;
        double d2 = 684.412;
        double[] dArray = this.level.getBiomeProvider().temperature;
        double[] dArray2 = this.level.getBiomeProvider().downfall;
        this.field_2479 = this.field_2473.method_1092(this.field_2479, i, k, l, n, 1.121, 1.121, 0.5);
        this.field_2480 = this.field_2474.method_1092(this.field_2480, i, k, l, n, 200.0, 200.0, 0.5);
        this.field_2476 = this.field_2485.method_1091(this.field_2476, i, j, k, l, m, n, d / 80.0, d2 / 160.0, d / 80.0);
        this.field_2477 = this.field_2483.method_1091(this.field_2477, i, j, k, l, m, n, d, d2, d);
        this.field_2478 = this.field_2484.method_1091(this.field_2478, i, j, k, l, m, n, d, d2, d);
        int n2 = 0;
        int n3 = 0;
        int n4 = 16 / l;
        for (int i2 = 0; i2 < l; ++i2) {
            int n5 = i2 * n4 + n4 / 2;
            for (int i3 = 0; i3 < n; ++i3) {
                double d3;
                int n6 = i3 * n4 + n4 / 2;
                double d4 = dArray[n5 * 16 + n6];
                double d5 = dArray2[n5 * 16 + n6] * d4;
                double d6 = 1.0 - d5;
                d6 *= d6;
                d6 *= d6;
                d6 = 1.0 - d6;
                double d7 = (this.field_2479[n3] + 256.0) / 512.0;
                if ((d7 *= d6) > 1.0) {
                    d7 = 1.0;
                }
                if ((d3 = this.field_2480[n3] / 8000.0) < 0.0) {
                    d3 = -d3 * 0.3;
                }
                if ((d3 = d3 * 3.0 - 2.0) < 0.0) {
                    if ((d3 /= 2.0) < -1.0) {
                        d3 = -1.0;
                    }
                    d3 /= 1.4;
                    d3 /= 2.0;
                    d7 = 0.0;
                } else {
                    if (d3 > 1.0) {
                        d3 = 1.0;
                    }
                    d3 /= 8.0;
                }
                if (d7 < 0.0) {
                    d7 = 0.0;
                }
                d7 += 0.5;
                d3 = d3 * (double)m / 16.0;
                double d8 = (double)m / 2.0 + d3 * 4.0;
                ++n3;
                for (int i4 = 0; i4 < m; ++i4) {
                    double d9 = 0.0;
                    double d10 = ((double)i4 - d8) * 12.0 / d7;
                    if (d10 < 0.0) {
                        d10 *= 4.0;
                    }
                    double d11 = this.field_2477[n2] / 512.0;
                    double d12 = this.field_2478[n2] / 512.0;
                    double d13 = (this.field_2476[n2] / 10.0 + 1.0) / 2.0;
                    d9 = d13 < 0.0 ? d11 : (d13 > 1.0 ? d12 : d11 + (d12 - d11) * d13);
                    d9 -= d10;
                    if (i4 > m - 4) {
                        double d14 = (float)(i4 - (m - 4)) / 3.0f;
                        d9 = d9 * (1.0 - d14) + -10.0 * d14;
                    }
                    ds[n2] = d9;
                    ++n2;
                }
            }
        }
        return ds;
    }

    public boolean hasChunk(int i, int j) {
        return true;
    }

    public void generate(LevelSource levelSource, int i, int j) {
        int n;
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        int n9;
        SandTile.generating = true;
        int n10 = i * 16;
        int n11 = j * 16;
        Biome biome = this.level.getBiomeProvider().getBiome(n10 + 16, n11 + 16);
        this.random.setSeed(this.level.getSeed());
        long l = this.random.nextLong() / 2L * 2L + 1L;
        long l2 = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed((long)i * l + (long)j * l2 ^ this.level.getSeed());
        double d = 0.25;
        if (this.random.nextInt(4) == 0) {
            n9 = n10 + this.random.nextInt(16) + 8;
            n8 = this.random.nextInt(128);
            n7 = n11 + this.random.nextInt(16) + 8;
            new PondFeature(Tile.WATER.id).generateFeature(this.level, this.random, n9, n8, n7);
        }
        if (this.random.nextInt(8) == 0) {
            n9 = n10 + this.random.nextInt(16) + 8;
            n8 = this.random.nextInt(this.random.nextInt(120) + 8);
            n7 = n11 + this.random.nextInt(16) + 8;
            if (n8 < 64 || this.random.nextInt(10) == 0) {
                new PondFeature(Tile.LAVA.id).generateFeature(this.level, this.random, n9, n8, n7);
            }
        }
        for (n9 = 0; n9 < 8; ++n9) {
            n8 = n10 + this.random.nextInt(16) + 8;
            n7 = this.random.nextInt(128);
            n6 = n11 + this.random.nextInt(16) + 8;
            new DungeonFeature().generateFeature(this.level, this.random, n8, n7, n6);
        }
        for (n9 = 0; n9 < 10; ++n9) {
            n8 = n10 + this.random.nextInt(16);
            n7 = this.random.nextInt(128);
            n6 = n11 + this.random.nextInt(16);
            new class_227(32).generateFeature(this.level, this.random, n8, n7, n6);
        }
        for (n9 = 0; n9 < 20; ++n9) {
            n8 = n10 + this.random.nextInt(16);
            n7 = this.random.nextInt(128);
            n6 = n11 + this.random.nextInt(16);
            new OreFeature(Tile.DIRT.id, 32).generateFeature(this.level, this.random, n8, n7, n6);
        }
        for (n9 = 0; n9 < 10; ++n9) {
            n8 = n10 + this.random.nextInt(16);
            n7 = this.random.nextInt(128);
            n6 = n11 + this.random.nextInt(16);
            new OreFeature(Tile.GRAVEL.id, 32).generateFeature(this.level, this.random, n8, n7, n6);
        }
        for (n9 = 0; n9 < 20; ++n9) {
            n8 = n10 + this.random.nextInt(16);
            n7 = this.random.nextInt(128);
            n6 = n11 + this.random.nextInt(16);
            new OreFeature(Tile.COAL_ORE.id, 16).generateFeature(this.level, this.random, n8, n7, n6);
        }
        for (n9 = 0; n9 < 20; ++n9) {
            n8 = n10 + this.random.nextInt(16);
            n7 = this.random.nextInt(64);
            n6 = n11 + this.random.nextInt(16);
            new OreFeature(Tile.IRON_ORE.id, 8).generateFeature(this.level, this.random, n8, n7, n6);
        }
        for (n9 = 0; n9 < 2; ++n9) {
            n8 = n10 + this.random.nextInt(16);
            n7 = this.random.nextInt(32);
            n6 = n11 + this.random.nextInt(16);
            new OreFeature(Tile.GOLD_ORE.id, 8).generateFeature(this.level, this.random, n8, n7, n6);
        }
        for (n9 = 0; n9 < 8; ++n9) {
            n8 = n10 + this.random.nextInt(16);
            n7 = this.random.nextInt(16);
            n6 = n11 + this.random.nextInt(16);
            new OreFeature(Tile.REDSTONE_ORE.id, 7).generateFeature(this.level, this.random, n8, n7, n6);
        }
        for (n9 = 0; n9 < 1; ++n9) {
            n8 = n10 + this.random.nextInt(16);
            n7 = this.random.nextInt(16);
            n6 = n11 + this.random.nextInt(16);
            new OreFeature(Tile.DIAMOND_ORE.id, 7).generateFeature(this.level, this.random, n8, n7, n6);
        }
        for (n9 = 0; n9 < 1; ++n9) {
            n8 = n10 + this.random.nextInt(16);
            n7 = this.random.nextInt(16) + this.random.nextInt(16);
            n6 = n11 + this.random.nextInt(16);
            new OreFeature(Tile.LAPIS_ORE.id, 6).generateFeature(this.level, this.random, n8, n7, n6);
        }
        d = 0.5;
        n9 = (int)((this.field_2475.getValue((double)n10 * d, (double)n11 * d) / 8.0 + this.random.nextDouble() * 4.0 + 4.0) / 3.0);
        n8 = 0;
        if (this.random.nextInt(10) == 0) {
            ++n8;
        }
        if (biome == Biome.FOREST) {
            n8 += n9 + 5;
        }
        if (biome == Biome.RAINFOREST) {
            n8 += n9 + 5;
        }
        if (biome == Biome.SEASONAL_FOREST) {
            n8 += n9 + 2;
        }
        if (biome == Biome.TAIGA) {
            n8 += n9 + 5;
        }
        if (biome == Biome.DESERT) {
            n8 -= 20;
        }
        if (biome == Biome.TUNDRA) {
            n8 -= 20;
        }
        if (biome == Biome.PLAINS) {
            n8 -= 20;
        }
        for (n7 = 0; n7 < n8; ++n7) {
            n6 = n10 + this.random.nextInt(16) + 8;
            n5 = n11 + this.random.nextInt(16) + 8;
            Feature feature = biome.method_1462(this.random);
            feature.method_800(1.0, 1.0, 1.0);
            feature.generateFeature(this.level, this.random, n6, this.level.getYHeight(n6, n5), n5);
        }
        n7 = 0;
        if (biome == Biome.FOREST) {
            n7 = 2;
        }
        if (biome == Biome.SEASONAL_FOREST) {
            n7 = 4;
        }
        if (biome == Biome.TAIGA) {
            n7 = 2;
        }
        if (biome == Biome.PLAINS) {
            n7 = 3;
        }
        for (n6 = 0; n6 < n7; ++n6) {
            n5 = n10 + this.random.nextInt(16) + 8;
            int n12 = this.random.nextInt(128);
            n4 = n11 + this.random.nextInt(16) + 8;
            new PlantPatchFeature(Tile.FLOWER.id).generateFeature(this.level, this.random, n5, n12, n4);
        }
        n6 = 0;
        if (biome == Biome.FOREST) {
            n6 = 2;
        }
        if (biome == Biome.RAINFOREST) {
            n6 = 10;
        }
        if (biome == Biome.SEASONAL_FOREST) {
            n6 = 2;
        }
        if (biome == Biome.TAIGA) {
            n6 = 1;
        }
        if (biome == Biome.PLAINS) {
            n6 = 10;
        }
        for (n5 = 0; n5 < n6; ++n5) {
            int n13 = 1;
            if (biome == Biome.RAINFOREST && this.random.nextInt(3) != 0) {
                n13 = 2;
            }
            n4 = n10 + this.random.nextInt(16) + 8;
            n3 = this.random.nextInt(128);
            n2 = n11 + this.random.nextInt(16) + 8;
            new TallPatchFeature(Tile.TALL_GRASS.id, n13).generateFeature(this.level, this.random, n4, n3, n2);
        }
        n6 = 0;
        if (biome == Biome.DESERT) {
            n6 = 2;
        }
        for (n5 = 0; n5 < n6; ++n5) {
            int n14 = n10 + this.random.nextInt(16) + 8;
            n4 = this.random.nextInt(128);
            n3 = n11 + this.random.nextInt(16) + 8;
            new PatchFeature(Tile.DEAD_BUSH.id).generateFeature(this.level, this.random, n14, n4, n3);
        }
        if (this.random.nextInt(2) == 0) {
            n5 = n10 + this.random.nextInt(16) + 8;
            int n15 = this.random.nextInt(128);
            n4 = n11 + this.random.nextInt(16) + 8;
            new PlantPatchFeature(Tile.ROSE.id).generateFeature(this.level, this.random, n5, n15, n4);
        }
        if (this.random.nextInt(4) == 0) {
            n5 = n10 + this.random.nextInt(16) + 8;
            int n16 = this.random.nextInt(128);
            n4 = n11 + this.random.nextInt(16) + 8;
            new PlantPatchFeature(Tile.BROWN_MUSHROOM.id).generateFeature(this.level, this.random, n5, n16, n4);
        }
        if (this.random.nextInt(8) == 0) {
            n5 = n10 + this.random.nextInt(16) + 8;
            int n17 = this.random.nextInt(128);
            n4 = n11 + this.random.nextInt(16) + 8;
            new PlantPatchFeature(Tile.RED_MUSHROOM.id).generateFeature(this.level, this.random, n5, n17, n4);
        }
        for (n5 = 0; n5 < 10; ++n5) {
            int n18 = n10 + this.random.nextInt(16) + 8;
            n4 = this.random.nextInt(128);
            n3 = n11 + this.random.nextInt(16) + 8;
            new ReedsFeature().generateFeature(this.level, this.random, n18, n4, n3);
        }
        if (this.random.nextInt(32) == 0) {
            n5 = n10 + this.random.nextInt(16) + 8;
            int n19 = this.random.nextInt(128);
            n4 = n11 + this.random.nextInt(16) + 8;
            new PumpkinPatchFeature().generateFeature(this.level, this.random, n5, n19, n4);
        }
        n5 = 0;
        if (biome == Biome.DESERT) {
            n5 += 10;
        }
        for (n = 0; n < n5; ++n) {
            n4 = n10 + this.random.nextInt(16) + 8;
            n3 = this.random.nextInt(128);
            n2 = n11 + this.random.nextInt(16) + 8;
            new CactusFeature().generateFeature(this.level, this.random, n4, n3, n2);
        }
        for (n = 0; n < 50; ++n) {
            n4 = n10 + this.random.nextInt(16) + 8;
            n3 = this.random.nextInt(this.random.nextInt(120) + 8);
            n2 = n11 + this.random.nextInt(16) + 8;
            new class_554(Tile.FLOWING_WATER.id).generateFeature(this.level, this.random, n4, n3, n2);
        }
        for (n = 0; n < 20; ++n) {
            n4 = n10 + this.random.nextInt(16) + 8;
            n3 = this.random.nextInt(this.random.nextInt(this.random.nextInt(112) + 8) + 8);
            n2 = n11 + this.random.nextInt(16) + 8;
            new class_554(Tile.FLOWING_LAVA.id).generateFeature(this.level, this.random, n4, n3, n2);
        }
        this.field_2495 = this.level.getBiomeProvider().method_1226(this.field_2495, n10 + 8, n11 + 8, 16, 16);
        for (n = n10 + 8; n < n10 + 8 + 16; ++n) {
            for (n4 = n11 + 8; n4 < n11 + 8 + 16; ++n4) {
                n3 = n - (n10 + 8);
                n2 = n4 - (n11 + 8);
                int n20 = this.level.getTopY(n, n4);
                double d2 = this.field_2495[n3 * 16 + n2] - (double)(n20 - 64) / 64.0 * 0.3;
                if (!(d2 < 0.5) || n20 <= 0 || n20 >= 128 || !this.level.isAir(n, n20, n4) || !this.level.getMaterial(n, n20 - 1, n4).method_651() || this.level.getMaterial(n, n20 - 1, n4) == Material.field_3336) continue;
                this.level.setTileWithUpdate(n, n20, n4, Tile.SNOW_LAYER.id);
            }
        }
        SandTile.generating = false;
    }

    public boolean save(boolean bl, class_441 arg) {
        return true;
    }

    public boolean method_71() {
        return false;
    }

    public boolean supportsSaving() {
        return true;
    }

    public String getDebugInfo() {
        return "RandomLevelSource";
    }

    @Override
    public boolean hasChunk(int x, int y, int z) {
        return false;
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        return null;
    }

    @Override
    public Chunk loadChunk(int x, int y, int z) {
        return null;
    }

    @Override
    public void generate(LevelSource chunkGenerator, int x, int y, int z) {

    }

    @Override
    public BiomeProvider getBiomeProvider(int y) {
        return this.level.getBiomeProvider();
    }

    @Override
    public Dimension getDimension(int y) {
        return this.level.dimension;
    }
}

