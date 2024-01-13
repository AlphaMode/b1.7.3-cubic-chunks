package me.alphamode.world.chunk;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import me.alphamode.world.ServerHeightMap;
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
import java.util.Map;

public class CubicChunk extends Chunk {
    public static final int MIN_SUPPORTED_BLOCK_Y = Integer.MIN_VALUE + 4096;
    public static final int MAX_SUPPORTED_BLOCK_Y = Integer.MAX_VALUE - 4095;
    public static final int SIZE = 16;
    public static final int LEGACY_CHUNK_SIZE = 32768;
    public static final int CHUNK_SIZE = LEGACY_CHUNK_SIZE;//4096;
    public final int yPos;
    public final long packedPos;
    public boolean markedToUnload = false;
    public List<Entity> cubic$entities = new ArrayList<>();
//    public final ServerHeightMap heightMap = new ServerHeightMap();

    public CubicChunk(Level level, int x, int y, int z) {
        super(level, x, z);
        this.yPos = y;
        this.entities = null;
        this.packedPos = SectionPos.toLong(x, z);
        if (!SectionTracker.CHUNKS.containsKey(packedPos))
            SectionTracker.CHUNKS.put(packedPos, new SectionTracker.SectionData(new ServerHeightMap(this.heightMap), new Int2ObjectLinkedOpenHashMap<>(Map.of(y, this))));
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
        byte[] tiles = new byte[CHUNK_SIZE];
        for (int i = 0; i < legacyTiles.length; i++) {
            int tileY = CubicChunk.getLegacyY(i);
            if (tileY / 16 == y)
                tiles[CubicChunk.getIndex(CubicChunk.getLegacyX(i), tileY & 15, CubicChunk.getLegacyZ(i))] = legacyTiles[i] == Tile.BEDROCK.id ? 1 : legacyTiles[i];
        }
        return tiles;
    }

