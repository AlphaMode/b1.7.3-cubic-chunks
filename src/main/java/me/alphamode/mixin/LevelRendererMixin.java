package me.alphamode.mixin;

import net.minecraft.Vec3;
import net.minecraft.class_768;
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
import java.util.Arrays;
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
                    this.field_589.add(this.chunks[index]);
                    var2 += 3;
                }
            }
        }

        if (this.level != null) {
            LivingEntity var9 = this.minecraft.cameraEntity;
            if (var9 != null) {
                this.repositionCamera(Mth.floor(var9.x), Mth.floor(var9.y), Mth.floor(var9.z));
                Arrays.sort(this.field_590, new class_768(var9));
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
}
