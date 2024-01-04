package me.alphamode.mixin;

import me.alphamode.ext.LevelExt;
import net.minecraft.LightUpdater;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.tile.Tile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.ForkJoinPool;

@Mixin(LightUpdater.class)
public class LightUpdaterMixin {

    @Shadow public int z1;

    @Shadow public int y1;

    @Shadow @Final public LightLayer type;

    @Shadow public int x1;

    @Shadow public int x0;
    @Shadow public int y0;
    @Shadow public int z0;

//    /**
//     * @author
//     * @reason
//     */
//    @Overwrite
//    public void update(Level level) {
////        POOL.execute(() -> {
//            int width = this.x1 - this.x0 + 1;
//            int height = this.y1 - this.y0 + 1;
//            int length = this.z1 - this.z0 + 1;
//            int area = width * height * length;
//            if (area > 32768) {
//                System.out.println("Light too large, skipping!");
//            } else {
//                int lastChunkX = 0;
//                int lastChunkY = 0;
//                int lastChunkZ = 0;
//                boolean var8 = false;
//                boolean var9 = false;
//
//                for (int x = this.x0; x <= this.x1; ++x) {
//                    for (int y = this.y0; y <= this.y1; ++y) {
//                        for (int z = this.z0; z <= this.z1; ++z) {
//                            int chunkX = x >> 4;
//                            int chunkY = y >> 4;
//                            int chunkZ = z >> 4;
//                            boolean isLoaded = false;
//                            if (var8 && chunkX == lastChunkX && chunkY == lastChunkY && chunkZ == lastChunkZ) {
//                                isLoaded = var9;
//                            } else {
//                                isLoaded = level.method_1425(x, y, z, 1);
//                                if (isLoaded) {
//                                    Chunk chunk = level.getChunk(x >> 4, y >> 4, z >> 4);
//                                    if (chunk.isEmptyChunk()) {
//                                        isLoaded = false;
//                                    }
//                                }
//
//                                var9 = isLoaded;
//                                lastChunkX = chunkX;
//                                lastChunkY = chunkY;
//                                lastChunkZ = chunkZ;
//                            }
//
//                            if (isLoaded) {
//                                int levelBrightness = level.getBrightness(this.type, x, y, z);
//                                int var17 = 0;
//                                int tile = level.getTile(x, y, z);
//                                int opacity = Tile.OPACITIES[tile];
//                                if (opacity == 0) {
//                                    opacity = 1;
//                                }
//
//                                int var20 = 0;
//                                if (this.type == LightLayer.SKY) {
//                                    if (level.method_308(x, y, z)) {
//                                        var20 = 15;
//                                    }
//                                } else if (this.type == LightLayer.BLOCK) {
//                                    var20 = Tile.LIGHT_TILES[tile];
//                                }
//
//                                if (opacity >= 15 && var20 == 0) {
//                                    var17 = 0;
//                                } else {
//                                    int var21 = level.getBrightness(this.type, x - 1, y, z);
//                                    int var22 = level.getBrightness(this.type, x + 1, y, z);
//                                    int var23 = level.getBrightness(this.type, x, y - 1, z);
//                                    int var24 = level.getBrightness(this.type, x, y + 1, z);
//                                    int var25 = level.getBrightness(this.type, x, y, z - 1);
//                                    int var26 = level.getBrightness(this.type, x, y, z + 1);
//                                    var17 = var21;
//                                    if (var22 > var21) {
//                                        var17 = var22;
//                                    }
//
//                                    if (var23 > var17) {
//                                        var17 = var23;
//                                    }
//
//                                    if (var24 > var17) {
//                                        var17 = var24;
//                                    }
//
//                                    if (var25 > var17) {
//                                        var17 = var25;
//                                    }
//
//                                    if (var26 > var17) {
//                                        var17 = var26;
//                                    }
//
//                                    var17 -= opacity;
//                                    if (var17 < 0) {
//                                        var17 = 0;
//                                    }
//
//                                    if (var20 > var17) {
//                                        var17 = var20;
//                                    }
//                                }
//
//                                if (levelBrightness != var17) {
//                                    level.setLightLevel(this.type, x, y, z, var17);
//                                    int var31 = var17 - 1;
//                                    if (var31 < 0) {
//                                        var31 = 0;
//                                    }
//
//                                    level.method_227(this.type, x - 1, y, z, var31);
//                                    level.method_227(this.type, x, y - 1, z, var31);
//                                    level.method_227(this.type, x, y, z - 1, var31);
//                                    if (x + 1 >= this.x1) {
//                                        level.method_227(this.type, x + 1, y, z, var31);
//                                    }
//
//                                    if (y + 1 >= this.y1) {
//                                        level.method_227(this.type, x, y + 1, z, var31);
//                                    }
//
//                                    if (z + 1 >= this.z1) {
//                                        level.method_227(this.type, x, y, z + 1, var31);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
////        });
//    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void update(Level level) {
//        int width = this.x1 - this.x0 + 1;
//        int height = this.y1 - this.y0 + 1;
//        int length = this.z1 - this.z0 + 1;
//        int area = width * height * length;
//        if (area > 32768) {
//            System.out.println("Light too large, skipping!");
//        } else {
//            int lastChunkX = 0;
//            int lastChunkY = 0;
//            int lastChunkZ = 0;
//            boolean var8 = false;
//            boolean var9 = false;
//
//            for(int x = this.x0; x <= this.x1; ++x) {
//                for(int y = this.y0; y <= this.y1; ++y) {
//                for(int z = this.z0; z <= this.z1; ++z) {
//                    int chunkX = x >> 4;
//                    int chunkY = y >> 4;
//                    int chunkZ = z >> 4;
//                    boolean var14 = false;
//                    if (var8 && chunkX == lastChunkX && chunkY == lastChunkY && chunkZ == lastChunkZ) {
//                        var14 = var9;
//                    } else {
//                        var14 = level.method_1425(x, y, z, 1);
//                        if (var14) {
//                            Chunk var15 = level.getChunk(x >> 4, y >> 4, z >> 4);
//                            if (var15.isEmptyChunk()) {
//                                var14 = false;
//                            }
//                        }
//
//                        var9 = var14;
//                        lastChunkX = chunkX;
//                        lastChunkZ = chunkZ;
//                    }
//
//                    if (var14) {
//                        if (this.y0 < 0) {
//                            this.y0 = 0;
//                        }
//
//                        if (this.y1 >= 128) {
//                            this.y1 = 127;
//                        }
//                            int var16 = level.getBrightness(this.type, x, y, z);
//                            int var17 = 0;
//                            int var18 = level.getTile(x, y, z);
//                            int var19 = Tile.OPACITIES[var18];
//                            if (var19 == 0) {
//                                var19 = 1;
//                            }
//
//                            int var20 = 0;
//                            if (this.type == LightLayer.SKY) {
//                                if (level.method_308(x, y, z)) {
//                                    var20 = 15;
//                                }
//                            } else if (this.type == LightLayer.BLOCK) {
//                                var20 = Tile.LIGHT_TILES[var18];
//                            }
//
//                            if (var19 >= 15 && var20 == 0) {
//                                var17 = 0;
//                            } else {
//                                int var21 = level.getBrightness(this.type, x - 1, y, z);
//                                int var22 = level.getBrightness(this.type, x + 1, y, z);
//                                int var23 = level.getBrightness(this.type, x, y - 1, z);
//                                int var24 = level.getBrightness(this.type, x, y + 1, z);
//                                int var25 = level.getBrightness(this.type, x, y, z - 1);
//                                int var26 = level.getBrightness(this.type, x, y, z + 1);
//                                var17 = var21;
//                                if (var22 > var21) {
//                                    var17 = var22;
//                                }
//
//                                if (var23 > var17) {
//                                    var17 = var23;
//                                }
//
//                                if (var24 > var17) {
//                                    var17 = var24;
//                                }
//
//                                if (var25 > var17) {
//                                    var17 = var25;
//                                }
//
//                                if (var26 > var17) {
//                                    var17 = var26;
//                                }
//
//                                var17 -= var19;
//                                if (var17 < 0) {
//                                    var17 = 0;
//                                }
//
//                                if (var20 > var17) {
//                                    var17 = var20;
//                                }
//                            }
//
//                            if (var16 != var17) {
//                                level.setLightLevel(this.type, x, y, z, var17);
//                                int var31 = var17 - 1;
//                                if (var31 < 0) {
//                                    var31 = 0;
//                                }
//
//                                level.method_227(this.type, x - 1, y, z, var31);
//                                level.method_227(this.type, x, y - 1, z, var31);
//                                level.method_227(this.type, x, y, z - 1, var31);
//                                if (x + 1 >= this.x1) {
//                                    level.method_227(this.type, x + 1, y, z, var31);
//                                }
//
//                                if (y + 1 >= this.y1) {
//                                    level.method_227(this.type, x, y + 1, z, var31);
//                                }
//
//                                if (z + 1 >= this.z1) {
//                                    level.method_227(this.type, x, y, z + 1, var31);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }
}
