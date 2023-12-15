package me.alphamode.mixin;

import net.minecraft.ChunkData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkData.class)
public class ChunkDataMixin {
    @Shadow @Final public byte[] data;

    private static int getIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getData(int x, int y, int z) {
        int index = getIndex(x, y, z);
        int var5 = index >> 1;
        int var6 = index & 1;
        return var6 == 0 ? this.data[var5] & 15 : this.data[var5] >> 4 & 15;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void setData(int x, int y, int z, int data) {
        int index = getIndex(x, y, z);
        int var6 = index >> 1;
        int var7 = index & 1;
        if (var7 == 0) {
            this.data[var6] = (byte)(this.data[var6] & 240 | data & 15);
        } else {
            this.data[var6] = (byte)(this.data[var6] & 15 | (data & 15) << 4);
        }
    }
}
