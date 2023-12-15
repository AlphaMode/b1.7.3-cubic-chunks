package me.alphamode.mixin;

import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.tile.Tile;
import net.minecraft.world.tile.entity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderChunkRegion.class)
public class RenderChunkRegionMixin {
    @Shadow private int centerX;
    @Shadow private int centerZ;
    @Shadow private Level level;
    @Shadow private Chunk[][] chunks;
    private Chunk[][][] cubic$chunks;
    private int centerY;
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Level;getChunk(II)Lnet/minecraft/world/Chunk;"))
    private Chunk preventChunk(Level instance, int j, int i) {
        return null;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cubicInit(Level level, int x0, int y0, int z0, int x1, int y1, int z1, CallbackInfo ci) {
        this.chunks = null;
        this.centerY = y0 >> 4;
        int chunkX = x1 >> 4;
        int chunkY = y1 >> 4;
        int chunkZ = z1 >> 4;
        this.cubic$chunks = new Chunk[chunkX - this.centerX + 1][chunkY - this.centerY + 1][chunkZ - this.centerZ + 1];

        for(int xRel = this.centerX; xRel <= chunkX; ++xRel) {
            for(int yRel = this.centerY; yRel <= chunkY; ++yRel) {
                for (int zRel = this.centerZ; zRel <= chunkZ; ++zRel) {
                    this.cubic$chunks[xRel - this.centerX][yRel - this.centerY][zRel - this.centerZ] = level.getChunk(xRel, yRel, zRel);
                }
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getTile(int x, int y, int z) {
        int xIndex = (x >> 4) - this.centerX;
        int yIndex = (y >> 4) - this.centerY;
        int zIndex = (z >> 4) - this.centerZ;
        if (xIndex >= 0 && xIndex < this.cubic$chunks.length && yIndex >= 0 && yIndex < this.cubic$chunks[xIndex].length && zIndex >= 0 && zIndex < this.cubic$chunks[xIndex][yIndex].length) {
            Chunk chunk = this.cubic$chunks[xIndex][yIndex][zIndex];
            return chunk == null ? 0 : chunk.getTile(x & 15, y & 15, z & 15);
        } else {
            return 0;
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public TileEntity getTileEntity(int x, int y, int z) {
        int xIndex = (x >> 4) - this.centerX;
        int yIndex = (y >> 4) - this.centerY;
        int zIndex = (z >> 4) - this.centerZ;
        return this.cubic$chunks[xIndex][yIndex][zIndex].getTileEntity(x & 15, y & 15, z & 15);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int method_207(int i, int j, int k, boolean bl) {
        if (i >= -32000000 && k >= -32000000 && i < 32000000 && k <= 32000000) {
            if (bl) {
                int var5 = this.getTile(i, j, k);
                if (var5 == Tile.SLAB.id || var5 == Tile.FARMLAND.id || var5 == Tile.WOOD_STAIRS.id || var5 == Tile.COBBLESTONE_STAIRS.id) {
                    int var13 = this.method_207(i, j + 1, k, false);
                    int var7 = this.method_207(i + 1, j, k, false);
                    int var8 = this.method_207(i - 1, j, k, false);
                    int var9 = this.method_207(i, j, k + 1, false);
                    int var10 = this.method_207(i, j, k - 1, false);
                    if (var7 > var13) {
                        var13 = var7;
                    }

                    if (var8 > var13) {
                        var13 = var8;
                    }

                    if (var9 > var13) {
                        var13 = var9;
                    }

                    if (var10 > var13) {
                        var13 = var10;
                    }

                    return var13;
                }
            }

            int var11 = (i >> 4) - this.centerX;
            int yIndex = (j >> 4) - this.centerY;
            int var6 = (k >> 4) - this.centerZ;
            return this.cubic$chunks[var11][yIndex][var6].method_640(i & 15, j & 15, k & 15, this.level.field_291);
        } else {
            return 15;
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getTileMeta(int x, int y, int z) {
        int xIndex = (x >> 4) - this.centerX;
        int yIndex = (y >> 4) - this.centerY;
        int zIndex = (z >> 4) - this.centerZ;
        return this.cubic$chunks[xIndex][yIndex][zIndex].getMeta(x & 15, y & 15, z & 15);
    }
}
