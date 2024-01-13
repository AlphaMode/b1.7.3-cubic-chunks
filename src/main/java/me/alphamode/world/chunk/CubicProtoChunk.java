//package me.alphamode.world.chunk;
//
//import net.minecraft.world.Chunk;
//import net.minecraft.world.Level;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.level.LightLayer;
//import net.minecraft.world.level.levelgen.LevelSource;
//import net.minecraft.world.phys.AABB;
//import net.minecraft.world.tile.entity.TileEntity;
//
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.ForkJoinPool;
//
//public class CubicProtoChunk extends CubicChunk {
//    private static final ForkJoinPool POOL = new ForkJoinPool();
//    private CubicChunk currentChunk;
//    private final LevelSource levelSource;
//    public CubicProtoChunk(Level level, LevelSource levelSource, int x, int y, int z) {
//        super(level, new byte[32768], x, y, z);
//        this.levelSource = levelSource;
//        this.currentChunk = new CubicChunk(level, new byte[CubicChunk.CHUNK_SIZE], x, y, z);
//        POOL.execute(() -> {
//            Chunk legacyChunk = levelSource.getChunk(x, z);
//            for (int i = 0; i < 8; i++) {
//                CubicChunk chunk = CubicChunk.convertLocal(legacyChunk, y);
//                if (chunk.yPos == y)
//                    this.currentChunk = chunk;
//                this.levelSource.putCache(chunk);
//            }
//        });
//    }
//
//    @Override
//    public boolean equals(int x, int z) {
//        return currentChunk.equals(x, z);
//    }
//
//    @Override
//    public boolean equals(int x, int y, int z) {
//        return currentChunk.equals(x, y, z);
//    }
//
//    @Override
//    public int getTile(int x, int y, int z) {
//        return currentChunk.getTile(x, y, z);
//    }
//
//    @Override
//    public boolean setTile(int x, int y, int z, int tile, int meta) {
//        return currentChunk.setTile(x, y, z, tile, meta);
//    }
//
//    @Override
//    public boolean setTile(int x, int y, int z, int tile) {
//        return currentChunk.setTile(x, y, z, tile);
//    }
//
//    @Override
//    public void updateSkyLight(int x, int y, int z) {
//        super.updateSkyLight(x, y, z);
//    }
//
//    @Override
//    public void removeEntity(Entity entity, int i) {
//        currentChunk.removeEntity(entity, i);
//    }
//
//    @Override
//    public void placeTileEntity(TileEntity tileEntity) {
//        currentChunk.placeTileEntity(tileEntity);
//    }
//
//    @Override
//    public TileEntity getTileEntity(int x, int y, int z) {
//        return currentChunk.getTileEntity(x, y, z);
//    }
//
//    @Override
//    public void placeTileEntity(int x, int y, int z, TileEntity tileEntity) {
//        currentChunk.placeTileEntity(x, y, z, tileEntity);
//    }
//
//    @Override
//    public void addEntity(Entity entity) {
//        currentChunk.addEntity(entity);
//    }
//
//    @Override
//    public void load() {
//        currentChunk.load();
//    }
//
//    @Override
//    public void unload() {
//        currentChunk.unload();
//    }
//
//    @Override
//    public void getEntities(Entity entity, AABB aABB, List list) {
//        currentChunk.getEntities(entity, aABB, list);
//    }
//
//    @Override
//    public void getEntitiesOfClass(Class entityClass, AABB aABB, List list) {
//        currentChunk.getEntitiesOfClass(entityClass, aABB, list);
//    }
//
//    @Override
//    public int method_631(byte[] tiles, int i, int j, int k, int l, int m, int n, int o) {
//        return currentChunk.method_631(tiles, i, j, k, l, m, n, o);
//    }
//
//    @Override
//    public void method_637() {
//        currentChunk.method_637();
//    }
//
//    @Override
//    public int getLightLevel(LightLayer lightType, int x, int y, int z) {
//        return currentChunk.getLightLevel(lightType, x, y, z);
//    }
//
//    @Override
//    public int getLightLevel(int i, int j, int k, int l) {
//        return currentChunk.getLightLevel(i, j, k, l);
//    }
//
//    @Override
//    public void primeHeightmap() {
//        currentChunk.primeHeightmap();
//    }
//
//    @Override
//    public int getYHeight(int i, int j) {
//        return currentChunk.getYHeight(i, j);
//    }
//
//    @Override
//    public void method_616() {
//        currentChunk.method_616();
//    }
//
//    @Override
//    public void method_641() {
//        currentChunk.method_641();
//    }
//
//    @Override
//    public int getMeta(int i, int j, int k) {
//        return currentChunk.getMeta(i, j, k);
//    }
//
//    @Override
//    public void setMeta(int i, int j, int k, int l) {
//        currentChunk.setMeta(i, j, k, l);
//    }
//
//    @Override
//    public void setLightLevel(LightLayer lightLayer, int i, int j, int k, int l) {
//        currentChunk.setLightLevel(lightLayer, i, j, k, l);
//    }
//
//    @Override
//    public void removeEntity(Entity entity) {
//        currentChunk.removeEntity(entity);
//    }
//
//    @Override
//    public boolean isHighestTile(int i, int j, int k) {
//        return currentChunk.isHighestTile(i, j, k);
//    }
//
//    @Override
//    public void removeTileEntity(int i, int j, int k) {
//        currentChunk.removeTileEntity(i, j, k);
//    }
//
//    @Override
//    public void setChanged() {
//        currentChunk.setChanged();
//    }
//
//    @Override
//    public boolean method_630(boolean bl) {
//        return currentChunk.method_630(bl);
//    }
//
//    @Override
//    public Random getRandom(long l) {
//        return currentChunk.getRandom(l);
//    }
//
//    @Override
//    public boolean isEmptyChunk() {
//        return currentChunk.isEmptyChunk();
//    }
//
//    @Override
//    public void method_1830() {
//        currentChunk.method_1830();
//    }
//}
