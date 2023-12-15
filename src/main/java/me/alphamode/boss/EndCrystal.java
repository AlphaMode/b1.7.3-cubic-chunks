package me.alphamode.boss;

import me.alphamode.TheEndDimension;
import net.minecraft.Vec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.Optional;

public class EndCrystal extends Entity {
//    private static final EntityDataAccessor<Optional<BlockPos>> DATA_BEAM_TARGET = SynchedEntityData.defineId(
//            EndCrystal.class, EntityDataSerializers.OPTIONAL_BLOCK_POS
//    );
//    private static final EntityDataAccessor<Boolean> DATA_SHOW_BOTTOM = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.BOOLEAN);
    public int time;

    public EndCrystal(Level level) {
        super(level);
//        this.blocksBuilding = true;
        this.time = this.random.nextInt(100000);
        this.dimensionsHeight = 2.0F;
        this.dimensionsWidth = 2.0F;
    }

    public EndCrystal(Level level, double d, double e, double f) {
        this(level);
        this.setPos(d, e, f);
    }

//    @Override
//    protected boolean isMovementNoisy() {
//        return false;
//    }

    @Override
    protected void defineSynchedData() {
        this.syncData.define(19, new Vec3i());
//        this.field_2411.method_1513(20, true);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        ++this.time;
//        if (!this.level.isClientSide) {
            BlockPos blockPos = new BlockPos(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z));
            if (this.level.dimension instanceof TheEndDimension && this.level.getTile(blockPos.x, blockPos.y, blockPos.z) == 0) {
                this.level.setTile(blockPos.x, blockPos.y, blockPos.z, Tile.FIRE.id);
            }
//        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.getBeamTarget() != null) {
//            compoundTag.put("BeamTarget", NbtUtils.writeBlockPos(this.getBeamTarget()));
        }

        compoundTag.putBoolean("ShowBottom", this.showsBottom());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
//        if (compoundTag.contains("BeamTarget", 10)) {
//            this.setBeamTarget(NbtUtils.readBlockPos(compoundTag.getCompound("BeamTarget")));
//        }
//
//        if (compoundTag.contains("ShowBottom", 1)) {
//            this.setShowBottom(compoundTag.getBoolean("ShowBottom"));
//        }
    }

//    @Override
//    public boolean isPickable() {
//        return true;
//    }

//    @Override
//    public boolean hurt(DamageSource damageSource, float f) {
//        if (this.isInvulnerableTo(damageSource)) {
//            return false;
//        } else if (damageSource.getEntity() instanceof EnderDragon) {
//            return false;
//        } else {
//            if (!this.removed && !this.level.isClientSide) {
//                this.remove();
//                if (!damageSource.isExplosion()) {
//                    this.level.explode(null, this.x, this.y, this.z, 6.0F, Explosion.BlockInteraction.DESTROY);
//                }
//
//                this.onDestroyedBy(damageSource);
//            }
//
//            return true;
//        }
//    }

//    @Override
//    public void kill() {
//        this.onDestroyedBy(DamageSource.GENERIC);
//        super.kill();
//    }

//    private void onDestroyedBy(DamageSource damageSource) {
//        if (this.level.dimension instanceof TheEndDimension) {
//            TheEndDimension theEndDimension = (TheEndDimension)this.level.dimension;
//            EndDragonFight endDragonFight = theEndDimension.getDragonFight();
//            if (endDragonFight != null) {
//                endDragonFight.onCrystalDestroyed(this, damageSource);
//            }
//        }
//    }

    public void setBeamTarget(@Nullable BlockPos blockPos) {
//        this.getEntityData().set(DATA_BEAM_TARGET, Optional.ofNullable(blockPos));
    }

    @Nullable
    public BlockPos getBeamTarget() {
        return new BlockPos(0, 0, 0);//(BlockPos)((Optional)this.getEntityData().get(DATA_BEAM_TARGET)).orElse(null);
    }

    public void setShowBottom(boolean bl) {
        //this.getEntityData().set(DATA_SHOW_BOTTOM, bl);
    }

    public boolean showsBottom() {
        return true;//this.field_2411.method_1813();
    }

//    @Environment(EnvType.CLIENT)
//    @Override
//    public boolean shouldRenderAtSqrDistance(double d) {
//        return super.shouldRenderAtSqrDistance(d) || this.getBeamTarget() != null;
//    }
//
//    @Override
//    public Packet<?> getAddEntityPacket() {
//        return new ClientboundAddEntityPacket(this);
//    }
}
