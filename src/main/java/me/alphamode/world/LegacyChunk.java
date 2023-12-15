//package me.alphamode.world;
//
//import net.minecraft.world.Chunk;
//import net.minecraft.world.Level;
//
//public class LegacyChunk extends CubicChunk {
//    public byte[] legacyTiles;
//
//    private CubicChunk[] chunks = new CubicChunk[8];
//
//    public LegacyChunk(Level level, Chunk legacy, int x, int z) {
//        super(level, x, z);
//    }
//
//    public void convert() {
//        this.tiles = CubicChunk.convert(this.tiles, yPos & 8);
//        this.tileMeta = CubicChunk.convert(this.tileMeta, yPos & 8);
//        this.skyLight = CubicChunk.convert(this.skyLight, yPos & 8);
//        this.blockLight = CubicChunk.convert(this.blockLight, yPos & 8);
//    }
//
//    public void setChunk(CubicChunk chunk, int y) {
//        chunks[y & 7] = chunk;
//    }
//}
