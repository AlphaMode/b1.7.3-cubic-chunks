package me.alphamode.mixin;

import me.alphamode.boss.EndCrystal;
import me.alphamode.boss.EnderDragon;
import net.minecraft.world.entity.Entities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entities.class)
public abstract class EntitiesMixin {
    @Shadow
    private static void registerEntity(Class class_, String string, int i) {
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void register(CallbackInfo ci) {
        registerEntity(EnderDragon.class, "EnderDragon", 63);
        registerEntity(EndCrystal.class, "EnderCrystal", 200);
    }
}
