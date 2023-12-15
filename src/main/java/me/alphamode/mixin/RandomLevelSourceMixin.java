package me.alphamode.mixin;

import me.alphamode.gen.CubicLevelSource;
import me.alphamode.world.CubicChunk;
import me.alphamode.world.levelgen.CubicCaveWorldCarver;
import me.alphamode.world.levelgen.CubicWorldCarver;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.level.levelgen.LevelSource;
import net.minecraft.world.level.levelgen.RandomLevelSource;
import net.minecraft.world.level.levelgen.WorldCarver;
import net.minecraft.world.level.levelgen.biome.Biome;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Mixin(RandomLevelSource.class)
public abstract class RandomLevelSourceMixin implements CubicLevelSource {
    @Shadow private Random random;

    @Shadow private Level level;

    @Shadow private Biome[] biomes;

    @Shadow private double[] field_2489;

    @Shadow public PerlinSimplexNoise field_2475;

    @Shadow private double[] field_2495;

    @Shadow private double[] beachNoise;

    @Shadow private double[] gravelNoise;

    @Shadow private double[] field_2492;

    @Shadow private PerlinSimplexNoise surface;

    @Shadow private PerlinSimplexNoise field_2487;

    @Shadow private WorldCarver carver;
    private final CubicWorldCarver cubicCarver = new CubicCaveWorldCarver();

    @Shadow private double[] field_2479;

    @Shadow private double[] field_2480;

    @Shadow private double[] field_2476;

    @Shadow private double[] field_2477;

    @Shadow private double[] field_2478;

    @Shadow private PerlinSimplexNoise field_2484;

    @Shadow private PerlinSimplexNoise field_2483;

    @Shadow private PerlinSimplexNoise field_2485;

    @Shadow public PerlinSimplexNoise field_2474;

    @Shadow public PerlinSimplexNoise field_2473;

    @Shadow protected abstract double[] method_1533(double[] ds, int i, int j, int k, int l, int m, int n);

    private Map<Integer, Chunk> cache = new HashMap();

    @Override
    public boolean hasChunk(int x, int y, int z) {
        return true;
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        this.random.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        byte[] tiles = new byte[32768]; // switch to 4096 once lighting is fixed
        Chunk chunk = new CubicChunk(this.level, tiles, x, y, z);
        this.biomes = this.level.getBiomeProvider().method_1227(this.biomes, x * 16, z * 16, 16, 16);
        double[] temperature = this.level.getBiomeProvider().temperature;
        this.fillFromNoise(x, y, z, tiles, this.biomes, temperature);
        this.decorate(x, y, z, chunk.tiles, this.biomes);
//        this.cubicCarver.place((LevelSource) this, this.level, x, y, z, tiles);
        chunk.method_637();
        return chunk;
    }

