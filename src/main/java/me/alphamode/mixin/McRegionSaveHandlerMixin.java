package me.alphamode.mixin;

import me.alphamode.CubicRegionFileCache;
import me.alphamode.ext.CubicSaveHandler;
import me.alphamode.world.CubicChunk;
import net.minecraft.NbtIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.LevelData;
import net.minecraft.world.save.DimensionSaveHandler;
import net.minecraft.world.save.McRegionSaveHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.*;

@Mixin(McRegionSaveHandler.class)
public class McRegionSaveHandlerMixin implements CubicSaveHandler {
    @Shadow @Final private File basePath;

    @Override
    public Chunk getChunk(Level level, int x, int y, int z) {
        InputStream var4 = CubicRegionFileCache.getChunkDataInputStream(this.basePath, x, y, z);
        if (var4 != null) {
            CompoundTag var5 = NbtIo.read(var4);
            if (!var5.hasKey("Level")) {
                System.out.println("Chunk file at " + x + "," + y + "," + z + " is missing level data, skipping");
                return null;
            } else if (!var5.getCompoundTag("Level").hasKey("Blocks")) {
                System.out.println("Chunk file at " + x + "," + y + "," + z + " is missing block data, skipping");
                return null;
            } else {
                Chunk var6 = DimensionSaveHandler.readNbt(level, var5.getCompoundTag("Level"));
                if (!((CubicChunk)var6).equals(x, y, z)) {
                    System.out
                            .println(
                                    "Chunk file at " + x + "," + y + "," + z + " is in the wrong location; relocating. (Expected " + x + ", " + y + ", " + z + ", got " + var6.xPos + ", " + ((CubicChunk)var6).yPos + ", " + var6.zPos + ")"
                            );
                    var5.putInt("xPos", x);
                    var5.putInt("yPos", y);
                    var5.putInt("zPos", z);
                    var6 = DimensionSaveHandler.readNbt(level, var5.getCompoundTag("Level"));
                }

                var6.method_1830();
                return var6;
            }
        } else {
            return null;
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void saveChunk(Level level, Chunk chunk) {
        level.checkSession();

        try {
            OutputStream var3 = CubicRegionFileCache.getChunkDataOutputStream(this.basePath, chunk.xPos, ((CubicChunk)chunk).yPos, chunk.zPos);
            CompoundTag var4 = new CompoundTag();
            CompoundTag var5 = new CompoundTag();
            var4.putTag("Level", var5);
            DimensionSaveHandler.writeNbt(chunk, level, var5);
            NbtIo.write(var4, var3);
            var3.close();
            LevelData var6 = level.getLevelData();
            var6.setSize(var6.getSize() + (long) CubicRegionFileCache.getSizeDelta(this.basePath, chunk.xPos, ((CubicChunk)chunk).yPos, chunk.zPos));
        } catch (Exception var7) {
            var7.printStackTrace();
        }
    }
}
