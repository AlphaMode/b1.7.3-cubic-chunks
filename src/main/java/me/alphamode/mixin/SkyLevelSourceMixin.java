package me.alphamode.mixin;

import net.minecraft.world.level.levelgen.SkyLevelSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SkyLevelSource.class)
public class SkyLevelSourceMixin {

    /**
     * @author AlphaMode
     * @reason correct name
     */
    @Overwrite
    public String getDebugInfo() {
        return "SkyLevelSource";
    }
}
