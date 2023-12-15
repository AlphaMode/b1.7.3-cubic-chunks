package me.alphamode.boss.phases;

import me.alphamode.boss.EnderDragon;
import me.alphamode.boss.Path;
import me.alphamode.util.BlockPos;
import me.alphamode.util.Vec3;
import net.minecraft.util.Mth;
import javax.annotation.Nullable;

public class DragonHoldingPatternPhase extends AbstractDragonPhaseInstance {
//    private static final TargetingConditions NEW_TARGET_TARGETING = new TargetingConditions().range(64.0);
    private Path currentPath;
    private Vec3 targetLocation;
    private boolean clockwise;

    public DragonHoldingPatternPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public EnderDragonPhase<DragonHoldingPatternPhase> getPhase() {
        return EnderDragonPhase.HOLDING_PATTERN;
    }

    @Override
    public void doServerTick() {
        double d = this.targetLocation == null ? 0.0 : this.targetLocation.distanceToSqr(this.dragon.x, this.dragon.y, this.dragon.z);
        if (d < 100.0 || d > 22500.0/* || this.dragon.horizontalCollision || this.dragon.verticalCollision*/) {
            this.findNewTarget();
        }
    }

    @Override
    public void begin() {
        this.currentPath = null;
        this.targetLocation = null;
    }

    @Nullable
    @Override
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    private void findNewTarget() {
        if (this.currentPath != null && this.currentPath.isDone()) {
//            BlockPos blockPos = this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(EndPodiumFeature.END_PODIUM_LOCATION));
            int i = this.dragon.getDragonFight() == null ? 0 : this.dragon.getDragonFight().getCrystalsAlive();
            if (this.dragon.getRandom().nextInt(i + 3) == 0) {
//                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING_APPROACH);
                return;
            }

            double d = 64.0;
//            Player player = this.dragon.level.getNearestPlayer(NEW_TARGET_TARGETING, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
//            if (player != null) {
//                d = blockPos.distSqr(player.position(), true) / 512.0;
//            }
//
//            if (player != null
//                    && !player.abilities.invulnerable
//                    && (this.dragon.getRandom().nextInt(Mth.abs((int)d) + 2) == 0 || this.dragon.getRandom().nextInt(i + 2) == 0)) {
//                this.strafePlayer(player);
//                return;
//            }
        }

        if (this.currentPath == null || this.currentPath.isDone()) {
            int j = this.dragon.findClosestNode();
            int i = j;
            if (this.dragon.getRandom().nextInt(8) == 0) {
                this.clockwise = !this.clockwise;
                i = j + 6;
            }

            if (this.clockwise) {
                ++i;
            } else {
                --i;
            }

            if (this.dragon.getDragonFight() != null && this.dragon.getDragonFight().getCrystalsAlive() >= 0) {
                i %= 12;
                if (i < 0) {
                    i += 12;
                }
            } else {
                i -= 12;
                i &= 7;
                i += 12;
            }

            this.currentPath = this.dragon.findPath(j, i, null);
            if (this.currentPath != null) {
                this.currentPath.next();
            }
        }

        this.navigateToNextPathNode();
    }

//    private void strafePlayer(Player player) {
//        this.dragon.getPhaseManager().setPhase(EnderDragonPhase.STRAFE_PLAYER);
//        this.dragon.getPhaseManager().getPhase(EnderDragonPhase.STRAFE_PLAYER).setTarget(player);
//    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null && !this.currentPath.isDone()) {
            Vec3 vec3 = this.currentPath.currentPos();
            this.currentPath.next();
            double d = vec3.x;
            double e = vec3.z;

            double f;
            do {
                f = vec3.y + (double)(this.dragon.getRandom().nextFloat() * 20.0F);
            } while(f < vec3.y);

            this.targetLocation = new Vec3(d, f, e);
        }
    }

//    @Override
//    public void onCrystalDestroyed(EndCrystal endCrystal, BlockPos blockPos, DamageSource damageSource, @Nullable Player player) {
//        if (player != null && !player.abilities.invulnerable) {
//            this.strafePlayer(player);
//        }
//    }
}