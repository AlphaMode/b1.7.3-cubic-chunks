package me.alphamode.mixin;

import me.alphamode.gen.CubicLevelSource;
import net.minecraft.world.level.levelgen.LevelSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelSource.class)
public interface LevelSourceMixin extends CubicLevelSource {
}
