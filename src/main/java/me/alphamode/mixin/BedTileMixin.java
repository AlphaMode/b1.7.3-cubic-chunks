package me.alphamode.mixin;

import net.minecraft.world.Level;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.entity.Player;
import net.minecraft.world.tile.BedTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BedTile.class)
public class BedTileMixin {
    @Redirect(method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;mayRespawn()Z"))
    private boolean canRespawn(Dimension instance, Level level, int x, int y, int z, Player player) {
        return level.getLevelSource().getDimension(y >> 4).mayRespawn();
    }
}
