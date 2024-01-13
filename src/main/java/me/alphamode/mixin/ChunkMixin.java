package me.alphamode.mixin;

import net.minecraft.world.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Chunk.class)
public class ChunkMixin {

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void method_637() {
    }
}
