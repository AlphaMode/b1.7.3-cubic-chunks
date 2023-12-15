package me.alphamode.boss.phases;

import me.alphamode.boss.EndCrystal;
import me.alphamode.boss.EnderDragon;
import me.alphamode.util.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Player;
import javax.annotation.Nullable;

public abstract class AbstractDragonPhaseInstance implements DragonPhaseInstance {
    protected final EnderDragon dragon;

    public AbstractDragonPhaseInstance(EnderDragon enderDragon) {
        this.dragon = enderDragon;
    }

    @Override
    public boolean isSitting() {
        return false;
    }

    @Override
    public void doClientTick() {
    }

    @Override
    public void doServerTick() {
    }

//    @Override
//    public void onCrystalDestroyed(EndCrystal endCrystal, BlockPos blockPos, DamageSource damageSource, @Nullable Player player) {
//    }

    @Override
    public void begin() {
    }

    @Override
    public void end() {
    }

    @Override
    public float getFlySpeed() {
        return 0.6F;
    }

    @Nullable
    @Override
    public Vec3 getFlyTargetLocation() {
        return null;
    }

//    @Override
//    public float onHurt(DamageSource damageSource, float f) {
//        return f;
//    }

    @Override
    public float getTurnSpeed() {
        float f = Mth.sqrt(EnderDragon.getHorizontalDistanceSqr(new Vec3(this.dragon.motionX, this.dragon.motionY, this.dragon.motionZ))) + 1.0F;
        float g = Math.min(f, 40.0F);
        return 0.7F / g / f;
    }
}