    public static ChunkData convert(ChunkData legacyData, long y) {
        byte[] newData = new byte[CubicChunk.CHUNK_SIZE];
//        for (int localX = 0; localX < SIZE; localX++) {
//            for (int localY = 0; localY < SIZE; localY++) {
//                for (int localZ = 0; localZ < SIZE; localZ++) {
//                    newData[CubicChunk.getIndex(localX, localY, localZ)] = legacyData.data[CubicChunk.getIndex(localX, (int) (y * SIZE + localY), localZ)];
//                }
//            }
//        }
        for (int i = 0; i < legacyData.data.length; i++) {
            int tileY = CubicChunk.getLegacyY(i);
            if (tileY / 16 == y)
                newData[CubicChunk.getIndex(CubicChunk.getLegacyX(i), tileY & 15, CubicChunk.getLegacyZ(i))] = legacyData.data[i];
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

    public static CubicChunk convertLocal(Chunk legacyChunk, int y) {
        if (legacyChunk instanceof CubicChunk cubicChunk)
            return cubicChunk;
        CubicChunk chunk = new CubicChunk(legacyChunk.level, CubicChunk.convert(legacyChunk.tiles, y & 7), legacyChunk.xPos, y, legacyChunk.zPos);
        chunk.tileMeta = CubicChunk.convert(legacyChunk.tileMeta, y & 7);
        chunk.skyLight = CubicChunk.convert(legacyChunk.skyLight, y & 7);
        chunk.blockLight = CubicChunk.convert(legacyChunk.blockLight, y & 7);
        chunk.heightMap = legacyChunk.heightMap;
        chunk.field_848 = legacyChunk.field_848;
        chunk.terrainPopulated = legacyChunk.terrainPopulated;
        chunk.method_637();
        return chunk;
    }

    public static int getIndex(int x, int y, int z) {
        return x << 11 | z << 7 | y;// y << 8 | z << 4 | x;
    }

    public static int getHeightmapIndex(int x, int z) {
        return z << 4 | x;
    }

    public static int getX(int packedIndex) {
        return packedIndex & 0xF; // Extracting x by applying a bitmask
    }

    public static int getY(int packedIndex) {
        return (packedIndex >> 8) & 0xF; // Extracting y by shifting right 8 bits and applying a bitmask
    }

    public static int getZ(int packedIndex) {
        return (packedIndex >> 4) & 0xF; // Extracting z by shifting right 4 bits and applying a bitmask
    }

    public static int getLegacyX(int packedIndex) {
        return packedIndex >> 11 & 0x1F; // Extracting x by shifting right 11 bits and applying a bitmask
    }

    public static int getLegacyY(int packedIndex) {
        return packedIndex & 0x7F; // Extracting y by applying a bitmask
    }

    public static int getLegacyZ(int packedIndex) {
        return packedIndex >> 7 & 0x1F; // Extracting z by shifting right 7 bits and applying a bitmask
    }

    @Override
    public int getTile(int x, int y, int z) {
        return this.tiles[getIndex(x, y, z)] & 0xFF;
    }

    @Override
    public int getYHeight(int x, int z) {
        return SectionTracker.getHeightmap(this.packedPos).getTopBlockY(x, z);
    }

    @Override
    public boolean isHighestTile(int x, int y, int z) {
        return y >= (SectionTracker.getHeightmap(this.packedPos).getTopBlockY(x, z));
    }

    @Override
    public boolean setTile(int x, int y, int z, int tile, int meta) {
        byte tileToPlace = (byte) tile;
        int height = SectionTracker.getHeightmap(this.packedPos).getTopBlockY(x, z);
        int var8 = this.tiles[getIndex(x, y, z)] & 255;
        if (var8 == tile && this.tileMeta.getData(x, y, z) == meta) {
            return false;
        } else {
            int tileX = this.xPos * 16 + x;
            int tileY = this.yPos * 16 + y;
            int tileZ = this.zPos * 16 + z;
            this.tiles[getIndex(x, y, z)] = (byte) (tileToPlace & 255);
            if (var8 != 0 && !this.level.isClientSide) {
                Tile.tiles[var8].onRemove(this.level, tileX, tileY, tileZ);
            }

            SectionTracker.getHeightmap(this.packedPos).onOpacityChange(x, tileY, z, Tile.OPACITIES[tileToPlace & 255]);
            this.tileMeta.setData(x, y, z, meta);
            if (!this.level.getLevelSource().getDimension(yPos).hasCeiling) {

                if (Tile.OPACITIES[tileToPlace & 255] != 0) {
                    if (tileY >= height) {
                        this.updateSkyLight(x, tileY + 1, z);
                    }
                } else if (tileY == height - 1) {
                    this.updateSkyLight(x, tileY, z);
                }

                this.level.enqueueLightUpdate(LightLayer.SKY, tileX, tileY, tileZ, tileX, tileY, tileZ);
            }

            this.level.enqueueLightUpdate(LightLayer.BLOCK, tileX, tileY, tileZ, tileX, tileY, tileZ);
            this.method_638(x, z);
            this.tileMeta.setData(x, y, z, meta);
            if (tile != 0) {
                Tile.tiles[tile].onPlace(this.level, tileX, tileY, tileZ);
            }

            this.changed = true;
            return true;
        }
    }

    @Override
    public boolean setTile(int x, int y, int z, int tile) {
        byte tileToPlace = (byte) tile;
        int height = SectionTracker.getHeightmap(this.packedPos).getTopBlockY(x, z);
        int tileAt = this.tiles[getIndex(x, y, z)] & 255;
        if (tileAt == tile) {
            return false;
        } else {
            int tileX = this.xPos * 16 + x;
            int tileY = this.yPos * 16 + y;
            int tileZ = this.zPos * 16 + z;
            this.tiles[getIndex(x, y, z)] = (byte) (tileToPlace & 255);
            if (tileAt != 0) {
                Tile.tiles[tileAt].onRemove(this.level, tileX, tileY, tileZ);
            }

            SectionTracker.getHeightmap(this.packedPos).onOpacityChange(x, tileY, z, Tile.OPACITIES[tileToPlace & 255]);
            this.tileMeta.setData(x, y, z, 0);
            if (Tile.OPACITIES[tileToPlace & 255] != 0) { // Handled by SectionTracker heightmap
                if (tileY >= height) {
                    this.updateSkyLight(x, tileY + 1, z);
                }
            } else if (tileY == height - 1) {
                this.updateSkyLight(x, tileY, z);
            }

            this.level.enqueueLightUpdate(LightLayer.SKY, tileX, tileY, tileZ, tileX, tileY, tileZ);
            this.level.enqueueLightUpdate(LightLayer.BLOCK, tileX, tileY, tileZ, tileX, tileY, tileZ);
            this.method_638(x, z);
            if (tile != 0 && !this.level.isClientSide) {
                Tile.tiles[tile].onPlace(this.level, tileX, tileY, tileZ);
            }

            this.changed = true;
            return true;
        }
    }

    public void updateSkyLight(int x, int y, int z) {
        int heightmapValue = SectionTracker.getHeightmap(this.packedPos).getTopBlockY(x, z);//this.heightMap[getHeightmapIndex(x, z)] & 255;
        int var5 = heightmapValue;
        if (y > heightmapValue) {
            var5 = y;
        }

        while (var5 > 0 && Tile.OPACITIES[this.tiles[getIndex(x, (var5 - 1) & 15, z)] & 255] == 0) {
            --var5;
        }

        if (var5 != heightmapValue) {
            this.level.method_293(x, z, var5, heightmapValue);
            SectionTracker.getHeightmap(this.packedPos).onOpacityChange(x, y, z, Tile.OPACITIES[this.tiles[getIndex(x, (var5 - 1) & 15, z)] & 255]);
//            this.heightMap[getHeightmapIndex(x, z)] = (byte) var5;
            if (var5 < this.field_848) {
                this.field_848 = var5;
            } else {
                int height = 15;

                for (int chunkX = 0; chunkX < 16; ++chunkX) {
                    for (int chunkZ = 0; chunkZ < 16; ++chunkZ) {
                        int calculatedHeight = SectionTracker.getHeightmap(packedPos).getTopBlockY(chunkX, chunkZ) & 15;
                        if (calculatedHeight < height) {
                            height = calculatedHeight;
                        }
                    }
                }

                this.field_848 = height;
            }

            int tileX = this.xPos * 16 + x;
            int tileZ = this.zPos * 16 + z;
            if (var5 < heightmapValue) {
                for (int var9 = var5; var9 < heightmapValue; ++var9) {
                    this.skyLight.setData(x, var9, z, 15);
                }
            } else {
                this.level.enqueueLightUpdate(LightLayer.SKY, tileX, this.yPos * 16 + heightmapValue, tileZ, tileX, this.yPos * 16 + var5, tileZ);

                for (int var9 = heightmapValue; var9 < var5; ++var9) {
                    this.skyLight.setData(x, var9, z, 0);
                }
            }

            int var9 = 15;

            int var10;
            for (var10 = var5; var5 > 0 && var9 > 0; this.skyLight.setData(x, var5, z, var9)) {
                --var5;
                int var11 = Tile.OPACITIES[this.getTile(x, var5, z)];
                if (var11 == 0) {
                    var11 = 1;
                }

                var9 -= var11;
                if (var9 < 0) {
                    var9 = 0;
                }
            }

            while (var5 > 0 && Tile.OPACITIES[this.getTile(x, var5 - 1, z)] == 0) {
                --var5;
            }

            if (var5 != var10) {
                this.level.enqueueLightUpdate(LightLayer.SKY, tileX - 1, var5, tileZ - 1, tileX + 1, var10, tileZ + 1);
            }

            this.changed = true;
        }
    }

    @Override
    public void removeEntity(Entity entity, int i) {
        this.cubic$entities.remove(entity);
    }

    @Override
    public void placeTileEntity(TileEntity tileEntity) {
        int x = tileEntity.x - this.xPos * 16;
        int y = tileEntity.y - this.yPos * 16;
        int z = tileEntity.z - this.zPos * 16;
        this.placeTileEntity(x, y, z, tileEntity);
        if (this.loaded) {
            this.level.field_3456.add(tileEntity);
        }
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        BlockPos var4 = new BlockPos(x, y, z);
        TileEntity var5 = (TileEntity) this.tileEntities.get(var4);
        if (var5 == null) {
            int var6 = this.getTile(x, y, z);
            if (!Tile.tileHasTileEntity[var6]) {
                return null;
            }

            TileEntityTile tile = (TileEntityTile) Tile.tiles[var6];
            tile.onPlace(this.level, this.xPos * 16 + x, this.yPos * 16 + y, this.zPos * 16 + z);
            var5 = (TileEntity) this.tileEntities.get(var4);
        }

        if (var5 != null && var5.isRemoved()) {
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
            tileEntity.clearRemoved();
            this.tileEntities.put(pos, tileEntity);
        } else {
            throw new RuntimeException("Attempted to place a tile entity where there was no entity tile!");
        }
    }

    public void addEntity(Entity entity) {
        this.lastSaveHadEntities = true;
        int chunkX = Mth.floor(entity.x / 16.0);
        int chunkY = Mth.floor(entity.y / 16.0);
        int chunkZ = Mth.floor(entity.z / 16.0);
        if (chunkX != this.xPos || chunkY != this.yPos || chunkZ != this.zPos) {
            System.out.println("Wrong location! Suppose to be in (" + this.xPos + ", " + this.yPos + ", " + this.zPos + ") instead location is: (" + chunkX + ", " + chunkY + ", " + chunkZ + ") " + entity);
            Thread.dumpStack();
        }

        entity.inChunk = true;
        entity.xChunk = this.xPos;
        entity.yChunk = this.yPos;
        entity.zChunk = this.zPos;
        this.cubic$entities.add(entity);
    }

    public void load() {
        this.loaded = true;
        this.level.method_2293(this.tileEntities.values());

        this.level.method_241(this.cubic$entities);
    }

    public void unload() {
        this.loaded = false;

        for (Object var2 : this.tileEntities.values()) {
            ((TileEntity) var2).setRemoved();
        }

        this.level.method_270(this.cubic$entities);
    }

    public void getEntities(Entity entity, AABB aABB, List list) {
        for (int var8 = 0; var8 < this.cubic$entities.size(); ++var8) {
            Entity var9 = this.cubic$entities.get(var8);
            if (var9 != entity && var9.bb.intersects(aABB)) {
                list.add(var9);
            }
        }
    }

    public void getEntitiesOfClass(Class entityClass, AABB aABB, List list) {
        for (int var8 = 0; var8 < this.cubic$entities.size(); ++var8) {
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
                int index = getIndex(var9, j, var10);
                int var12 = m - j;
                System.arraycopy(tiles, o, this.tiles, index, var12);
                o += var12;
            }
        }

        this.primeHeightmap();

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
        int var1 = 15;


        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int y = 15;
                int index = getIndex(x, y, z);

                while (y > 0 && Tile.OPACITIES[this.tiles[index] & 255] == 0) {
                    --y;
                    index = getIndex(x, y, z);
                }

                int tileY = this.yPos * 16 + y;
                SectionTracker.getHeightmap(this.packedPos).onOpacityChange(x, tileY, z, Tile.OPACITIES[this.tiles[index] & 255]);
                if (y < var1) {
                    var1 = y;
                }

                if (!this.level.dimension.hasCeiling) {
                    int level = 15;
                    int var7 = 15;

                    while (true) {
                        level -= Tile.OPACITIES[this.tiles[getIndex(x, var7, z)] & 255];
                        if (level > 0) {
                            this.skyLight.setData(x, var7, z, level);
                        }

                        if (--var7 <= 0 || level <= 0) {
                            break;
                        }
                    }
                }
            }
        }

