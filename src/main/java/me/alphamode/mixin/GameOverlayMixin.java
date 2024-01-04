package me.alphamode.mixin;

import net.minecraft.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entities;
import net.minecraft.world.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Gui.class)
public abstract class GameOverlayMixin extends GuiComponent {
    @Shadow private Minecraft minecraft;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void renderExtraInfo(float bl, boolean i, int j, int par4, CallbackInfo ci, Window var5, int width, int height, Font font) {
        int line = 22;

        int tile = (this.minecraft.hitResult != null && this.minecraft.hitResult.hitType == HitType.TILE) ? this.minecraft.level.getTile(this.minecraft.hitResult.x, this.minecraft.hitResult.y, this.minecraft.hitResult.z) : 0;
        if (tile != 0) {
            int x = this.minecraft.hitResult.x;
            int y = this.minecraft.hitResult.y;
            int z = this.minecraft.hitResult.z;
            String targeted = "Targeted Tile: " + tile + " (%s)".formatted(Tile.tiles[tile].getName()) + " Meta: " + this.minecraft.level.getTileMeta(x, y, z) + " X: %d Y: %d Z: %d".formatted(x, y, z); // this method name is wrong it gets the tile meta
            drawString(font, targeted, width - font.getLength(targeted) - 2, line, 14737632);
            line += 10;
//            String meta = " Meta: " + ;
//            drawString(font, meta, width - font.getLength(meta) - 2, line, 14737632);
//            line += 10;
        }
        if (this.minecraft.hitResult != null && this.minecraft.hitResult.entity != null) {
            String targetedEntity = "Targeted Entity: " + Entities.getEntityId(this.minecraft.hitResult.entity) + " (%s)".formatted(Entities.getEntityName(this.minecraft.hitResult.entity));
            drawString(font, targetedEntity, width - font.getLength(targetedEntity) - 2, line, 14737632);
        }

        this.drawString(font, "Biome: " + this.minecraft.player.level.getBiomeProvider(Mth.floor(this.minecraft.player.y) >> 4).getBiome(Mth.floor(this.minecraft.player.x), Mth.floor(this.minecraft.player.z)).name, 2, 104, 0xE0E0E0);
    }
}
