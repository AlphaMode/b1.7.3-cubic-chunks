package me.alphamode.mixin;

import net.minecraft.class_378;
import net.minecraft.client.Minecraft;
import net.minecraft.stats.Achievements;
import net.minecraft.util.Mth;
import net.minecraft.world.Level;
import net.minecraft.world.entity.LocalPlayer;
import net.minecraft.world.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
//    @Inject(method = "method_669", at = @At("TAIL"))
//    private void addY(CallbackInfo ci) {
//        this.field_926 = this.field_130.field_1587;
//    }

    @Shadow protected Minecraft minecraft;

    @Shadow public class_378 field_130;

    public LocalPlayerMixin(Level level) {
        super(level);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void aiStep() {
        if (!this.minecraft.field_3086.method_2117(Achievements.openInventory)) {
            this.minecraft.field_2905.method_2097(Achievements.openInventory);
        }

        this.oPortalTime = this.portalTime;
        if (this.field_3275) {
            if (!this.level.isClientSide && this.vechicle != null) {
                this.startRiding(null);
            }

            if (this.minecraft.screen != null) {
                this.minecraft.setScreen(null);
            }

            if (this.portalTime == 0.0F) {
                this.minecraft.soundManager.playSound("portal.trigger", 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
            }

            this.portalTime += 0.0125F;
            if (this.portalTime >= 1.0F) {
                this.portalTime = 1.0F;
                if (!this.level.isClientSide) {
                    this.field_3274 = 10;
                    this.minecraft.soundManager.playSound("portal.travel", 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
                    this.minecraft.changeDimension();
                }
            }

            this.field_3275 = false;
        } else {
            if (this.portalTime > 0.0F) {
                this.portalTime -= 0.05F;
            }

            if (this.portalTime < 0.0F) {
                this.portalTime = 0.0F;
            }
        }

        if (this.field_3274 > 0) {
            --this.field_3274;
        }

        this.field_130.method_1107(this);
        if (this.field_130.field_1590 && this.field_1342 < 0.2F) {
            this.field_1342 = 0.2F;
        }

        this.method_2245(this.x - (double)this.dimensionsWidth * 0.35, this.bb.y0 + 0.5, this.z + (double)this.dimensionsWidth * 0.35);
        this.method_2245(this.x - (double)this.dimensionsWidth * 0.35, this.bb.y0 + 0.5, this.z - (double)this.dimensionsWidth * 0.35);
        this.method_2245(this.x + (double)this.dimensionsWidth * 0.35, this.bb.y0 + 0.5, this.z - (double)this.dimensionsWidth * 0.35);
        this.method_2245(this.x + (double)this.dimensionsWidth * 0.35, this.bb.y0 + 0.5, this.z + (double)this.dimensionsWidth * 0.35);
//        if (this.abilities.flying && !this.isPassenger()) {
            double g = this.motionY;
            float flySpeed = 0.5F;
            super.aiStep();
            this.setMotion(this.motionX, g * 0.6, this.motionZ);
            this.fallDistance = 0.0F;
//        } else {
//            super.aiStep();
//        }

        int flightType = 0;
        if (this.field_130.field_1590) {
            --flightType;
        }

        if (this.field_130.field_1589) {
            ++flightType;
        }

        if (flightType != 0) {
            this.setMotion(this.motionX, (double)((float)flightType * flySpeed * 1.0F), this.motionZ);
        }
    }

    @Override
    public void updateMovement(float f, float g, float h) {
        float var4 = Mth.sqrt(f * f + g * g);
        if (!(var4 < 0.01F)) {
            if (var4 < 1.0F) {
                var4 = 1.0F;
            }

            var4 = (h * 10) / var4;
            f *= var4;
            g *= var4;
            float var5 = Mth.sin(this.yRot * (float) Math.PI / 180.0F);
            float var6 = Mth.cos(this.yRot * (float) Math.PI / 180.0F);
            this.motionX += (double)(f * var6 - g * var5);
            this.motionZ += (double)(g * var6 + f * var5);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void method_144(String msg) {
        String[] args = msg.split(" ");
        if (args[0].equalsIgnoreCase("tp")) {
            try {
                int coordX = parseCoord(args[1], Mth.floor(x));
                int coordY = parseCoord(args[2], Mth.floor(y));
                int coordZ = parseCoord(args[3], Mth.floor(z));

                setPos(coordX, coordY, coordZ);
            } catch (Exception ignored) {

            }
        }
    }

    private int parseCoord(String coord, int rel) {
        return coord.startsWith("~") ? rel + Integer.parseInt(coord.substring(1).isEmpty() ? "0" : coord.substring(1)) : Integer.parseInt(coord);
    }
}
