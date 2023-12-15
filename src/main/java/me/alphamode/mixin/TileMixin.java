package me.alphamode.mixin;

import me.alphamode.tile.TheEndTiles;
import net.minecraft.class_515;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TileItem;
import net.minecraft.world.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Tile.class)
public class TileMixin {
    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/tile/ButtonTile;strength(F)Lnet/minecraft/world/tile/Tile;"))
    private static void initEndBlocks(CallbackInfo ci) {
        TheEndTiles.init();
    }
}
