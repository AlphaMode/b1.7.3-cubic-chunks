package me.alphamode.world;

import net.minecraft.ChunkData;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.tile.Tile;
import net.minecraft.world.tile.TileEntityTile;
import net.minecraft.world.tile.entity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public class CubicChunk extends Chunk {
    public final int yPos;
    public boolean markedToUnload = false;
    public List<Entity> cubic$entities = new ArrayList<>();

    public CubicChunk(Level level, int x, int y, int z) {
        super(level, x, z);
        this.yPos = y;
        this.entities = null;
    }

    public CubicChunk(Level level, byte[] bs, int x, int y, int z) {
        this(level, x, y, z);
        this.tiles = bs;
        this.tileMeta = new ChunkData(bs.length);
        this.skyLight = new ChunkData(bs.length);
        this.blockLight = new ChunkData(bs.length);
    }

    @Override
    public boolean equals(int x, int z) {
        throw new RuntimeException("Use cubic equal");
    }

    public boolean equals(int x, int y, int z) {
        return x == this.xPos && y == this.yPos && z == this.zPos;
    }

    public static byte[] convert(byte[] legacyTiles, long y) {
        byte[] tiles = new byte[32768];
        for (int i = 0; i < legacyTiles.length; i++) {
            int tileY = CubicChunk.getY(i);
            if (tileY / 16 == y)
                tiles[CubicChunk.getIndex(CubicChunk.getX(i), tileY & 15, CubicChunk.getZ(i))] = legacyTiles[i];
        }
        return tiles;
    }

    public static ChunkData convert(ChunkData legacyData, long y) {
        byte[] newData = new byte[legacyData.data.length];
        for (int i = 0; i < legacyData.data.length; i++) {
            int tileY = CubicChunk.getY(i);
            if (tileY / 16 == y)
                newData[CubicChunk.getIndex(CubicChunk.getX(i), tileY & 15, CubicChunk.getZ(i))] = legacyData.data[i];
        }
        return new ChunkData(newData);
    }

    public static CubicChunk convert(Chunk legacyChunk, int y) {
        if (legacyChunk instanceof CubicChunk cubicChunk)
            return cubicChunk;
        CubicChunk chunk = new CubicChunk(legacyChunk.level, CubicChunk.convert(legacyChunk.tiles, y), legacyChunk.xPos, y, legacyChunk.zPos);
        chunk.tileMeta = CubicChunk.convert(legacyChunk.tileMeta, y);
        chunk.skyLight = CubicChunk.convert(legacyChunk.skyLight, y);
        chunk.blockLight = CubicChunk.convert(legacyChunk.blockLight, y);
        chunk.heightMap = legacyChunk.heightMap;
        chunk.field_848 = legacyChunk.field_848;
        chunk.terrainPopulated = legacyChunk.terrainPopulated;
        return chunk;
    }

    public static int getIndex(int x, int y, int z) {
        return x << 11 | z << 7 | y;//y << 8 | z << 4 | x;
    }

    public static int getX(int packedIndex) {
        return packedIndex >> 11 & 0x1F; // Extracting x by shifting right 11 bits and applying a bitmask
    }

    public static int getY(int packedIndex) {
        return packedIndex & 0x7F; // Extracting y by applying a bitmask
    }

    public static int getZ(int packedIndex) {
        return packedIndex >> 7 & 0x1F; // Extracting z by shifting right 7 bits and applying a bitmask
    }

    @Override
    public int getTile(int x, int y, int z) {
        return this.tiles[getIndex(x, y, z)] & 0xFF;
    }

    @Override
    public boolean setTile(int x, int y, int z, int l, int m) {
        byte var6 = (byte) l;
        int var7 = this.heightMap[z << 4 | x] & 255;
        int var8 = this.tiles[getIndex(x, y, z)] & 255;
        if (var8 == l && this.tileMeta.getData(x, y, z) == m) {
            return false;
        } else {
            int tileX = this.xPos * 16 + x;
            int tileY = this.xPos * 16 + y;
            int tileZ = this.zPos * 16 + z;
            this.tiles[getIndex(x, y, z)] = (byte) (var6 & 255);
            if (var8 != 0 && !this.level.isClientSide) {
                Tile.tiles[var8].method_1139(this.level, tileX, tileY, tileZ);
            }

            this.tileMeta.setData(x, y, z, m);
            if (!this.level.dimension.hasCeiling) {
                if (Tile.field_1703[var6 & 255] != 0) {
                    if ((y << 4) + yPos >= var7) {
                        this.method_648(x, y + 1, z);
                    }
                } else if ((y << 4) + yPos == var7 - 1) {
                    this.method_648(x, y, z);
                }

                this.level.method_228(LightLayer.SKY, tileX, tileY, tileZ, tileX, tileY, tileZ);
            }

            this.level.method_228(LightLayer.BLOCK, tileX, tileY, tileZ, tileX, tileY, tileZ);
            this.method_638(x, z);
            this.tileMeta.setData(x, y, z, m);
            if (l != 0) {
                Tile.tiles[l].method_1160(this.level, tileX, tileY, tileZ);
            }

            this.changed = true;
            return true;
        }
    }

    @Override
    public boolean setTile(int x, int y, int z, int tile) {
        byte tileToPlace = (byte) tile;
        int height = this.heightMap[z << 4 | x] & 255;
        int tileAt = this.tiles[getIndex(x, y, z)] & 255;
        if (tileAt == tile) {
            return false;
        } else {
            int tileX = this.xPos * 16 + x;
            int tileY = this.yPos * 16 + y;
            int tileZ = this.zPos * 16 + z;
            this.tiles[getIndex(x, y, z)] = (byte) (tileToPlace & 255);
            if (tileAt != 0) {
                Tile.tiles[tileAt].method_1139(this.level, tileX, tileY, tileZ);
            }

            this.tileMeta.setData(x, y, z, 0);
            if (Tile.field_1703[tileToPlace & 255] != 0) {
                if ((y << 4) + yPos >= height) {
                    this.method_648(x, y + 1, z);
                }
            } else if ((y << 4) + yPos == height - 1) {
                this.method_648(x, y, z);
            }

            this.level.method_228(LightLayer.SKY, tileX, tileY, tileZ, tileX, tileY, tileZ);
            this.level.method_228(LightLayer.BLOCK, tileX, tileY, tileZ, tileX, tileY, tileZ);
            this.method_638(x, z);
            if (tile != 0 && !this.level.isClientSide) {
                Tile.tiles[tile].method_1160(this.level, tileX, tileY, tileZ);
            }

            this.changed = true;
            return true;
        }
    }

    @Override
    public void method_628(Entity entity, int i) {
        this.cubic$entities.remove(entity);
    }

    @Override
    public void placeTileEntity(TileEntity tileEntity) {
        int x = tileEntity.x - this.xPos * 16;
        int y = tileEntity.y - this.yPos * 16;
        int z = tileEntity.z - this.zPos * 16;
        this.placeTileEntity(x, y, z, tileEntity);
        if (this.field_842) {
            this.level.field_3456.add(tileEntity);
        }
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        BlockPos var4 = new BlockPos(x, y, z);
        TileEntity var5 = (TileEntity)this.tileEntities.get(var4);
        if (var5 == null) {
            int var6 = this.getTile(x, y, z);
            if (!Tile.tileHasTileEntity[var6]) {
                return null;
            }

            TileEntityTile tile = (TileEntityTile)Tile.tiles[var6];
            tile.method_1160(this.level, this.xPos * 16 + x, this.yPos * 16 + y, this.zPos * 16 + z);
            var5 = (TileEntity)this.tileEntities.get(var4);
        }

        if (var5 != null && var5.method_2317()) {
            this.tileEntities.remove(var4);
            return null;
        } else {
            return var5;
        }
    }

    @Override
    public void placeTileEntity(int x, int y, int z, TileEntity tileEntity) {
        BlockPos pos = new BlockPos(x, y, z);
        tileEntity.level = this.level;
        tileEntity.x = this.xPos * 16 + x;
        tileEntity.y = this.yPos * 16 + y;
        tileEntity.z = this.zPos * 16 + z;
        if (this.getTile(x, y, z) != 0 && Tile.tiles[this.getTile(x, y, z)] instanceof TileEntityTile) {
            tileEntity.method_2319();
            this.tileEntities.put(pos, tileEntity);
        } else {
            throw new RuntimeException("Attempted to place a tile entity where there was no entity tile!");
        }
    }

    public void addEntity(Entity entity) {
        this.field_857 = true;
        int chunkX = Mth.floor(entity.x / 16.0);
        int chunkY = Mth.floor(entity.y / 16.0);
        int chunkZ = Mth.floor(entity.z / 16.0);
        if (chunkX != this.xPos || chunkY != this.yPos || chunkZ != this.zPos) {
            System.out.println("Wrong location! " + entity);
            Thread.dumpStack();
        }

        entity.field_1371 = true;
        entity.field_1372 = this.xPos;
        entity.field_1373 = this.yPos;
        entity.field_1374 = this.zPos;
        this.cubic$entities.add(entity);
    }

    public void method_643() {
        this.field_842 = true;
        this.level.method_2293(this.tileEntities.values());

        this.level.method_241(this.cubic$entities);
    }

    public void method_645() {
        this.field_842 = false;

        for (Object var2 : this.tileEntities.values()) {
            ((TileEntity) var2).method_2318();
        }

        this.level.method_270(this.cubic$entities);
    }

    public void method_629(Entity entity, AABB aABB, List list) {
        for (int var8 = 0; var8 < this.cubic$entities.size(); ++var8) {
            Entity var9 = this.cubic$entities.get(var8);
            if (var9 != entity && var9.bb.intersects(aABB)) {
                list.add(var9);
            }
        }
    }

    public void method_625(Class entityClass, AABB aABB, List list) {
        for(int var8 = 0; var8 < this.cubic$entities.size(); ++var8) {
            Entity entity = this.cubic$entities.get(var8);
            if (entityClass.isAssignableFrom(entity.getClass()) && entity.bb.intersects(aABB)) {
                list.add(entity);
            }
        }
    }

    @Override
    public int method_631(byte[] tiles, int i, int j, int k, int l, int m, int n, int o) {
        for (int var9 = i; var9 < l; ++var9) {
            for (int var10 = k; var10 < n; ++var10) {
                int index = getIndex(var9 << 11, j, var10);
                int var12 = m - j;
                System.arraycopy(tiles, o, this.tiles, index, var12);
                o += var12;
            }
        }

        this.method_632();

        for (int var13 = i; var13 < l; ++var13) {
            for (int var16 = k; var16 < n; ++var16) {
                int index = getIndex(var13, j, var16) >> 1;
                int var22 = (m - j) / 2;
                System.arraycopy(tiles, o, this.tileMeta.data, index, var22);
                o += var22;
            }
        }

        for (int var14 = i; var14 < l; ++var14) {
            for (int var17 = k; var17 < n; ++var17) {
                int var20 = getIndex(var14, j, var17) >> 1;
                int var23 = (m - j) / 2;
                System.arraycopy(tiles, o, this.blockLight.data, var20, var23);
                o += var23;
            }
        }

        for (int var15 = i; var15 < l; ++var15) {
            for (int var18 = k; var18 < n; ++var18) {
                int var21 = getIndex(var15, j, var18) >> 1;
                int var24 = (m - j) / 2;
                System.arraycopy(tiles, o, this.skyLight.data, var21, var24);
                o += var24;
            }
        }

        return o;
    }

    @Override
    public void method_637() {
        int heightLimit = 127;

        for(int var2 = 0; var2 < 16; ++var2) {
            for(int var3 = 0; var3 < 16; ++var3) {
                int var4 = 127;
                int var5 = var2 << 11 | var3 << 7;

                while(var4 > 0 && Tile.field_1703[this.tiles[var5 + var4 - 1] & 255] == 0) {
                    --var4;
                }

                this.heightMap[var3 << 4 | var2] = (byte)var4;
                if (var4 < heightLimit) {
                    heightLimit = var4;
                }

                if (!this.level.dimension.hasCeiling) {
                    int var6 = 15;
                    int var7 = 127;

                    while(true) {
                        var6 -= Tile.field_1703[this.tiles[var5 + var7] & 255];
                        if (var6 > 0) {
                            this.skyLight.setData(var2, var7, var3, var6);
                        }

                        if (--var7 <= 0 || var6 <= 0) {
                            break;
                        }
                    }
                }
            }
        }

        this.field_848 = heightLimit;

        for (int chunkX = 0; chunkX < 16; ++chunkX) {
            for (int chunkY = 0; chunkX < 16; ++chunkX) {
                for (int chunkZ = 0; chunkZ < 16; ++chunkZ) {
                    this.skyLight.setData(chunkX, chunkY, chunkZ, 15);
                    this.method_638(chunkX, chunkY, chunkZ);
                }
            }
        }

        this.changed = true;
    }

    public int method_623(LightLayer lightType, int i, int j, int k) {
        return 15;
    }

    public int method_640(int i, int j, int k, int l) {


        return 15;
    }

    private void method_638(int x, int y, int z) {
        int height = this.getYHeight(x, z);
        int relX = this.xPos * 16 + x;
        int relY = this.yPos * 16 + y;
        int relZ = this.zPos * 16 + z;
        this.method_646(relX - 1, relY, relZ, height);
        this.method_646(relX + 1, relY, relZ, height);
        this.method_646(relX, relY - 1, relZ, height);
        this.method_646(relX, relY + 1, relZ, height);
        this.method_646(relX, relY, relZ - 1, height);
        this.method_646(relX, relY, relZ + 1, height);
    }

    private void method_646(int x, int y, int z, int height) {
        int var4 = this.level.getYHeight(x, y, z);
        if (var4 > height) {
            this.level.method_228(LightLayer.SKY, x, height, z, x, var4, z);
            this.changed = true;
        } else if (var4 < height) {
            this.level.method_228(LightLayer.SKY, x, var4, z, x, height, z);
            this.changed = true;
        }
    }
}
