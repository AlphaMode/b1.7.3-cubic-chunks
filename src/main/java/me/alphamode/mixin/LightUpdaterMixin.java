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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ForkJoinPool;

@Mixin(LightUpdater.class)
public class LightUpdaterMixin {

    @Shadow
    public int z1;

    @Shadow
    public int y1;

    @Shadow
    @Final
    public LightLayer type;

    @Shadow
    public int x1;

    @Shadow
    public int x0;
    @Shadow
    public int y0;
    @Shadow
    public int z0;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void throwEarly(LightLayer x0, int y0, int z0, int x1, int y1, int z1, int par7, CallbackInfo ci) {
        int width = this.x1 - this.x0 + 1;
        int height = this.y1 - this.y0 + 1;
        int length = this.z1 - this.z0 + 1;
        int area = width * height * length;
        if (area > 32768)
            System.out.println("Light too large, skipping!");
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void update(Level level) {
//        POOL.execute(() -> {
            int width = this.x1 - this.x0 + 1;
            int height = this.y1 - this.y0 + 1;
            int length = this.z1 - this.z0 + 1;
            int area = width * height * length;
            if (area > 32768) {
                System.out.println("Light too large, skipping!");
            } else {
                int lastChunkX = 0;
                int lastChunkY = 0;
                int lastChunkZ = 0;
                boolean var8 = false;
                boolean var9 = false;

                for (int x = this.x0; x <= this.x1; ++x) {
                    for (int y = this.y0; y <= this.y1; ++y) {
                        for (int z = this.z0; z <= this.z1; ++z) {
                            int chunkX = x >> 4;
                            int chunkY = y >> 4;
                            int chunkZ = z >> 4;
                            boolean isLoaded = false;
                            if (var8 && chunkX == lastChunkX && chunkY == lastChunkY && chunkZ == lastChunkZ) {
                                isLoaded = var9;
                            } else {
                                isLoaded = level.isAreaLoaded(x, y, z, 1);
                                if (isLoaded) {
                                    Chunk chunk = level.getChunk(x >> 4, y >> 4, z >> 4);
                                    if (chunk.isEmptyChunk()) {
                                        isLoaded = false;
                                    }
                                }

                                var9 = isLoaded;
                                lastChunkX = chunkX;
                                lastChunkY = chunkY;
                                lastChunkZ = chunkZ;
                            }

                            if (isLoaded) {
                                int levelBrightness = level.getBrightness(this.type, x, y, z);
                                int newLevel = 0;
                                int tile = level.getTile(x, y, z);
                                int opacity = Tile.OPACITIES[tile];
                                if (opacity == 0) {
                                    opacity = 1;
                                }

                                int var20 = 0;
                                if (this.type == LightLayer.SKY) {
                                    if (level.isHighestTile(x, y, z)) {
                                        var20 = 15;
                                    }
                                } else if (this.type == LightLayer.BLOCK) {
                                    var20 = Tile.LIGHT_TILES[tile];
                                }

                                if (opacity >= 15 && var20 == 0) {
                                    newLevel = 0;
                                } else {
                                    int xNeg = level.getBrightness(this.type, x - 1, y, z);
                                    int xPos = level.getBrightness(this.type, x + 1, y, z);
                                    int yNeg = level.getBrightness(this.type, x, y - 1, z);
                                    int yPos = level.getBrightness(this.type, x, y + 1, z);
                                    int zNeg = level.getBrightness(this.type, x, y, z - 1);
                                    int zPos = level.getBrightness(this.type, x, y, z + 1);
                                    newLevel = xNeg;
                                    if (xPos > xNeg) {
                                        newLevel = xPos;
                                    }

                                    if (yNeg > newLevel) {
                                        newLevel = yNeg;
                                    }

                                    if (yPos > newLevel) {
                                        newLevel = yPos;
                                    }

                                    if (zNeg > newLevel) {
                                        newLevel = zNeg;
                                    }

                                    if (zPos > newLevel) {
                                        newLevel = zPos;
                                    }

                                    newLevel -= opacity;
                                    if (newLevel < 0) {
                                        newLevel = 0;
                                    }

                                    if (var20 > newLevel) {
                                        newLevel = var20;
                                    }
                                }

                                if (levelBrightness != newLevel) {
                                    level.setLightLevel(this.type, x, y, z, newLevel);
                                    int var31 = newLevel - 1;
                                    if (var31 < 0) {
                                        var31 = 0;
                                    }

                                    level.updateLightLevel(this.type, x - 1, y, z, var31);
                                    level.updateLightLevel(this.type, x, y - 1, z, var31);
                                    level.updateLightLevel(this.type, x, y, z - 1, var31);
                                    if (x + 1 >= this.x1) {
                                        level.updateLightLevel(this.type, x + 1, y, z, var31);
                                    }

                                    if (y + 1 >= this.y1) {
                                        level.updateLightLevel(this.type, x, y + 1, z, var31);
                                    }

                                    if (z + 1 >= this.z1) {
                                        level.updateLightLevel(this.type, x, y, z + 1, var31);
                                    }
                                }
                            }
                        }
                    }
                }
            }
//        });
    }

//    /**
//     * @author
//     * @reason
//     */
//    @Overwrite
//    public void update(Level level) {
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
//
//            for (int x = this.x0; x <= this.x1; ++x) {
//                for (int y = this.y0; y <= this.y1; ++y) {
//                    for (int z = this.z0; z <= this.z1; ++z) {
//                        int chunkX = x >> 4;
//                        int chunkY = y >> 4;
//                        int chunkZ = z >> 4;
//                        boolean loaded = false;
//                        // Check if the area is loaded
//                        if (chunkX != lastChunkX && chunkY != lastChunkY && chunkZ != lastChunkZ) {
//                            loaded = level.isAreaLoaded(x, y, z, 1);
//                            if (loaded)
//                                if (level.getChunk(x >> 4, y >> 4, z >> 4).isEmptyChunk())
//                                    loaded = false;
//
//                            lastChunkX = chunkX;
//                            lastChunkY = chunkY;
//                            lastChunkZ = chunkZ;
//                        }
//
//                        if (loaded) {
//                            int oldLevel = level.getBrightness(this.type, x, y, z);
//                            int newLevel;
//                            int tile = level.getTile(x, y, z);
//                            int tileOpacity = Tile.OPACITIES[tile];
//                            if (tileOpacity == 0)
//                                tileOpacity = 1;
//
//                            int emitingLight = 0;
//
//                            if (this.type == LightLayer.SKY) {
//                                if (level.isHighestTile(x, y, z))
//                                    emitingLight = 15;
//                            } else if (this.type == LightLayer.BLOCK) {
//                                emitingLight = Tile.LIGHT_TILES[tile];
//                            }
//
//                            // Neighboring tiles brightness
//                            int xNeg = level.getBrightness(this.type, x - 1, y, z);
//                            int xPos = level.getBrightness(this.type, x + 1, y, z);
//                            int yNeg = level.getBrightness(this.type, x, y - 1, z);
//                            int yPos = level.getBrightness(this.type, x, y + 1, z);
//                            int zNeg = level.getBrightness(this.type, x, y, z - 1);
//                            int zPos = level.getBrightness(this.type, x, y, z + 1);
//
//                            newLevel = max(xPos, xNeg);
//                            newLevel = max(yNeg, newLevel);
//                            newLevel = max(yPos, newLevel);
//                            newLevel = max(zNeg, newLevel);
//                            newLevel = max(zPos, newLevel);
//
//
//                            newLevel -= tileOpacity;
//                            if (newLevel < 0)
//                                newLevel = 0;
//
//                            if (emitingLight > newLevel) {
//                                newLevel = emitingLight;
//                            }
//
//                            if (oldLevel != newLevel) {
//                                level.setLightLevel(this.type, x, y, z, newLevel);
//                                int lightlevel = newLevel - 1;
//                                if (lightlevel < 0)
//                                    lightlevel = 0;
//
//                                level.updateLightLevel(this.type, x - 1, y, z, lightlevel);
//                                level.updateLightLevel(this.type, x, y - 1, z, lightlevel);
//                                level.updateLightLevel(this.type, x, y, z - 1, lightlevel);
//                                if (x + 1 >= this.x1) {
//                                    level.updateLightLevel(this.type, x + 1, y, z, lightlevel);
//                                }
//
//                                if (y + 1 >= this.y1) {
//                                    level.updateLightLevel(this.type, x, y + 1, z, lightlevel);
//                                }
//
//                                if (z + 1 >= this.z1) {
//                                    level.updateLightLevel(this.type, x, y, z + 1, lightlevel);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    public int max(int v0, int v1) {
        return v0 > v1 ? v0 : v1;
    }
}
