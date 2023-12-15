package me.alphamode.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Slime.class)
public abstract class SlimeMixin extends Entity {
    public SlimeMixin(Level level) {
        super(level);
    }

    @Redirect(method = "isDarkEnoughToSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Level;getChunkFromPos(II)Lnet/minecraft/world/Chunk;"))
    private Chunk useCubicMethod(Level instance, int j, int i) {
        return this.level.getChunkFromPos(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z));
    }
}
