package me.alphamode.mixin;

import net.minecraft.Vec3;
import net.minecraft.class_217;
import net.minecraft.client.DirtyChunkSorter;
import net.minecraft.client.LevelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RenderList;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.Level;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.tile.Tile;
import net.minecraft.world.tile.entity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow private Minecraft minecraft;

    @Shadow private TextureManager textureManager;

    @Shadow private int lastViewDistance;

    @Shadow private RenderChunk[] chunks;

    @Shadow private int chunkGridSizeX;

    @Shadow private int chunkGridSizeY;

    @Shadow private int chunkGridSizeZ;

    @Shadow private RenderChunk[] field_590;

    @Shadow private int field_559;

    @Shadow private int field_560;

    @Shadow private int field_561;

    @Shadow private int field_562;

    @Shadow private int field_563;

    @Shadow private int field_564;

    @Shadow private List<RenderChunk> field_589;

    @Shadow public List<TileEntity> globalBlockEntities;

    @Shadow private Level level;

    @Shadow private boolean field_599;

    @Shadow private int field_595;

    @Shadow private IntBuffer field_598;

    @Shadow private int noEntityRenderFrames;

    @Shadow private List<RenderChunk> field_575;

    @Shadow private int totalChunks;

    @Shadow private int field_574;

    @Shadow private int field_571;

    @Shadow private int field_572;

    @Shadow private int renderedChunks;

    @Shadow private RenderList[] renderLists;

    @Shadow public abstract void method_418(int i, double d);

    @Shadow private int field_600;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addMoreRegions(Minecraft textureManager, TextureManager par2, CallbackInfo ci) {
        renderLists = new RenderList[]{new RenderList(), new RenderList(), new RenderList(), new RenderList(), new RenderList(), new RenderList(), new RenderList(), new RenderList()};
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void allChanged() {
        Tile.LEAVES.setFancy(this.minecraft.options.fancyGraphics);
        this.lastViewDistance = this.minecraft.options.viewDistance;
        if (this.chunks != null) {
            for(int var1 = 0; var1 < this.chunks.length; ++var1) {
                this.chunks[var1].method_158();
            }
        }

        int viewDistance = 64 << 3 - this.lastViewDistance;
        if (viewDistance > 400) {
            viewDistance = 400;
        }

        this.chunkGridSizeX = viewDistance / 16 + 1;
        this.chunkGridSizeY = viewDistance / 16 + 1;
        this.chunkGridSizeZ = viewDistance / 16 + 1;
        this.chunks = new RenderChunk[this.chunkGridSizeX * this.chunkGridSizeY * this.chunkGridSizeZ];
        this.field_590 = new RenderChunk[this.chunkGridSizeX * this.chunkGridSizeY * this.chunkGridSizeZ];
        int var2 = 0;
        int var3 = 0;
        this.field_559 = 0;
        this.field_560 = 0;
        this.field_561 = 0;
        this.field_562 = this.chunkGridSizeX;
        this.field_563 = this.chunkGridSizeY;
        this.field_564 = this.chunkGridSizeZ;

        for(int var4 = 0; var4 < this.field_589.size(); ++var4) {
            this.field_589.get(var4).field_168 = false;
        }

        this.field_589.clear();
        this.globalBlockEntities.clear();

        for(int var8 = 0; var8 < this.chunkGridSizeX; ++var8) {
            for(int var5 = 0; var5 < this.chunkGridSizeY; ++var5) {
                for(int var6 = 0; var6 < this.chunkGridSizeZ; ++var6) {
                    int index = (var6 * this.chunkGridSizeY + var5) * this.chunkGridSizeX + var8;
                    this.chunks[index] = new RenderChunk(
                            this.level, this.globalBlockEntities, var8 * 16, var5 * 16, var6 * 16, 16, this.field_595 + var2
                    );
                    if (this.field_599) {
                        this.chunks[index].field_173 = this.field_598.get(var3);
                    }

                    this.chunks[index].field_172 = false;
                    this.chunks[index].field_171 = true;
                    this.chunks[index].visible = true;
                    this.chunks[index].field_170 = var3++;
                    this.chunks[index].method_161();
                    this.field_590[index] = this.chunks[index];
                    if (chunks[index] == null)
                        throw new RuntimeException("OEE");
                    this.field_589.add(this.chunks[index]);
                    var2 += 3;
                }
            }
        }

        if (this.level != null) {
            LivingEntity var9 = this.minecraft.cameraEntity;
            if (var9 != null) {
                this.repositionCamera(Mth.floor(var9.x), Mth.floor(var9.y), Mth.floor(var9.z));
                Arrays.sort(this.field_590, new DirtyChunkSorter(var9));
            }
        }

        this.noEntityRenderFrames = 2;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void repositionCamera(int x, int y, int z) {
        x -= 8;
        y -= 8;
        z -= 8;
        this.field_559 = Integer.MAX_VALUE;
        this.field_560 = Integer.MAX_VALUE;
        this.field_561 = Integer.MAX_VALUE;
        this.field_562 = Integer.MIN_VALUE;
        this.field_563 = Integer.MIN_VALUE;
        this.field_564 = Integer.MIN_VALUE;
        int var4 = this.chunkGridSizeX * 16;
        int var5 = var4 / 2;

        for(int relX = 0; relX < this.chunkGridSizeX; ++relX) {
            int xPos = relX * 16;
            int xOff = xPos + var5 - x;
            if (xOff < 0) {
                xOff -= var4 - 1;
            }

            xOff /= var4;
            xPos -= xOff * var4;
            if (xPos < this.field_559) {
                this.field_559 = xPos;
            }

            if (xPos > this.field_562) {
                this.field_562 = xPos;
            }

            for(int relZ = 0; relZ < this.chunkGridSizeZ; ++relZ) {
                int zPos = relZ * 16;
                int zOff = zPos + var5 - z;
                if (zOff < 0) {
                    zOff -= var4 - 1;
                }

                zOff /= var4;
                zPos -= zOff * var4;
                if (zPos < this.field_561) {
                    this.field_561 = zPos;
                }

                if (zPos > this.field_564) {
                    this.field_564 = zPos;
                }

                for(int relY = 0; relY < this.chunkGridSizeY; ++relY) {
                    int yPos = relY * 16;
                    int yOff = yPos + var5 - y;
                    if (yOff < 0) {
                        yOff -= var4 - 1;
                    }

                    yOff /= var4;
                    yPos -= yOff * var4;
                    if (yPos < this.field_560) {
                        this.field_560 = yPos;
                    }

                    if (yPos > this.field_563) {
                        this.field_563 = yPos;
                    }

                    RenderChunk var14 = this.chunks[(relZ * this.chunkGridSizeY + relY) * this.chunkGridSizeX + relX];
                    boolean var15 = var14.field_168;
                    var14.setOrigin(xPos, yPos, zPos);
                    if (!var15 && var14.field_168) {
                        if (var14 == null)
                            throw new RuntimeException("OH no");
                        this.field_589.add(var14);
                    }
                }
            }
        }

    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;hasNormalSkyColor()Z"))
    private boolean checkDimension(Dimension instance) {
        return this.level.getLevelSource().getDimension(Mth.floor(this.minecraft.cameraEntity.y) >> 4).hasNormalSkyColor();
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;getSunriseColor(FF)[F"))
    private float[] getDimensionSunrise(Dimension instance, float g, float partialTick) {
        return this.level.getLevelSource().getDimension(Mth.floor(this.minecraft.cameraEntity.y) >> 4).getSunriseColor(g, partialTick);
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Level;getTimeOfDay(F)F"))
    private float getDimensionTime(Level instance, float partialTick) {
        return this.level.getTimeOfDay(partialTick, Mth.floor(this.minecraft.cameraEntity.y) >> 4);
    }

    @Redirect(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Level;getCloudColor(F)Lnet/minecraft/Vec3;"))
    private Vec3 getDimensionCloudColor(Level instance, float partialTick) {
        return instance.getCloudColor(partialTick, Mth.floor(this.minecraft.cameraEntity.y) >> 4);
    }

    @Redirect(method = "buildClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Level;getCloudColor(F)Lnet/minecraft/Vec3;"))
    private Vec3 getDimensionBuildCloudColor(Level instance, float partialTick) {
        return instance.getCloudColor(partialTick, Mth.floor(this.minecraft.cameraEntity.y) >> 4);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean method_1537(LivingEntity livingEntity, boolean bl) {
        boolean var3 = false;
        if (var3) {
            Collections.sort(this.field_589, new class_217(livingEntity));
            int var17 = this.field_589.size() - 1;
            int var18 = this.field_589.size();

            for(int var19 = 0; var19 < var18; ++var19) {
                RenderChunk var20 = (RenderChunk)this.field_589.get(var17 - var19);
                if (!bl) {
                    if (var20.method_155(livingEntity) > 256.0F) {
                        if (var20.visible) {
                            if (var19 >= 3) {
                                return false;
                            }
                        } else if (var19 >= 1) {
                            return false;
                        }
                    }
                } else if (!var20.visible) {
                    continue;
                }

                var20.compile();
                this.field_589.remove(var20);
                var20.field_168 = false;
            }

            return this.field_589.size() == 0;
        } else {
            byte var4 = 2;
            class_217 var5 = new class_217(livingEntity);
            RenderChunk[] var6 = new RenderChunk[var4];
            ArrayList var7 = null;
            int var8 = this.field_589.size();
            int var9 = 0;

            for(int var10 = 0; var10 < var8; ++var10) {
                RenderChunk var11 = (RenderChunk)this.field_589.get(var10);
                if (!bl) {
                    if (var11 == null)
                        continue; // temp
                    if (var11.method_155(livingEntity) > 256.0F) {
                        int var12 = 0;

                        while(var12 < var4 && (var6[var12] == null || var5.compare(var6[var12], var11) <= 0)) {
                            ++var12;
                        }

                        if (--var12 <= 0) {
                            continue;
                        }

                        int var13 = var12;

                        while(--var13 != 0) {
                            var6[var13 - 1] = var6[var13];
                        }

                        var6[var12] = var11;
                        continue;
                    }
                } else if (!var11.visible) {
                    continue;
                }

                if (var7 == null) {
                    var7 = new ArrayList();
                }

                ++var9;
                var7.add(var11);
                this.field_589.set(var10, null);
            }

            if (var7 != null) {
                if (var7.size() > 1) {
                    Collections.sort(var7, var5);
                }

                for(int var21 = var7.size() - 1; var21 >= 0; --var21) {
                    RenderChunk var23 = (RenderChunk)var7.get(var21);
                    var23.compile();
                    var23.field_168 = false;
                }
            }

            int var22 = 0;

            for(int var24 = var4 - 1; var24 >= 0; --var24) {
                RenderChunk var27 = var6[var24];
                if (var27 != null) {
                    if (!var27.visible && var24 != var4 - 1) {
                        var6[var24] = null;
                        var6[0] = null;
                        break;
                    }

                    var6[var24].compile();
                    var6[var24].field_168 = false;
                    ++var22;
                }
            }

            int var25 = 0;
            int var28 = 0;

            for(int var29 = this.field_589.size(); var25 != var29; ++var25) {
                RenderChunk var14 = (RenderChunk)this.field_589.get(var25);
                if (var14 != null) {
                    boolean var15 = false;

                    for(int var16 = 0; var16 < var4 && !var15; ++var16) {
                        if (var14 == var6[var16]) {
                            var15 = true;
                        }
                    }

                    if (!var15) {
                        if (var28 != var25) {
                            if (var14 == null)
                                throw new RuntimeException("EEE");
                            this.field_589.set(var28, var14);
                        }

                        ++var28;
                    }
                }
            }

            while(--var25 >= var28) {
                this.field_589.remove(var25);
            }

            return var8 == var9 + var22;
        }
    }
}
