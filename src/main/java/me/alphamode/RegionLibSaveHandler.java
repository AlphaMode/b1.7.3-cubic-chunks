package me.alphamode;

import cubicchunks.regionlib.impl.EntryLocation2D;
import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.SaveCubeColumns;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.UnpooledByteBufAllocator;
import me.alphamode.ext.CubicSaveHandler;
import me.alphamode.world.chunk.CubicChunk;
import net.minecraft.NbtIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.save.DimensionSaveHandler;
import net.minecraft.world.save.SaveHandler;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class RegionLibSaveHandler implements SaveHandler, CubicSaveHandler {
    private final Path path;
    private SaveCubeColumns save;

    public RegionLibSaveHandler(Path path) throws IOException {
        this.path = Objects.requireNonNull(path, "path");
        this.save = SaveCubeColumns.create(path);
    }

    @Override
    public Chunk getChunk(Level level, int x, int y, int z) throws IOException {
        //see comment in readColumn
        Optional<ByteBuffer> data = this.save.load(new EntryLocation3D(x, y, z), true);
        if (data.isPresent()) {
            CompoundTag chunkData = NbtIo.read(new ByteArrayInputStream(data.get().array())); //decompress and parse NBT
            if (!chunkData.hasKey("Level")) {
                System.out.println("Chunk file at " + x + "," + y + "," + z + " is missing level data, skipping");
                return null;
            } else if (!chunkData.getCompoundTag("Level").hasKey("Blocks")) {
                System.out.println("Chunk file at " + x + "," + y + "," + z + " is missing block data, skipping");
                return null;
            } else {
                Chunk var6 = DimensionSaveHandler.readNbt(level, chunkData.getCompoundTag("Level"));
                if (!((CubicChunk)var6).equals(x, y, z)) {
                    System.out
                            .println(
                                    "Chunk file at " + x + "," + y + "," + z + " is in the wrong location; relocating. (Expected " + x + ", " + y + ", " + z + ", got " + var6.xPos + ", " + ((CubicChunk)var6).yPos + ", " + var6.zPos + ")"
                            );
                    chunkData.putInt("xPos", x);
                    chunkData.putInt("yPos", y);
                    chunkData.putInt("zPos", z);
                    var6 = DimensionSaveHandler.readNbt(level, chunkData.getCompoundTag("Level"));
                }

                var6.method_1830();
                return var6;
            }
        } else {
            return null;
        }
    }

    @Override
    public Chunk getChunk(Level level, int x, int z) {
        //see comment in readColumn
        try {
            Optional<ByteBuffer> data = this.save.load(new EntryLocation2D(x, z), true);
            if (data.isPresent()) {
                CompoundTag chunkData = NbtIo.read(new ByteArrayInputStream(data.get().array())); //decompress and parse NBT
                if (!chunkData.hasKey("Level")) {
                    System.out.println("Chunk file at " + x + "," + z + " is missing level data, skipping");
                    return null;
                } else if (!chunkData.getCompoundTag("Level").hasKey("Blocks")) {
                    System.out.println("Chunk file at " + x + "," + z + " is missing block data, skipping");
                    return null;
                } else {
                    Chunk chunk = DimensionSaveHandler.readNbt(level, chunkData.getCompoundTag("Level"));
                    if (!chunk.equals(x, z)) {
                        System.out
                                .println(
                                        "Chunk file at " + x + "," + z + " is in the wrong location; relocating. (Expected " + x + ", " + z + ", got " + chunk.xPos + ", " + ((CubicChunk) chunk).yPos + ", " + chunk.zPos + ")"
                                );
                        chunkData.putInt("xPos", x);
                        chunkData.putInt("zPos", z);
                        chunk = DimensionSaveHandler.readNbt(level, chunkData.getCompoundTag("Level"));
                    }

                    chunk.method_1830();
                    return chunk;
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveChunk(Level level, Chunk chunk) {
        level.checkSession();

        CompoundTag chunkData = new CompoundTag();
        CompoundTag levelData = new CompoundTag();
        chunkData.putTag("Level", levelData);
        DimensionSaveHandler.writeNbt(chunk, level, levelData);

        ByteBuf compressedBuf = UnpooledByteBufAllocator.DEFAULT.ioBuffer();
        try {
            //compress NBT data
            NbtIo.write(chunkData, (OutputStream) new ByteBufOutputStream(compressedBuf));

            //write compressed data to disk
            try {
                this.save.save3d(new EntryLocation3D(chunk.xPos, ((CubicChunk) chunk).yPos, chunk.zPos), compressedBuf.nioBuffer());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            compressedBuf.release();
        }

//        LevelData levelDat = level.getLevelData();
//        levelDat.setSize(levelDat.getSize() + (long)CubicRegionFileCache.getSizeDelta(this.path.toFile(), chunk.xPos, ((CubicChunk) chunk).yPos, chunk.zPos));
    }

    @Override
    public void method_29(Level level, Chunk chunk) {

    }

    @Override
    public void method_25() {

    }

    @Override
    public void method_28() {

    }
}
