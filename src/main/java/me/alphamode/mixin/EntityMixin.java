package me.alphamode.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void keepAlive(CallbackInfo ci) {
        if ((Object) this instanceof Player entity) {
            entity.health = 20;
        }
    }
}