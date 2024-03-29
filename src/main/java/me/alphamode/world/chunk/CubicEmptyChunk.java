package me.alphamode.world.chunk;

import me.alphamode.world.chunk.CubicChunk;
import net.minecraft.world.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.tile.entity.TileEntity;

import java.util.List;
import java.util.Random;

public class CubicEmptyChunk extends CubicChunk {
    public CubicEmptyChunk(Level level, int x, int y, int z) {
        super(level, x, y, z);
        this.field_2376 = true;
    }

    public CubicEmptyChunk(Level level, byte[] tiles, int x, int y, int z) {
        super(level, tiles, x, y, z);
        this.field_2376 = true;
    }

    @Override
    public boolean equals(int i, int j) {
        return i == this.xPos && j == this.zPos;
    }

    @Override
    public int getYHeight(int i, int j) {
        return 0;
    }

    @Override
    public void method_616() {
    }

    @Override
    public void primeHeightmap() {
    }

    @Override
    public void method_637() {
    }

    @Override
    public void method_641() {
    }

    @Override
    public int getTile(int i, int j, int k) {
        return 0;
    }

    @Override
    public boolean setTile(int i, int j, int k, int tile, int meta) {
        return true;
    }

    @Override
    public boolean setTile(int i, int j, int k, int l) {
        return true;
    }

    @Override
    public int getMeta(int i, int j, int k) {
        return 0;
    }

    @Override
    public void setMeta(int i, int j, int k, int l) {
    }

    @Override
    public int getLightLevel(LightLayer lightLayer, int i, int j, int k) {
        return 0;
    }

    @Override
    public void setLightLevel(LightLayer lightLayer, int i, int j, int k, int l) {
    }

    @Override
    public int getLightLevel(int i, int j, int k, int l) {
        return 0;
    }

    @Override
    public void addEntity(Entity entity) {
    }

    @Override
    public void removeEntity(Entity entity) {
    }

    @Override
    public void removeEntity(Entity entity, int i) {
    }

    @Override
    public boolean isHighestTile(int i, int j, int k) {
        return false;
    }

    @Override
    public TileEntity getTileEntity(int i, int j, int k) {
        return null;
    }

    @Override
    public void placeTileEntity(TileEntity tileEntity) {
    }

    @Override
    public void placeTileEntity(int i, int j, int k, TileEntity tileEntity) {
    }

    @Override
    public void removeTileEntity(int i, int j, int k) {
    }

    @Override
    public void load() {
    }

    @Override
    public void unload() {
    }

    @Override
    public void setChanged() {
    }

    @Override
    public void getEntities(Entity entity, AABB aABB, List list) {
    }

    @Override
    public void getEntitiesOfClass(Class class_, AABB aABB, List list) {
    }

    @Override
    public boolean method_630(boolean bl) {
        return false;
    }

    @Override
    public int method_631(byte[] bs, int i, int j, int k, int l, int m, int n, int o) {
        int var9 = l - i;
        int var10 = m - j;
        int var11 = n - k;
        int var12 = var9 * var10 * var11;
        return var12 + var12 / 2 * 3;
    }

    @Override
    public Random getRandom(long l) {
        return new Random(
                this.level.getSeed()
                        + (long)(this.xPos * this.xPos * 4987142)
                        + (long)(this.xPos * 5947611)
                        + (long)(this.zPos * this.zPos) * 4392871L
                        + (long)(this.zPos * 389711)
                        ^ l
        );
    }

    @Override
    public boolean isEmptyChunk() {
        return true;
    }
}