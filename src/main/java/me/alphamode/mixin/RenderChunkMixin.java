package me.alphamode.mixin;

import net.minecraft.*;
import net.minecraft.client.renderer.ItemEntityRenderer;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.tile.Tile;
import net.minecraft.world.tile.entity.TileEntity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashSet;
import java.util.List;

@Mixin(RenderChunk.class)
public abstract class RenderChunkMixin {
//    @Overwrite
//    public void method_152() {
//
//    }

    @Shadow public int originX;
    @Shadow public int originY;
    @Shadow public int originZ;
    @Shadow public int centerX;
    @Shadow public int centerY;
    @Shadow public int centerZ;
    @Shadow public int field_164;
    @Shadow public int field_165;
    @Shadow public int field_166;

    @Shadow public abstract void reset();

    @Shadow public int field_153;

    @Shadow public int field_154;

    @Shadow public int field_155;

    @Shadow public int field_156;

    @Shadow public int field_157;

    @Shadow public int field_158;

    @Shadow public AABB bb;

    @Shadow private int field_144;

    @Shadow public abstract void method_161();

    @Shadow public boolean field_168;

    @Shadow public static int field_149;

    @Shadow public boolean[] field_163;

    @Shadow public List<TileEntity> renderableBlockEntities;

    @Shadow public Level level;

    @Shadow private static Tesselator tesselator;

    @Shadow private List<TileEntity> field_147;

    @Shadow public boolean field_142;

    @Shadow private boolean field_146;

    @Shadow protected abstract void translate();

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void setOrigin(int x, int y, int z) {
        if (x != this.originX || y != this.originY || z != this.originZ) {
            this.reset();
            this.originX = x;
            this.originY = y;
            this.originZ = z;
            this.field_164 = x + this.field_153 / 2;
            this.field_165 = y + this.field_154 / 2;
            this.field_166 = z + this.field_155 / 2;
            this.centerX = x & 1023;
            this.centerY = y & 1023;
            this.centerZ = z & 1023;
            this.field_156 = x - this.centerX;
            this.field_157 = y - this.centerY;
            this.field_158 = z - this.centerZ;
            float var4 = 6.0F;
            this.bb = AABB.create(
                    (double)((float)x - var4),
                    (double)((float)y - var4),
                    (double)((float)z - var4),
                    (double)((float)(x + this.field_153) + var4),
                    (double)((float)(y + this.field_154) + var4),
                    (double)((float)(z + this.field_155) + var4)
            );
            GL11.glNewList(this.field_144 + 2, 4864);
            ItemEntityRenderer.renderAABB(
                    AABB.newTemp(
                            (double)((float)this.centerX - var4),
                            (double)((float)this.centerY - var4),
                            (double)((float)this.centerZ - var4),
                            (double)((float)(this.centerX + this.field_153) + var4),
                            (double)((float)(this.centerY + this.field_154) + var4),
                            (double)((float)(this.centerZ + this.field_155) + var4)
                    )
            );
            GL11.glEndList();
            this.method_161();
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void compile() {
        if (this.field_168) {
            ++field_149;
            int minX = this.originX;
            int minY = this.originY;
            int minZ = this.originZ;
            int maxX = this.originX + this.field_153;
            int maxY = this.originY + this.field_154;
            int maxZ = this.originZ + this.field_155;

            for(int renderLayer = 0; renderLayer < 2; ++renderLayer) {
                this.field_163[renderLayer] = true;
            }

            Chunk.field_840 = false;
            HashSet<TileEntity> var21 = new HashSet<>(this.renderableBlockEntities);
            this.renderableBlockEntities.clear();
            byte offset = 1;
            RenderChunkRegion area = new RenderChunkRegion(this.level, minX - offset, minY - offset, minZ - offset, maxX + offset, maxY + offset, maxZ + offset);
            BlockRenderer blockRenderer = new BlockRenderer(area);

            for(int renderLayer = 0; renderLayer < 2; ++renderLayer) {
                boolean var12 = false;
                boolean shouldRender = false;
                boolean rendered = false;

                for(int y = minY; y < maxY; ++y) {
                    for(int z = minZ; z < maxZ; ++z) {
                        for(int x = minX; x < maxX; ++x) {
                            int tile = area.getTile(x, y, z);
                            if (tile > 0) {
                                if (!rendered) {
                                    rendered = true;
                                    GL11.glNewList(this.field_144 + renderLayer, 4864);
                                    GL11.glPushMatrix();
                                    this.translate();
                                    float var19 = 1.000001F;
                                    GL11.glTranslatef((float)(-this.field_155) / 2.0F, (float)(-this.field_154) / 2.0F, (float)(-this.field_155) / 2.0F);
                                    GL11.glScalef(var19, var19, var19);
                                    GL11.glTranslatef((float)this.field_155 / 2.0F, (float)this.field_154 / 2.0F, (float)this.field_155 / 2.0F);
                                    tesselator.beginQuads();
                                    tesselator.method_747((double)(-this.originX), (double)(-this.originY), (double)(-this.originZ));
                                }

                                if (renderLayer == 0 && Tile.tileHasTileEntity[tile]) {
                                    TileEntity var23 = area.getTileEntity(x, y, z);
                                    if (TileEntityRendererManager.INSTANCE.hasTileEntityRenderer(var23)) {
                                        this.renderableBlockEntities.add(var23);
                                    }
                                }

                                Tile var24 = Tile.tiles[tile];
                                int tileRenderLayer = var24.getRenderLayer();
                                if (tileRenderLayer != renderLayer) {
                                    var12 = true;
                                } else if (tileRenderLayer == renderLayer) {
                                    shouldRender |= blockRenderer.renderBatched(var24, x, y, z);
                                }
                            }
                        }
                    }
                }

                if (rendered) {
                    tesselator.flush();
                    GL11.glPopMatrix();
                    GL11.glEndList();
                    tesselator.method_747(0.0, 0.0, 0.0);
                } else {
                    shouldRender = false;
                }

                if (shouldRender) {
                    this.field_163[renderLayer] = false;
                }

                if (!var12) {
                    break;
                }
            }

            HashSet<TileEntity> var22 = new HashSet<>(this.renderableBlockEntities);
            var22.removeAll(var21);
            this.field_147.addAll(var22);
            this.renderableBlockEntities.forEach(var21::remove);
            this.field_147.removeAll(var21);
            this.field_142 = Chunk.field_840;
            this.field_146 = true;
        }
    }
}
