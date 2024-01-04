package me.alphamode.mixin;

import net.minecraft.world.level.GrassColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GrassColor.class)
public class GrassColorMixin {
    @Shadow private static int[] pixels;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static int get(double d, double e) {
        int n = (int)((1.0 - d) * 255.0);
        int n2 = (int)((1.0 - (e *= d)) * 255.0);
        if ((n2 << 8 | n) > 65536 || (n2 << 8 | n) < 0)
            return 0;
        return pixels[n2 << 8 | n];
    }
}
