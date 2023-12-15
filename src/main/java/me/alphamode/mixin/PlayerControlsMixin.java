package me.alphamode.mixin;

import me.alphamode.ext.ControllsExtension;
import net.minecraft.PlayerControls;
import net.minecraft.class_378;
import net.minecraft.client.Options;
import net.minecraft.world.entity.Player;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerControls.class)
public abstract class PlayerControlsMixin extends class_378 implements ControllsExtension {
    public float jumpVelocity = 0.0F;
    @Shadow private boolean[] field_889;

    @Shadow private Options config;

    @Inject(method = "method_1106", at = @At("TAIL"))
    private void addFlight(int key, boolean bl, CallbackInfo ci) {
        if (key == this.config.jumpKeybind.keyId) {
            this.field_889[6] = bl;
        }
    }
    
    @Inject(method = "method_1107", at = @At("TAIL"))
    private void handleFlight(Player par1, CallbackInfo ci) {
        this.jumpVelocity = 0.0F;
        if (this.field_889[6]) {
            ++this.jumpVelocity;
        }

        if (this.field_1590) {
            this.jumpVelocity = (float)((double)this.jumpVelocity * 0.3);
        }
    }

    @Override
    public float getYVelocity() {
        return this.jumpVelocity;
    }
}
