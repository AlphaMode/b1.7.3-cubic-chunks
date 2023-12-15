package me.alphamode.mixin;

import me.alphamode.RegionLibSaveHandler;
import net.minecraft.McRegionLevelSaver;
import net.minecraft.OldLevelSaver;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.NetherDimension;
import net.minecraft.world.save.McRegionSaveHandler;
import net.minecraft.world.save.SaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.File;
import java.io.IOException;

@Mixin(McRegionLevelSaver.class)
public abstract class McRegionLevelSaverMixin extends OldLevelSaver {
    public McRegionLevelSaverMixin(File file, String string, boolean bl) {
        super(file, string, bl);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public SaveHandler readDimension(Dimension dimension) {
        File var2 = this.getPath();
        try {
            if (dimension instanceof NetherDimension) {
                File var3 = new File(var2, "DIM-1");
                var3.mkdirs();
                return new RegionLibSaveHandler(var3.toPath());
            } else {
                return new RegionLibSaveHandler(var2.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