        this.field_848 = var1;

        for (int var8 = 0; var8 < 16; ++var8) {
            for (int var9 = 0; var9 < 16; ++var9) {
                this.method_638(var8, var9);
            }
        }

        this.changed = true;
    }

    private void method_638(int x, int z) {
        int var3 = this.getYHeight(x, z);
        int tileX = this.xPos * 16 + x;
        int tileZ = this.zPos * 16 + z;
        this.method_646(tileX - 1, tileZ, var3);
        this.method_646(tileX + 1, tileZ, var3);
        this.method_646(tileX, tileZ - 1, var3);
        this.method_646(tileX, tileZ + 1, var3);
    }

    private void method_646(int x, int z, int k) {
        int var4 = this.level.getYHeight(x, z);
        if (var4 > k) {
            this.level.enqueueLightUpdate(LightLayer.SKY, x, k, z, x, var4, z);
            this.changed = true;
        } else if (var4 < k) {
            this.level.enqueueLightUpdate(LightLayer.SKY, x, var4, z, x, k, z);
            this.changed = true;
        }
    }

    @Override
    public void primeHeightmap() {
        int var1 = 15;

        for(int x = 0; x < 16; ++x) {
            for(int z = 0; z < 16; ++z) {
                int y = 15;

                while(y > 0 && Tile.OPACITIES[this.tiles[getIndex(x, y - 1, z)] & 15] == 0) {
                    --y;
                }

                SectionTracker.getHeightmap(this.packedPos).onOpacityChange(x, this.yPos * 16 + y, z, Tile.OPACITIES[this.tiles[getIndex(x, y - 1, z)] & 15]);
                if (y < var1) {
                    var1 = y;
                }
            }
        }

        this.field_848 = var1;
        this.changed = true;
    }

//    public final void cubic_method_638(int x, int z) {
//        int height = this.getYHeight(x, z);
//        int var4 = this.xPos * 16 + x;
//        int var5 = this.zPos * 16 + z;
//        this.method_646(var4 - 1, var5, height);
//        this.method_646(var4 + 1, var5, height);
//        this.method_646(var4, var5 - 1, height);
//        this.method_646(var4, var5 + 1, height);
//    }
//
//    private void method_646(int x, int z, int height) {
//        int var4 = this.level.getYHeight(x, z);
//        if (var4 > height) {
//            this.level.enqueueLightUpdate(LightLayer.SKY, x, height, z, x, var4, z);
//            this.changed = true;
//        } else if (var4 < height) {
//            this.level.enqueueLightUpdate(LightLayer.SKY, x, var4, z, x, height, z);
//            this.changed = true;
//        }
//
//    }

//    @Override
//    public int getLightLevel(LightLayer lightLayer, int i, int j, int k) {
//        return 15;
//    }

}
