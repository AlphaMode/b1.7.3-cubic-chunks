package me.alphamode.world.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.Level;
import net.minecraft.world.tile.Tile;

import java.util.Random;

public class CubicCaveWorldCarver extends CubicWorldCarver {
    protected void genRoom(int x, int y, int z, byte[] tiles, double randX, double randY, double randZ) {
        this.genTunnel(x, y, z, tiles, randX, randY, randZ, 1.0F + this.random.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5);
    }

    protected void genTunnel(int x, int y, int z, byte[] tiles, double d, double e, double f, float g, float h, float k, int l, int m, double n) {
        double relX = (double)(x * 16 + 8);
        double relY = (double)(y * 16 + 8);
        double relZ = (double)(z * 16 + 8);
        float var21 = 0.0F;
        float var22 = 0.0F;
        Random rand = new Random(this.random.nextLong());
        if (m <= 0) {
            int var24 = this.radius * 16 - 16;
            m = var24 - rand.nextInt(var24 / 4);
        }

        boolean var55 = false;
        if (l == -1) {
            l = m / 2;
            var55 = true;
        }

        int var25 = rand.nextInt(m / 2) + m / 4;

        for(boolean var26 = rand.nextInt(6) == 0; l < m; ++l) {
            double var27 = 1.5 + (double)(Mth.sin((float)l * (float) Math.PI / (float)m) * g * 1.0F);
            double var29 = var27 * n;
            float var31 = Mth.cos(k);
            float var32 = Mth.sin(k);
            d += (double)(Mth.cos(h) * var31);
            e += (double)var32;
            f += (double)(Mth.sin(h) * var31);
            if (var26) {
                k *= 0.92F;
            } else {
                k *= 0.7F;
            }

            k += var22 * 0.1F;
            h += var21 * 0.1F;
            var22 *= 0.9F;
            var21 *= 0.75F;
            var22 += (rand.nextFloat() - rand.nextFloat()) * rand.nextFloat() * 2.0F;
            var21 += (rand.nextFloat() - rand.nextFloat()) * rand.nextFloat() * 4.0F;
            if (!var55 && l == var25 && g > 1.0F) {
                this.genTunnel(x, y, z, tiles, d, e, f, rand.nextFloat() * 0.5F + 0.5F, h - (float) (Math.PI / 2), k / 3.0F, l, m, 1.0);
                this.genTunnel(x, y, z, tiles, d, e, f, rand.nextFloat() * 0.5F + 0.5F, h + (float) (Math.PI / 2), k / 3.0F, l, m, 1.0);
                return;
            }

            if (var55 || rand.nextInt(4) != 0) {
                double var33 = d - relX;
                double var35 = f - relZ;
                double var37 = (double)(m - l);
                double var39 = (double)(g + 2.0F + 16.0F);
                if (var33 * var33 + var35 * var35 - var37 * var37 > var39 * var39) {
                    return;
                }

                if (!(d < relX - 16.0 - var27 * 2.0) && !(f < relZ - 16.0 - var27 * 2.0) && !(d > relX + 16.0 + var27 * 2.0) && !(f > relZ + 16.0 + var27 * 2.0)) {
                    int var56 = Mth.floor(d - var27) - x * 16 - 1;
                    int var34 = Mth.floor(d + var27) - x * 16 + 1;
                    int var57 = Mth.floor(e - var29) - 1;
                    int var36 = Mth.floor(e + var29) + 1;
                    int var58 = Mth.floor(f - var27) - z * 16 - 1;
                    int var38 = Mth.floor(f + var27) - z * 16 + 1;
                    if (var56 < 0) {
                        var56 = 0;
                    }

                    if (var34 > 16) {
                        var34 = 16;
                    }

                    if (var57 < 1) {
                        var57 = 1;
                    }

                    if (var36 > 120) {
                        var36 = 120;
                    }

                    if (var58 < 0) {
                        var58 = 0;
                    }

                    if (var38 > 16) {
                        var38 = 16;
                    }

                    boolean var59 = false;

                    for(int var40 = var56; !var59 && var40 < var34; ++var40) {
                        for(int var41 = var58; !var59 && var41 < var38; ++var41) {
                            for(int var42 = var36 + 1; !var59 && var42 >= var57 - 1; --var42) {
                                int var43 = (var40 * 16 + var41) * 128 + var42;
                                if (var42 >= 0 && var42 < 128) {
                                    if (tiles[var43] == Tile.FLOWING_WATER.id || tiles[var43] == Tile.WATER.id) {
                                        var59 = true;
                                    }

                                    if (var42 != var57 - 1 && var40 != var56 && var40 != var34 - 1 && var41 != var58 && var41 != var38 - 1) {
                                        var42 = var57;
                                    }
                                }
                            }
                        }
                    }

                    if (!var59) {
                        for(int var60 = var56; var60 < var34; ++var60) {
                            double var61 = ((double)(var60 + x * 16) + 0.5 - d) / var27;

                            for(int var62 = var58; var62 < var38; ++var62) {
                                double var44 = ((double)(var62 + z * 16) + 0.5 - f) / var27;
                                int var46 = (var60 * 16 + var62) * 128 + var36;
                                boolean var47 = false;
                                if (var61 * var61 + var44 * var44 < 1.0) {
                                    for(int var48 = var36 - 1; var48 >= var57; --var48) {
                                        double var49 = ((double)var48 + 0.5 - e) / var29;
                                        if (var49 > -0.7 && var61 * var61 + var49 * var49 + var44 * var44 < 1.0) {
                                            byte var51 = tiles[var46];
                                            if (var51 == Tile.GRASS.id) {
                                                var47 = true;
                                            }

                                            if (var51 == Tile.STONE.id || var51 == Tile.DIRT.id || var51 == Tile.GRASS.id) {
                                                if (var48 < 10) {
                                                    tiles[var46] = (byte)Tile.FLOWING_LAVA.id;
                                                } else {
                                                    tiles[var46] = 0;
                                                    if (var47 && tiles[var46 - 1] == Tile.DIRT.id) {
                                                        tiles[var46 - 1] = (byte)Tile.GRASS.id;
                                                    }
                                                }
                                            }
                                        }

                                        --var46;
                                    }
                                }
                            }
                        }

                        if (var55) {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void carve(Level level, int chunkX, int chunkY, int chunkZ, int x, int y, int z, byte[] tiles) {
        int var7 = this.random.nextInt(this.random.nextInt(this.random.nextInt(40) + 1) + 1);
        if (this.random.nextInt(15) != 0) {
            var7 = 0;
        }

        for(int var8 = 0; var8 < var7; ++var8) {
            double randX = (double)(chunkX * 16 + this.random.nextInt(16));
            double randY = (double)(chunkY * 16 + this.random.nextInt(16));//(double)this.random.nextInt(this.random.nextInt(120) + 8);
            double randZ = (double)(chunkZ * 16 + this.random.nextInt(16));
            int var15 = 1;
            if (this.random.nextInt(4) == 0) {
                this.genRoom(x, y, z, tiles, randX, randY, randZ);
                var15 += this.random.nextInt(4);
            }

            for(int var16 = 0; var16 < var15; ++var16) {
                float var17 = this.random.nextFloat() * (float) Math.PI * 2.0F;
                float var18 = (this.random.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float var19 = this.random.nextFloat() * 2.0F + this.random.nextFloat();
                this.genTunnel(x, y, z, tiles, randX, randY, randZ, var19, var17, var18, 0, 0, 1.0);
            }
        }
    }
}
