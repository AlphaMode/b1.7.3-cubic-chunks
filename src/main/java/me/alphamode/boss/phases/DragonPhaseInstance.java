package me.alphamode.boss.phases;

import me.alphamode.boss.EndCrystal;
import me.alphamode.util.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Player;
import javax.annotation.Nullable;

public interface DragonPhaseInstance {
    boolean isSitting();

    void doClientTick();

    void doServerTick();

//    void onCrystalDestroyed(EndCrystal endCrystal, BlockPos blockPos, DamageSource damageSource, @Nullable Player player);

    void begin();

    void end();

    float getFlySpeed();

    float getTurnSpeed();

    EnderDragonPhase<? extends DragonPhaseInstance> getPhase();

    @Nullable
    Vec3 getFlyTargetLocation();

//    float onHurt(DamageSource damageSource, float f);
}
