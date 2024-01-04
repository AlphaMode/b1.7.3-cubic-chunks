package me.alphamode.mixin;

import me.alphamode.ext.CubicSaveHandler;
import me.alphamode.world.chunk.CubicChunk;
import net.minecraft.ChunkData;
import net.minecraft.NbtIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.LevelData;
import net.minecraft.world.entity.Entities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.save.DimensionSaveHandler;
import net.minecraft.world.tile.entity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Mixin(DimensionSaveHandler.class)
public abstract class DimensionSaveHandlerMixin implements CubicSaveHandler {
    @Shadow private File dimensionFile;

    @Shadow private boolean field_1469;

    private File getChunkFile(int x, int y, int z) {
        String var3 = "c." + Integer.toString(x, 36) + "." + Integer.toString(y, 36) + "." + Integer.toString(z, 36) + ".dat";
        String xCoord = Integer.toString(x & 63, 36);
        String yCoord = Integer.toString(x & 63, 36);
        String zCoord = Integer.toString(z & 63, 36);
        File var6 = new File(this.dimensionFile, xCoord);
        if (!var6.exists()) {
            if (!this.field_1469) {
                return null;
            }

            var6.mkdir();
        }

        var6 = new File(var6, yCoord);
        if (!var6.exists()) {
            if (!this.field_1469) {
                return null;
            }

            var6.mkdir();
        }

        var6 = new File(var6, zCoord);
        if (!var6.exists()) {
            if (!this.field_1469) {
                return null;
            }

            var6.mkdir();
        }

        var6 = new File(var6, var3);
        return !var6.exists() && !this.field_1469 ? null : var6;
    }

    @Override
    public Chunk getChunk(Level level, int x, int y, int z) {
        File var4 = this.getChunkFile(x, y, z);
        if (var4 != null && var4.exists()) {
            try {
                FileInputStream var5 = new FileInputStream(var4);
                CompoundTag var6 = NbtIo.read(var5);
                if (!var6.hasKey("Level")) {
                    System.out.println("Chunk file at " + x + "," + y + "," + z + " is missing level data, skipping");
                    return null;
                }

                if (!var6.getCompoundTag("Level").hasKey("Blocks")) {
                    System.out.println("Chunk file at " + x + "," + y + "," + z + " is missing block data, skipping");
                    return null;
                }

                CubicChunk chunk = (CubicChunk) readNbt(level, var6.getCompoundTag("Level"));
                if (!chunk.equals(x, y, z)) {
                    System.out
                            .println(
                                    "Chunk file at " + x + "," + y + "," + z + " is in the wrong location; relocating. (Expected " + x + ", " + y + ", " + z + ", got " + chunk.xPos + ", " + chunk.yPos + ", " + chunk.zPos + ")"
                            );
                    var6.putInt("xPos", x);
                    var6.putInt("yPos", y);
                    var6.putInt("zPos", z);
                    chunk = (CubicChunk) readNbt(level, var6.getCompoundTag("Level"));
                }

                chunk.method_1830();
                return chunk;
            } catch (Exception var8) {
                var8.printStackTrace();
            }
        }

        return null;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void saveChunk(Level level, Chunk chunk) {
        level.checkSession();
        File var3 = this.getChunkFile(chunk.xPos, ((CubicChunk)chunk).yPos, chunk.zPos);
        if (var3.exists()) {
            LevelData var4 = level.getLevelData();
            var4.setSize(var4.getSize() - var3.length());
        }

        try {
            File var10 = new File(this.dimensionFile, "tmp_chunk.dat");
            FileOutputStream var5 = new FileOutputStream(var10);
            CompoundTag var6 = new CompoundTag();
            CompoundTag var7 = new CompoundTag();
            var6.putTag("Level", var7);
            writeNbt(chunk, level, var7);
            NbtIo.write(var6, var5);
            var5.close();
            if (var3.exists()) {
                var3.delete();
            }

            var10.renameTo(var3);
            LevelData var8 = level.getLevelData();
            var8.setSize(var8.getSize() + var3.length());
        } catch (Exception var9) {
            var9.printStackTrace();
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static void writeNbt(Chunk chunk, Level level, CompoundTag compoundTag) {
        level.checkSession();
        compoundTag.putInt("xPos", chunk.xPos);
        compoundTag.putInt("yPos", ((CubicChunk)chunk).yPos);
        compoundTag.putInt("zPos", chunk.zPos);
        compoundTag.putLong("LastUpdate", level.getTime());
        compoundTag.putByteArray("Blocks", chunk.tiles);
        compoundTag.putByteArray("Data", chunk.tileMeta.data);
        compoundTag.putByteArray("SkyLight", chunk.skyLight.data);
        compoundTag.putByteArray("BlockLight", chunk.blockLight.data);
        compoundTag.putByteArray("HeightMap", chunk.heightMap);
        compoundTag.putBoolean("TerrainPopulated", chunk.terrainPopulated);
        chunk.lastSaveHadEntities = false;
        ListTag var3 = new ListTag();

        for(Entity entity : ((CubicChunk)chunk).cubic$entities) {
            chunk.lastSaveHadEntities = true;
            CompoundTag var7 = new CompoundTag();
            if (entity.saveAsPassenger(var7)) {
                var3.add(var7);
            }
        }

        compoundTag.putTag("Entities", var3);
        ListTag var8 = new ListTag();

        for(Object var10 : chunk.tileEntities.values()) {
            CompoundTag var11 = new CompoundTag();
            ((TileEntity)var10).save(var11);
            var8.add(var11);
        }

        compoundTag.putTag("TileEntities", var8);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static Chunk readNbt(Level level, CompoundTag compoundTag) {
        int xPos = compoundTag.getInt("xPos");
        int yPos = compoundTag.getInt("yPos");
        int zPos = compoundTag.getInt("zPos");
        Chunk var4 = new CubicChunk(level, xPos, yPos, zPos);
        var4.tiles = compoundTag.getByteArray("Blocks");
        var4.tileMeta = new ChunkData(compoundTag.getByteArray("Data"));
        var4.skyLight = new ChunkData(compoundTag.getByteArray("SkyLight"));
        var4.blockLight = new ChunkData(compoundTag.getByteArray("BlockLight"));
        var4.heightMap = compoundTag.getByteArray("HeightMap");
        var4.terrainPopulated = compoundTag.getBoolean("TerrainPopulated");
        if (!var4.tileMeta.hasData()) {
            var4.tileMeta = new ChunkData(var4.tiles.length);
        }

        if (var4.heightMap == null || !var4.skyLight.hasData()) {
            var4.heightMap = new byte[256];
            var4.skyLight = new ChunkData(var4.tiles.length);
            var4.method_637();
        }

        if (!var4.blockLight.hasData()) {
            var4.blockLight = new ChunkData(var4.tiles.length);
            var4.method_616();
        }

        ListTag var5 = compoundTag.getList("Entities");
        if (var5 != null) {
            for(int var6 = 0; var6 < var5.size(); ++var6) {
                CompoundTag var7 = (CompoundTag)var5.get(var6);
                Entity var8 = Entities.method_517(var7, level);
                var4.lastSaveHadEntities = true;
                if (var8 != null) {
                    var4.addEntity(var8);
                }
            }
        }

        ListTag var10 = compoundTag.getList("TileEntities");
        if (var10 != null) {
            for(int var11 = 0; var11 < var10.size(); ++var11) {
                CompoundTag var12 = (CompoundTag)var10.get(var11);
                TileEntity var9 = TileEntity.loadFromNbt(var12);
                if (var9 != null) {
                    var4.placeTileEntity(var9);
                }
            }
        }

        return var4;
    }
}
