package me.alphamode.mixin;

import me.alphamode.ext.RegionFileExt;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

@Mixin(RegionFile.class)
public abstract class RegionFileMixin implements RegionFileExt {
    @Shadow private ArrayList<Boolean> sectors;

    @Shadow protected abstract void write(int i, byte[] bs, int j);

    @Shadow private RandomAccessFile file;

    @Shadow @Final private static byte[] emptySector;

    @Shadow protected abstract void debug(String mode, int x, int z, int count, String in);

    @Shadow protected abstract int getOffset(int x, int z);

    @Shadow private int sizeDelta;

    @Shadow protected abstract void method_1667(int i, int j, int k);

    @Shadow protected abstract void method_1675(int i, int j, int k);

    @Override
    public synchronized void write(int x, int y, int z, byte[] data, int count) {
        try {
            int maxSections = this.getOffset(x, z);
            int startSection = maxSections >> 8;
            int var7 = maxSections & 0xFF;
            int var8 = (count + 5) / 4096 + 1;
            if (var8 >= 256) {
                return;
            }

            if (startSection != 0 && var7 == var8) {
                this.debug("SAVE", x, z, count, "rewrite");
                this.write(startSection, data, count);
            } else {
                for(int var9 = 0; var9 < var7; ++var9) {
                    this.sectors.set(startSection + var9, true);
                }

                int var15 = this.sectors.indexOf(true);
                int var10 = 0;
                if (var15 != -1) {
                    for(int var11 = var15; var11 < this.sectors.size(); ++var11) {
                        if (var10 != 0) {
                            if (this.sectors.get(var11)) {
                                ++var10;
                            } else {
                                var10 = 0;
                            }
                        } else if (this.sectors.get(var11)) {
                            var15 = var11;
                            var10 = 1;
                        }

                        if (var10 >= var8) {
                            break;
                        }
                    }
                }

                if (var10 >= var8) {
                    this.debug("SAVE", x, z, count, "reuse");
                    startSection = var15;
                    this.method_1667(x, z, var15 << 8 | var8);

                    for(int var17 = 0; var17 < var8; ++var17) {
                        this.sectors.set(startSection + var17, false);
                    }

                    this.write(startSection, data, count);
                } else {
                    this.debug("SAVE", x, z, count, "grow");
                    this.file.seek(this.file.length());
                    startSection = this.sectors.size();

                    for(int var16 = 0; var16 < var8; ++var16) {
                        this.file.write(emptySector);
                        this.sectors.add(false);
                    }

                    this.sizeDelta += 4096 * var8;
                    this.write(startSection, data, count);
                    this.method_1667(x, z, startSection << 8 | var8);
                }
            }

            this.method_1675(x, z, (int)(System.currentTimeMillis() / 1000L));
        } catch (IOException var12) {
            var12.printStackTrace();
        }
    }
}