    public void decorate(int x, int y, int z, byte[] tiles, Biome[] biomes) {
        byte seaLevelChunk = 4;
        byte seaLevel = 0;
        double var6 = 0.03125;
        this.beachNoise = this.surface.method_1091(this.beachNoise, (double)(x * 16), (double)(z * 16), 0.0, 16, 16, 1, var6, var6, 1.0);
        this.gravelNoise = this.surface.method_1091(this.gravelNoise, (double)(x * 16), 109.0134, (double)(z * 16), 16, 1, 16, var6, 1.0, var6);
        this.field_2492 = this.field_2487.method_1091(this.field_2492, (double)(x * 16), (double)(z * 16), 0.0, 16, 16, 1, var6 * 2.0, var6 * 2.0, var6 * 2.0);

        for(int tileX = 0; tileX < 16; ++tileX) {
            for(int tileZ = 0; tileZ < 16; ++tileZ) {
                Biome biome = biomes[tileX + tileZ * 16];
                boolean var11 = this.beachNoise[tileX + tileZ * 16] + this.random.nextDouble() * 0.2 > 0.0;
                boolean gravel = this.gravelNoise[tileX + tileZ * 16] + this.random.nextDouble() * 0.2 > 3.0;
                int var13 = (int)(this.field_2492[tileX + tileZ * 16] / 3.0 + 3.0 + this.random.nextDouble() * 0.25);
                int var14 = -1;
                byte topBlock = biome.topMaterial;
                byte underBlock = biome.underMaterial;

                for(int yLevel = 16; yLevel >= 0; --yLevel) {
                    int index = CubicChunk.getIndex(tileX, yLevel, tileZ);//(tileZ * 16 + tileX) * 128 + yLevel;
                    if (y == 0 && yLevel <= 0 + this.random.nextInt(5)) {
//                        tiles[index] = (byte)Tile.BEDROCK.id;
                    } else {
                        byte var19 = tiles[index];
                        if (var19 == 0) {
                            var14 = -1;
                        } else if (var19 == Tile.STONE.id) {
                            if (var14 == -1) {
                                if (var13 <= 0) {
                                    topBlock = 0;
                                    underBlock = (byte)Tile.STONE.id;
                                } else if (yLevel >= seaLevel - 4 && yLevel <= seaLevel + 1) {
                                    topBlock = biome.topMaterial;
                                    underBlock = biome.underMaterial;
                                    if (gravel) {
                                        topBlock = 0;
                                    }

                                    if (gravel) {
                                        underBlock = (byte)Tile.GRAVEL.id;
                                    }

                                    if (var11) {
                                        topBlock = (byte)Tile.SAND.id;
                                    }

                                    if (var11) {
                                        underBlock = (byte)Tile.SAND.id;
                                    }
                                }

                                if (yLevel < seaLevel && topBlock == 0) {
                                    topBlock = (byte)Tile.WATER.id;
                                }

                                var14 = var13;
                                if (yLevel >= seaLevel - 1) {
                                    tiles[index] = topBlock;
                                } else {
                                    tiles[index] = underBlock;
                                }
                            } else if (var14 > 0) {
                                --var14;
                                tiles[index] = underBlock;
                                if (var14 == 0 && underBlock == Tile.SAND.id) {
                                    var14 = this.random.nextInt(4);
                                    underBlock = (byte)Tile.SANDSTONE.id;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void fillFromNoise(int chunkX, int chunkY, int chunkZ, byte[] tiles, Biome[] biomes, double[] temperature) {
        if (chunkY > 15)
            return;
        byte sampleRate = 4;
        byte chunkSeaLevel = 4;
        byte seaLevel = 64;
        int sampleX = sampleRate + 1;
        int sampleY = 17;//sampleRate + 1;
        int sampleZ = sampleRate + 1;
        double scale = 0.25;
        this.field_2489 = this.method_1533(this.field_2489, chunkX * sampleRate, /*chunkY * sampleRate*/0, chunkZ * sampleRate, sampleX, sampleY, sampleZ);

        for(int x = 0; x < sampleRate; ++x) {
            for(int z = 0; z < sampleRate; ++z) {
                for(int y = 0; y < 4; ++y) {

                    double noise1 = this.field_2489[((x + 0) * sampleZ + z + 0) * sampleY + y + 0];
                    double noise2 = this.field_2489[((x + 0) * sampleZ + z + 1) * sampleY + y + 0];
                    double noise3 = this.field_2489[((x + 1) * sampleZ + z + 0) * sampleY + y + 0];
                    double noise4 = this.field_2489[((x + 1) * sampleZ + z + 1) * sampleY + y + 0];
                    double noise1Offset = (this.field_2489[((x + 0) * sampleZ + z + 0) * sampleY + y + 1] - noise1) * 0.125;
                    double noise2Offset = (this.field_2489[((x + 0) * sampleZ + z + 1) * sampleY + y + 1] - noise2) * 0.125;
                    double noise3Offset = (this.field_2489[((x + 1) * sampleZ + z + 0) * sampleY + y + 1] - noise3) * 0.125;
                    double noise4Offset = (this.field_2489[((x + 1) * sampleZ + z + 1) * sampleY + y + 1] - noise4) * 0.125;

                    for(int currentY = 0; currentY < 4; ++currentY) {
                        double var35 = noise1;
                        double var37 = noise2;
                        double var39 = (noise3 - noise1) * scale;
                        double var41 = (noise4 - noise2) * scale;

                        for(int currentX = 0; currentX < 4; ++currentX) {
                            int packedPos = CubicChunk.getIndex(currentX + x * 4, y * 4 + currentY, z * 4);
                            short overflow = 128;
                            double surface = var35;
                            double var50 = (var37 - var35) * scale;

                            for(int currentZ = 0; currentZ < 4; ++currentZ) {
                                double temp = temperature[(x * 4 + currentX) * 16 + z * 4 + currentZ];
                                int tile = 0;
                                if (chunkY < chunkSeaLevel) {
                                    if (temp < 0.5 && y * 4 + currentY >= chunkSeaLevel - 1) {
//                                        tile = Tile.ICE.id;
                                    } else {
//                                        tile = Tile.WATER.id;
                                    }
                                }

                                if (surface > 0.0 || chunkY < 0) {
                                    tile = Tile.STONE.id;
                                }

                                tiles[packedPos] = (byte)tile;
                                packedPos += overflow; // increment the z axis by overflowing the y value
                                surface += var50;
                            }

                            var35 += var39;
                            var37 += var41;
                        }

                        noise1 += noise1Offset;
                        noise2 += noise2Offset;
                        noise3 += noise3Offset;
                        noise4 += noise4Offset;
                    }
                }
            }
        }
    }

    @Override
    public Chunk loadChunk(int x, int y, int z) {
        return this.getChunk(x, y, z);
    }

    @Override
    public void generate(LevelSource chunkGenerator, int x, int y, int z) {

    }

    @Override
    public boolean isCubic() {
        return true;
    }
}
