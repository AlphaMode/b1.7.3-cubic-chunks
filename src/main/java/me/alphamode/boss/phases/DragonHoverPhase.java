package me.alphamode.boss.phases;

import me.alphamode.boss.EnderDragon;
import me.alphamode.util.Vec3;
import javax.annotation.Nullable;

public class DragonHoverPhase extends AbstractDragonPhaseInstance {
    private Vec3 targetLocation;

    public DragonHoverPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public void doServerTick() {
        if (this.targetLocation == null) {
            this.targetLocation = new Vec3(this.dragon.x, this.dragon.y, this.dragon.z);
        }
    }

    @Override
    public boolean isSitting() {
        return true;
    }

    @Override
    public void begin() {
        this.targetLocation = null;
    }

    @Override
    public float getFlySpeed() {
        return 1.0F;
    }

    @Nullable
    @Override
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override
    public EnderDragonPhase<DragonHoverPhase> getPhase() {
        return EnderDragonPhase.HOVERING;
    }
}