package me.alphamode.mixin;

import me.alphamode.TheEndDimension;
import net.minecraft.*;
import net.minecraft.client.LevelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RenderedChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.tile.Tile;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow private Minecraft minecraft;

    @Shadow private TextureManager textureManager;

    @Shadow private int lastViewDistance;

    @Shadow private RenderChunk[] field_591;

    @Shadow private int field_592;

    @Shadow private int field_593;

    @Shadow private int field_594;

    @Shadow private RenderChunk[] field_590;

    @Shadow private int field_559;

    @Shadow private int field_560;

    @Shadow private int field_561;

    @Shadow private int field_562;

    @Shadow private int field_563;

    @Shadow private int field_564;

    @Shadow private List<RenderChunk> field_589;

    @Shadow public List field_577;

    @Shadow private Level level;

    @Shadow private boolean field_599;

    @Shadow private int field_595;

    @Shadow private IntBuffer field_598;

    @Shadow private int field_566;

    @Shadow private List<RenderChunk> field_575;

    @Shadow private int totalChunks;

    @Shadow private int field_574;

    @Shadow private int field_571;

    @Shadow private int field_572;

    @Shadow private int renderedChunks;

    @Shadow private RenderedChunk[] field_576;

    @Shadow public abstract void method_418(int i, double d);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addMoreRegions(Minecraft textureManager, TextureManager par2, CallbackInfo ci) {
        field_576 = new RenderedChunk[]{new RenderedChunk(), new RenderedChunk(), new RenderedChunk(), new RenderedChunk(), new RenderedChunk(), new RenderedChunk(), new RenderedChunk(), new RenderedChunk()};
    }

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void renderEndSky(float par1, CallbackInfo ci) {
        if (this.minecraft.level.dimension instanceof TheEndDimension) {
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL14.glBlendFuncSeparate(
                    GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO
            );
//            Lighting.turnOff();
            GL11.glDepthMask(false);
            GL11.glBindTexture(3553, this.textureManager.getTextureId("/environment/end_sky.png"));
            Tesselator tesselator = Tesselator.instance;

            for(int i = 0; i < 6; ++i) {
                GL11.glPushMatrix();
                if (i == 1) {
                    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 2) {
                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 3) {
                    GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 4) {
                    GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
                }

                if (i == 5) {
                    GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                }

                tesselator.beginQuads();
                tesselator.method_1485(40, 40, 40, 255);
                tesselator.vertexUV(-100.0, -100.0, -100.0, 0.0, 0.0);
                tesselator.vertexUV(-100.0, -100.0, 100.0, 0.0, 16.0);
                tesselator.vertexUV(100.0, -100.0, 100.0, 16.0, 16.0);
                tesselator.vertexUV(100.0, -100.0, -100.0, 16.0, 0.0);
                tesselator.flush();
                GL11.glPopMatrix();
            }

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            ci.cancel();
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void allChanged() {
        Tile.LEAVES.setFancy(this.minecraft.config.fancyGraphics);
        this.lastViewDistance = this.minecraft.config.viewDistance;
        if (this.field_591 != null) {
            for(int var1 = 0; var1 < this.field_591.length; ++var1) {
                this.field_591[var1].method_158();
            }
        }

        int viewDistance = 64 << 3 - this.lastViewDistance;
        if (viewDistance > 400) {
            viewDistance = 400;
        }

        this.field_592 = viewDistance / 16 + 1;
        this.field_593 = viewDistance / 16 + 1;
        this.field_594 = viewDistance / 16 + 1;
        this.field_591 = new RenderChunk[this.field_592 * this.field_593 * this.field_594];
        this.field_590 = new RenderChunk[this.field_592 * this.field_593 * this.field_594];
        int var2 = 0;
        int var3 = 0;
        this.field_559 = 0;
        this.field_560 = 0;
        this.field_561 = 0;
        this.field_562 = this.field_592;
        this.field_563 = this.field_593;
        this.field_564 = this.field_594;

        for(int var4 = 0; var4 < this.field_589.size(); ++var4) {
            ((RenderChunk)this.field_589.get(var4)).field_168 = false;
        }

        this.field_589.clear();
        this.field_577.clear();

        for(int var8 = 0; var8 < this.field_592; ++var8) {
            for(int var5 = 0; var5 < this.field_593; ++var5) {
                for(int var6 = 0; var6 < this.field_594; ++var6) {
                    int index = (var6 * this.field_593 + var5) * this.field_592 + var8;
                    this.field_591[index] = new RenderChunk(
                            this.level, this.field_577, var8 * 16, var5 * 16, var6 * 16, 16, this.field_595 + var2
                    );
                    if (this.field_599) {
                        this.field_591[index].field_173 = this.field_598.get(var3);
                    }

                    this.field_591[index].field_172 = false;
                    this.field_591[index].field_171 = true;
                    this.field_591[index].field_162 = true;
                    this.field_591[index].field_170 = var3++;
                    this.field_591[index].method_161();
                    this.field_590[index] = this.field_591[index];
                    this.field_589.add(this.field_591[index]);
                    var2 += 3;
                }
            }
        }

        if (this.level != null) {
            LivingEntity var9 = this.minecraft.cameraEntity;
            if (var9 != null) {
                this.method_431(Mth.floor(var9.x), Mth.floor(var9.y), Mth.floor(var9.z));
                Arrays.sort(this.field_590, new class_768(var9));
            }
        }

        this.field_566 = 2;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void method_431(int x, int y, int z) {
        x -= 8;
        y -= 8;
        z -= 8;
        this.field_559 = Integer.MAX_VALUE;
        this.field_560 = Integer.MAX_VALUE;
        this.field_561 = Integer.MAX_VALUE;
        this.field_562 = Integer.MIN_VALUE;
        this.field_563 = Integer.MIN_VALUE;
        this.field_564 = Integer.MIN_VALUE;
        int var4 = this.field_592 * 16;
        int var5 = var4 / 2;

        for(int relX = 0; relX < this.field_592; ++relX) {
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

            for(int relZ = 0; relZ < this.field_594; ++relZ) {
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

                for(int relY = 0; relY < this.field_593; ++relY) {
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

                    RenderChunk var14 = this.field_591[(relZ * this.field_593 + relY) * this.field_592 + relX];
                    boolean var15 = var14.field_168;
                    var14.setOrigin(xPos, yPos, zPos);
                    if (!var15 && var14.field_168) {
                        this.field_589.add(var14);
                    }
                }
            }
        }

    }
}
