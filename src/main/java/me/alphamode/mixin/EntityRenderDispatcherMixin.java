package me.alphamode.mixin;

import me.alphamode.boss.EndCrystal;
import me.alphamode.client.renderer.EndCrystalRenderer;
import net.minecraft.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Shadow private Map<Class<?>, EntityRenderer> renderers;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void init(CallbackInfo ci) {
        this.renderers.put(EndCrystal.class, new EndCrystalRenderer());
    }
}
