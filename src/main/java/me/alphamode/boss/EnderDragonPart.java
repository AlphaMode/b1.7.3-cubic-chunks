package me.alphamode.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class EnderDragonPart extends Entity {
    public final EnderDragon parentMob;
    public final String name;

    public EnderDragonPart(EnderDragon enderDragon, String string, float f, float g) {
        super(enderDragon.level);
        setDimensions(f, g);
        this.parentMob = enderDragon;
        this.name = string;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }

//    @Override
//    public boolean isPickable() {
//        return true;
//    }

//    @Override
//    public boolean hurt(DamageSource damageSource, float f) {
//        return this.isInvulnerableTo(damageSource) ? false : this.parentMob.hurt(this, damageSource, f);
//    }
//
//    @Override
//    public boolean is(Entity entity) {
//        return this == entity || this.parentMob == entity;
//    }
//
//    @Override
//    public Packet<?> getAddEntityPacket() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public EntityDimensions getDimensions(Pose pose) {
//        return this.size;
//    }

    @Override
    protected void defineSynchedData() {

    }
}