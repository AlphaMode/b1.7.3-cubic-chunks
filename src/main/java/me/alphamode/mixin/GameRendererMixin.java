package me.alphamode.mixin;

import net.minecraft.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow private Minecraft minecraft;

    @Redirect(method = "setupClearColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Level;getFogColor(F)Lnet/minecraft/Vec3;"))
    private Vec3 getDimensionFog(Level instance, float partialTick) {
        return instance.getFogColor(partialTick, Mth.floor(this.minecraft.cameraEntity.y) >> 4);
    }
}
