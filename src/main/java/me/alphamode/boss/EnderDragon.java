package me.alphamode.boss;

import me.alphamode.MathHelper;
import me.alphamode.TheEndDimension;
import me.alphamode.boss.phases.DragonPhaseInstance;
import me.alphamode.boss.phases.EnderDragonPhase;
import me.alphamode.boss.phases.EnderDragonPhaseManager;
import me.alphamode.fight.EndDragonFight;
import me.alphamode.tile.TheEndTiles;
import me.alphamode.util.Vec3;
import net.minecraft.Mob;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Player;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.tile.Tile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnderDragon extends Mob implements Enemy {
    private static final Logger LOGGER = LogManager.getLogger();
//    public static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(EnderDragon.class, EntityDataSerializers.INT);
//    private static final TargetingConditions CRYSTAL_DESTROY_TARGETING = new TargetingConditions().range(64.0);
    private static final List<Integer> DRAGON_IMMUNE = List.of(
            Tile.BEDROCK.id,
//            Tile.END_PORTAL,
//            Tile.END_PORTAL_FRAME,
//            Tile.END_GATEWAY,
            Tile.OBSIDIAN.id,
            TheEndTiles.END_STONE.id);
    public final double[][] positions = new double[64][3];
    public int posPointer = -1;
    public final EnderDragonPart[] subEntities;
    public final EnderDragonPart head;
    public final EnderDragonPart neck;
    public final EnderDragonPart body;
    public final EnderDragonPart tail1;
    public final EnderDragonPart tail2;
    public final EnderDragonPart tail3;
    public final EnderDragonPart wing1;
    public final EnderDragonPart wing2;
    public float oFlapTime;
    public float flapTime;
    public boolean inWall;
    public int dragonDeathTime;
    public EndCrystal nearestCrystal;
    private final EndDragonFight dragonFight;
    private final EnderDragonPhaseManager phaseManager;
    private int growlTime = 100;
    private int sittingDamageReceived;
    private final Node[] nodes = new Node[24];
    private final int[] nodeAdjacency = new int[24];
    private final BinaryHeap openSet = new BinaryHeap();

    public EnderDragon(Level level) {
        super(level);
        this.head = new EnderDragonPart(this, "head", 1.0F, 1.0F);
        this.neck = new EnderDragonPart(this, "neck", 3.0F, 3.0F);
        this.body = new EnderDragonPart(this, "body", 5.0F, 3.0F);
        this.tail1 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
        this.tail2 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
        this.tail3 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
        this.wing1 = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
        this.wing2 = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
        this.subEntities = new EnderDragonPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.wing1, this.wing2};
//        this.setHealth(this.getMaxHealth());
//        this.noPhysics = true;
//        this.noCulling = true;
        if (/*!level.isClientSide && */level.dimension instanceof TheEndDimension) {
            this.dragonFight = ((TheEndDimension)level.dimension).getDragonFight();
        } else {
            this.dragonFight = null;
        }

        this.phaseManager = new EnderDragonPhaseManager(this);
    }

//    @Override
//    protected void registerAttributes() {
//        super.registerAttributes();
//        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200.0);
//    }

    public double[] getLatencyPos(int i, float f) {
        if (this.health <= 0.0F) {
            f = 0.0F;
        }

        f = 1.0F - f;
        int j = this.posPointer - i & 63;
        int k = this.posPointer - i - 1 & 63;
        double[] ds = new double[3];
        double d = this.positions[j][0];
        double e = MathHelper.wrapDegrees(this.positions[k][0] - d);
        ds[0] = d + e * (double)f;
        d = this.positions[j][1];
        e = this.positions[k][1] - d;
        ds[1] = d + e * (double)f;
        ds[2] = MathHelper.lerp((double)f, this.positions[j][2], this.positions[k][2]);
        return ds;
    }

    @Override
    public void aiStep() {
//        if (this.level.isClientSide) {
//            this.setHealth(this.getHealth());
            /*if (!this.isSilent())*/ {
                float f = Mth.cos(this.flapTime * (float) (Math.PI * 2));
                float g = Mth.cos(this.oFlapTime * (float) (Math.PI * 2));
                if (g <= -0.3F && f >= -0.3F) {
                    this.level
                            .playSound(this, "entity.ender_dragon.flap", 5.0F, 0.8F + this.random.nextFloat() * 0.3F);
                }

                if (!this.phaseManager.getCurrentPhase().isSitting() && --this.growlTime < 0) {
                    this.level
                            .playSound(this, "entity.ender_dragon.growl", 2.5F, 0.8F + this.random.nextFloat() * 0.3F);
                    this.growlTime = 200 + this.random.nextInt(200);
                }
            }
//        }

        this.oFlapTime = this.flapTime;
        if (this.health <= 0.0F) {
            float f = (this.random.nextFloat() - 0.5F) * 8.0F;
            float g = (this.random.nextFloat() - 0.5F) * 4.0F;
            float h = (this.random.nextFloat() - 0.5F) * 8.0F;
            this.level.spawnParticle("explode", this.x + (double)f, this.y + 2.0 + (double)g, this.z + (double)h, 0.0, 0.0, 0.0);
        } else {
            this.checkCrystals();
            Vec3 vec3 = new Vec3(this.motionX, this.motionY, this.motionZ);
            float g = 0.2F / (Mth.sqrt(getHorizontalDistanceSqr(vec3)) * 10.0F + 1.0F);
            g *= (float)Math.pow(2.0, vec3.y);
            if (this.phaseManager.getCurrentPhase().isSitting()) {
                this.flapTime += 0.1F;
            } else if (this.inWall) {
                this.flapTime += g * 0.5F;
            } else {
                this.flapTime += g;
            }

            this.yRot = MathHelper.wrapDegrees(this.yRot);
            if (/*this.isNoAi()*/false) {
                this.flapTime = 0.5F;
            } else {
                if (this.posPointer < 0) {
                    for(int i = 0; i < this.positions.length; ++i) {
                        this.positions[i][0] = (double)this.yRot;
                        this.positions[i][1] = this.y;
                    }
                }

                if (++this.posPointer == this.positions.length) {
                    this.posPointer = 0;
                }

                this.positions[this.posPointer][0] = (double)this.yRot;
                this.positions[this.posPointer][1] = this.y;
                if (this.level.isClientSide) {
                    if (this.lerpSteps > 0) {
                        double d = this.x + (this.lerpX - this.x) / (double)this.lerpSteps;
                        double e = this.y + (this.lerpY - this.y) / (double)this.lerpSteps;
                        double j = this.z + (this.lerpZ - this.z) / (double)this.lerpSteps;
                        double k = MathHelper.wrapDegrees(this.lerpYRot - (double)this.yRot);
                        this.yRot = (float)((double)this.yRot + k / (double)this.lerpSteps);
                        this.xRot = (float)((double)this.xRot + (this.lerpXRot - (double)this.xRot) / (double)this.lerpSteps);
                        --this.lerpSteps;
                        this.setPos(d, e, j);
                        this.setRot(this.yRot, this.xRot);
                    }

                    this.phaseManager.getCurrentPhase().doClientTick();
                } else {
                    DragonPhaseInstance dragonPhaseInstance = this.phaseManager.getCurrentPhase();
                    dragonPhaseInstance.doServerTick();
                    if (this.phaseManager.getCurrentPhase() != dragonPhaseInstance) {
                        dragonPhaseInstance = this.phaseManager.getCurrentPhase();
                        dragonPhaseInstance.doServerTick();
                    }

                    Vec3 vec32 = dragonPhaseInstance.getFlyTargetLocation();
                    if (vec32 != null) {
                        double e = vec32.x - this.x;
                        double j = vec32.y - this.y;
                        double k = vec32.z - this.z;
                        double l = e * e + j * j + k * k;
                        float m = dragonPhaseInstance.getFlySpeed();
                        double n = (double)Mth.sqrt(e * e + k * k);
                        if (n > 0.0) {
                            j = MathHelper.clamp(j / n, (double)(-m), (double)m);
                        }

                        this.setMotion(this.motionX, j * 0.01, this.motionZ);
                        this.yRot = MathHelper.wrapDegrees(this.yRot);
                        double o = MathHelper.clamp(MathHelper.wrapDegrees(180.0 - MathHelper.atan2(e, k) * 180.0F / (float)Math.PI - (double)this.yRot), -50.0, 50.0);
                        Vec3 vec33 = vec32.subtract(this.x, this.y, this.z).normalize();
                        Vec3 vec34 = new Vec3(
                                (double)Mth.sin(this.yRot * (float) (Math.PI / 180.0)), this.motionY, (double)(-Mth.cos(this.yRot * (float) (Math.PI / 180.0)))
                        )
                                .normalize();
                        float p = Math.max(((float)vec34.dot(vec33) + 0.5F) / 1.5F, 0.0F);
//                        this.yRotA *= 0.8F;
//                        this.yRotA = (float)((double)this.yRotA + o * (double)dragonPhaseInstance.getTurnSpeed());
//                        this.yRot += this.yRotA * 0.1F;
                        float q = (float)(2.0 / (l + 1.0));
                        float r = 0.06F;
//                        this.moveRelative(0.06F * (p * q + (1.0F - q)), new Vec3(0.0, 0.0, -1.0));
//                        if (this.inWall) {
//                            this.move(MoverType.SELF, this.getDeltaMovement().scale(0.8F));
//                        } else {
//                            this.move(MoverType.SELF, this.getDeltaMovement());
//                        }

//                        Vec3 vec35 = this.getDeltaMovement().normalize();
//                        double s = 0.8 + 0.15 * (vec35.dot(vec34) + 1.0) / 2.0;
//                        this.setDeltaMovement(this.getDeltaMovement().multiply(s, 0.91F, s));
                    }
                }

//                this.yBodyRot = this.yRot;
                Vec3[] vec3s = new Vec3[this.subEntities.length];

                for(int t = 0; t < this.subEntities.length; ++t) {
                    vec3s[t] = new Vec3(this.subEntities[t].x, this.subEntities[t].y, this.subEntities[t].z);
                }

                float u = (float)(this.getLatencyPos(5, 1.0F)[1] - this.getLatencyPos(10, 1.0F)[1]) * 10.0F * (float) (Math.PI / 180.0);
                float v = Mth.cos(u);
                float w = Mth.sin(u);
                float x = this.yRot * (float) (Math.PI / 180.0);
                float y = Mth.sin(x);
                float z = Mth.cos(x);
                this.body.tick();
//                this.body.moveTo(this.x + (double)(y * 0.5F), this.y, this.z - (double)(z * 0.5F), 0.0F, 0.0F);
                this.wing1.tick();
//                this.wing1.moveTo(this.x + (double)(z * 4.5F), this.y + 2.0, this.z + (double)(y * 4.5F), 0.0F, 0.0F);
                this.wing2.tick();
//                this.wing2.moveTo(this.x - (double)(z * 4.5F), this.y + 2.0, this.z - (double)(y * 4.5F), 0.0F, 0.0F);
                if (/*!this.level.isClientSide &&*/ this.hurtTime == 0) {
//                    this.knockBack(
//                            this.level.getEntities(this, this.wing1.bb.inflate(4.0, 2.0, 4.0).grow(0.0, -2.0, 0.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR)
//                    );
//                    this.knockBack(
//                            this.level.getEntities(this, this.wing2.bb.inflate(4.0, 2.0, 4.0).grow(0.0, -2.0, 0.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR)
//                    );
//                    this.hurt(this.level.getEntities(this, this.head.bb.inflate(1.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
//                    this.hurt(this.level.getEntities(this, this.neck.bb.inflate(1.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                }

                double[] ds = this.getLatencyPos(5, 1.0F);
//                float aa = Mth.sin(this.yRot * (float) (Math.PI / 180.0) - this.yRotA * 0.01F);
//                float ab = Mth.cos(this.yRot * (float) (Math.PI / 180.0) - this.yRotA * 0.01F);
                this.head.tick();
                this.neck.tick();
//                float m = this.getHeadYOffset(1.0F);
//                this.head.moveTo(this.x + (double)(aa * 6.5F * v), this.y + (double)m + (double)(w * 6.5F), this.z - (double)(ab * 6.5F * v), 0.0F, 0.0F);
//                this.neck.moveTo(this.x + (double)(aa * 5.5F * v), this.y + (double)m + (double)(w * 5.5F), this.z - (double)(ab * 5.5F * v), 0.0F, 0.0F);

                for(int ac = 0; ac < 3; ++ac) {
                    EnderDragonPart enderDragonPart = null;
                    if (ac == 0) {
                        enderDragonPart = this.tail1;
                    }

                    if (ac == 1) {
                        enderDragonPart = this.tail2;
                    }

                    if (ac == 2) {
                        enderDragonPart = this.tail3;
                    }

                    double[] es = this.getLatencyPos(12 + ac * 2, 1.0F);
                    float ad = this.yRot * (float) (Math.PI / 180.0) + this.rotWrap(es[0] - ds[0]) * (float) (Math.PI / 180.0);
                    float ae = Mth.sin(ad);
                    float af = Mth.cos(ad);
                    float ag = 1.5F;
                    float ah = (float)(ac + 1) * 2.0F;
                    enderDragonPart.tick();
//                    enderDragonPart.moveTo(
//                            this.x - (double)((y * 1.5F + ae * ah) * v),
//                            this.y + (es[1] - ds[1]) - (double)((ah + 1.5F) * w) + 1.5,
//                            this.z + (double)((z * 1.5F + af * ah) * v),
//                            0.0F,
//                            0.0F
//                    );
                }

//                if (!this.level.isClientSide) {
                    this.inWall = this.checkWalls(this.head.bb) | this.checkWalls(this.neck.bb) | this.checkWalls(this.body.bb);
                    if (this.dragonFight != null) {
                        this.dragonFight.updateDragon(this);
                    }
//                }

                for(int ac = 0; ac < this.subEntities.length; ++ac) {
                    this.subEntities[ac].xo = vec3s[ac].x;
                    this.subEntities[ac].yo = vec3s[ac].y;
                    this.subEntities[ac].zo = vec3s[ac].z;
                }
            }
        }
    }

    private float getHeadYOffset(float f) {
        double d;
        if (this.phaseManager.getCurrentPhase().isSitting()) {
            d = -1.0;
        } else {
            double[] ds = this.getLatencyPos(5, 1.0F);
            double[] es = this.getLatencyPos(0, 1.0F);
            d = ds[1] - es[1];
        }

        return (float)d;
    }

    private void checkCrystals() {
        if (this.nearestCrystal != null) {
            if (this.nearestCrystal.removed) {
                this.nearestCrystal = null;
            } /*else if (this.tickCount % 10 == 0 && this.health < this.getMaxHealth()) {
                this.health += 1.0F;
            }*/
        }

        if (this.random.nextInt(10) == 0) {
//            List<EndCrystal> list = this.level.getEntitiesOfClass(EndCrystal.class, this.getBoundingBox().inflate(32.0));
//            EndCrystal endCrystal = null;
//            double d = Double.MAX_VALUE;
//
//            for(EndCrystal endCrystal2 : list) {
//                double e = endCrystal2.distanceToSqr(this);
//                if (e < d) {
//                    d = e;
//                    endCrystal = endCrystal2;
//                }
//            }
//
//            this.nearestCrystal = endCrystal;
        }
    }

    private void knockBack(List<Entity> list) {
        double d = (this.body.bb.x0 + this.body.bb.x1) / 2.0;
        double e = (this.body.bb.z0 + this.body.bb.z1) / 2.0;

        for(Entity entity : list) {
            if (entity instanceof LivingEntity) {
                double f = entity.x - d;
                double g = entity.z - e;
                double h = f * f + g * g;
//                entity.push(f / h * 4.0, 0.2F, g / h * 4.0);
//                if (!this.phaseManager.getCurrentPhase().isSitting() && ((LivingEntity)entity).getLastHurtByMobTimestamp() < entity.tickCount - 2) {
//                    entity.hurt(DamageSource.mobAttack(this), 5.0F);
//                    this.doEnchantDamageEffects(this, entity);
//                }
            }
        }
    }

    private void hurt(List<Entity> list) {
//        for(int i = 0; i < list.size(); ++i) {
//            Entity entity = (Entity)list.get(i);
//            if (entity instanceof LivingEntity) {
//                entity.hurt(DamageSource.mobAttack(this), 10.0F);
//                this.doEnchantDamageEffects(this, entity);
//            }
//        }
    }

    private float rotWrap(double d) {
        return (float)MathHelper.wrapDegrees(d);
    }

    private boolean checkWalls(AABB aABB) {
        int i = Mth.floor(aABB.x0);
        int j = Mth.floor(aABB.y0);
        int k = Mth.floor(aABB.z0);
        int l = Mth.floor(aABB.x1);
        int m = Mth.floor(aABB.y1);
        int n = Mth.floor(aABB.z1);
        boolean bl = false;
        boolean bl2 = false;

        for(int o = i; o <= l; ++o) {
            for(int p = j; p <= m; ++p) {
                for(int q = k; q <= n; ++q) {
                    int blockState = this.level.getTile(o, p, q);
                    if (blockState != 0 && blockState != Tile.FIRE.id) {
                        if (!DRAGON_IMMUNE.contains(blockState)) {
//                            bl2 = this.level.removeBlock(blockPos, false) || bl2;
                        } else {
                            bl = true;
                        }
                    }
                }
            }
        }

        if (bl2) {
//            BlockPos blockPos2 = new BlockPos(i + this.random.nextInt(l - i + 1), j + this.random.nextInt(m - j + 1), k + this.random.nextInt(n - k + 1));
//            this.level.levelEvent(2008, blockPos2, 0);
        }

        return bl;
    }

//    public boolean hurt(EnderDragonPart enderDragonPart, DamageSource damageSource, float f) {
//        f = this.phaseManager.getCurrentPhase().onHurt(damageSource, f);
//        if (enderDragonPart != this.head) {
//            f = f / 4.0F + Math.min(f, 1.0F);
//        }
//
//        if (f < 0.01F) {
//            return false;
//        } else {
//            if (damageSource.getEntity() instanceof Player || damageSource.isExplosion()) {
//                float g = this.getHealth();
//                this.reallyHurt(damageSource, f);
//                if (this.getHealth() <= 0.0F && !this.phaseManager.getCurrentPhase().isSitting()) {
//                    this.setHealth(1.0F);
//                    this.phaseManager.setPhase(EnderDragonPhase.DYING);
//                }
//
//                if (this.phaseManager.getCurrentPhase().isSitting()) {
//                    this.sittingDamageReceived = (int)((float)this.sittingDamageReceived + (g - this.getHealth()));
//                    if ((float)this.sittingDamageReceived > 0.25F * this.getMaxHealth()) {
//                        this.sittingDamageReceived = 0;
//                        this.phaseManager.setPhase(EnderDragonPhase.TAKEOFF);
//                    }
//                }
//            }
//
//            return true;
//        }
//    }

//    @Override
//    public boolean hurt(DamageSource damageSource, float f) {
//        if (damageSource instanceof EntityDamageSource && ((EntityDamageSource)damageSource).isThorns()) {
//            this.hurt(this.body, damageSource, f);
//        }
//
//        return false;
//    }

//    protected boolean reallyHurt(DamageSource damageSource, float f) {
//        return super.hurt(damageSource, f);
//    }

//    @Override
//    public void kill() {
//        this.remove();
//        if (this.dragonFight != null) {
//            this.dragonFight.updateDragon(this);
//            this.dragonFight.setDragonKilled(this);
//        }
//    }

//    @Override
//    protected void tickDeath() {
//        if (this.dragonFight != null) {
//            this.dragonFight.updateDragon(this);
//        }
//
//        ++this.dragonDeathTime;
//        if (this.dragonDeathTime >= 180 && this.dragonDeathTime <= 200) {
//            float f = (this.random.nextFloat() - 0.5F) * 8.0F;
//            float g = (this.random.nextFloat() - 0.5F) * 4.0F;
//            float h = (this.random.nextFloat() - 0.5F) * 8.0F;
//            this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x + (double)f, this.y + 2.0 + (double)g, this.z + (double)h, 0.0, 0.0, 0.0);
//        }
//
//        boolean bl = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
//        int i = 500;
//        if (this.dragonFight != null && !this.dragonFight.hasPreviouslyKilledDragon()) {
//            i = 12000;
//        }
//
//        if (!this.level.isClientSide) {
//            if (this.dragonDeathTime > 150 && this.dragonDeathTime % 5 == 0 && bl) {
//                this.dropExperience(Mth.floor((float)i * 0.08F));
//            }
//
//            if (this.dragonDeathTime == 1) {
//                this.level.globalLevelEvent(1028, new BlockPos(this), 0);
//            }
//        }
//
//        this.move(MoverType.SELF, new Vec3(0.0, 0.1F, 0.0));
//        this.yRot += 20.0F;
//        this.yBodyRot = this.yRot;
//        if (this.dragonDeathTime == 200 && !this.level.isClientSide) {
//            if (bl) {
//                this.dropExperience(Mth.floor((float)i * 0.2F));
//            }
//
//            if (this.dragonFight != null) {
//                this.dragonFight.setDragonKilled(this);
//            }
//
//            this.remove();
//        }
//    }

//    private void dropExperience(int i) {
//        while(i > 0) {
//            int j = ExperienceOrb.getExperienceValue(i);
//            i -= j;
//            this.level.addFreshEntity(new ExperienceOrb(this.level, this.x, this.y, this.z, j));
//        }
//    }

    public int findClosestNode() {
        int seaLevel = 63;
        if (this.nodes[0] == null) {
            for(int i = 0; i < 24; ++i) {
                int j = 5;
                int l;
                int m;
                if (i < 12) {
                    l = Mth.floor(60.0F * Mth.cos(2.0F * ((float) -Math.PI + (float) (Math.PI / 12) * (float)i)));
                    m = Mth.floor(60.0F * Mth.sin(2.0F * ((float) -Math.PI + (float) (Math.PI / 12) * (float)i)));
                } else if (i < 20) {
                    int k = i - 12;
                    l = Mth.floor(40.0F * Mth.cos(2.0F * ((float) -Math.PI + (float) (Math.PI / 8) * (float)k)));
                    m = Mth.floor(40.0F * Mth.sin(2.0F * ((float) -Math.PI + (float) (Math.PI / 8) * (float)k)));
                    j += 10;
                } else {
                    int var7 = i - 20;
                    l = Mth.floor(20.0F * Mth.cos(2.0F * ((float) -Math.PI + (float) (Math.PI / 4) * (float)var7)));
                    m = Mth.floor(20.0F * Mth.sin(2.0F * ((float) -Math.PI + (float) (Math.PI / 4) * (float)var7)));
                }

                int n = seaLevel + 10;//Math.max(seaLevel + 10, this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(l, 0, m)).getY() + j);
                this.nodes[i] = new Node(l, n, m);
            }

            this.nodeAdjacency[0] = 6146;
            this.nodeAdjacency[1] = 8197;
            this.nodeAdjacency[2] = 8202;
            this.nodeAdjacency[3] = 16404;
            this.nodeAdjacency[4] = 32808;
            this.nodeAdjacency[5] = 32848;
            this.nodeAdjacency[6] = 65696;
            this.nodeAdjacency[7] = 131392;
            this.nodeAdjacency[8] = 131712;
            this.nodeAdjacency[9] = 263424;
            this.nodeAdjacency[10] = 526848;
            this.nodeAdjacency[11] = 525313;
            this.nodeAdjacency[12] = 1581057;
            this.nodeAdjacency[13] = 3166214;
            this.nodeAdjacency[14] = 2138120;
            this.nodeAdjacency[15] = 6373424;
            this.nodeAdjacency[16] = 4358208;
            this.nodeAdjacency[17] = 12910976;
            this.nodeAdjacency[18] = 9044480;
            this.nodeAdjacency[19] = 9706496;
            this.nodeAdjacency[20] = 15216640;
            this.nodeAdjacency[21] = 13688832;
            this.nodeAdjacency[22] = 11763712;
            this.nodeAdjacency[23] = 8257536;
        }

        return this.findClosestNode(this.x, this.y, this.z);
    }

    public int findClosestNode(double d, double e, double f) {
        float g = 10000.0F;
        int i = 0;
        Node node = new Node(Mth.floor(d), Mth.floor(e), Mth.floor(f));
        int j = 0;
        if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
            j = 12;
        }

        for(int k = j; k < 24; ++k) {
            if (this.nodes[k] != null) {
                float h = this.nodes[k].distanceToSqr(node);
                if (h < g) {
                    g = h;
                    i = k;
                }
            }
        }

        return i;
    }

    @Nullable
    public Path findPath(int i, int j, @Nullable Node node) {
        for(int k = 0; k < 24; ++k) {
            Node node2 = this.nodes[k];
            node2.closed = false;
            node2.f = 0.0F;
            node2.g = 0.0F;
            node2.h = 0.0F;
            node2.cameFrom = null;
            node2.heapIdx = -1;
        }

        Node node3 = this.nodes[i];
        Node node2 = this.nodes[j];
        node3.g = 0.0F;
        node3.h = node3.distanceTo(node2);
        node3.f = node3.h;
        this.openSet.clear();
        this.openSet.insert(node3);
        Node node4 = node3;
        int l = 0;
        if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
            l = 12;
        }

        while(!this.openSet.isEmpty()) {
            Node node5 = this.openSet.pop();
            if (node5.equals(node2)) {
                if (node != null) {
                    node.cameFrom = node2;
                    node2 = node;
                }

                return this.reconstructPath(node3, node2);
            }

            if (node5.distanceTo(node2) < node4.distanceTo(node2)) {
                node4 = node5;
            }

            node5.closed = true;
            int m = 0;

            for(int n = 0; n < 24; ++n) {
                if (this.nodes[n] == node5) {
                    m = n;
                    break;
                }
            }

            for(int n = l; n < 24; ++n) {
                if ((this.nodeAdjacency[m] & 1 << n) > 0) {
                    Node node6 = this.nodes[n];
                    if (!node6.closed) {
                        float f = node5.g + node5.distanceTo(node6);
                        if (!node6.inOpenSet() || f < node6.g) {
                            node6.cameFrom = node5;
                            node6.g = f;
                            node6.h = node6.distanceTo(node2);
                            if (node6.inOpenSet()) {
                                this.openSet.changeCost(node6, node6.g + node6.h);
                            } else {
                                node6.f = node6.g + node6.h;
                                this.openSet.insert(node6);
                            }
                        }
                    }
                }
            }
        }

        if (node4 == node3) {
            return null;
        } else {
            LOGGER.debug("Failed to find path from {} to {}", i, j);
            if (node != null) {
                node.cameFrom = node4;
                node4 = node;
            }

            return this.reconstructPath(node3, node4);
        }
    }

    private Path reconstructPath(Node node, Node node2) {
        List<Node> list = new ArrayList<>();
        Node node3 = node2;
        list.add(0, node2);

        while(node3.cameFrom != null) {
            node3 = node3.cameFrom;
            list.add(0, node3);
        }

        return new Path(list, new BlockPos(node2.x, node2.y, node2.z), true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("DragonPhase", this.phaseManager.getCurrentPhase().getPhase().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
//        if (compoundTag.contains("DragonPhase")) {
//            this.phaseManager.setPhase(EnderDragonPhase.getById(compoundTag.getInt("DragonPhase")));
//        }
    }

//    @Override
//    protected void checkDespawn() {
//    }

    public EnderDragonPart[] getSubEntities() {
        return this.subEntities;
    }

//    @Override
//    public boolean isPickable() {
//        return false;
//    }

//    @Override
//    public SoundSource getSoundSource() {
//        return SoundSource.HOSTILE;
//    }

//    @Override
//    protected SoundEvent getAmbientSound() {
//        return SoundEvents.ENDER_DRAGON_AMBIENT;
//    }
//
//    @Override
//    protected String getHurtSound(DamageSource damageSource) {
//        return SoundEvents.ENDER_DRAGON_HURT;
//    }

//    @Override
//    protected float getSoundVolume() {
//        return 5.0F;
//    }

//    @Environment(EnvType.CLIENT)
    public float getHeadPartYOffset(int i, double[] ds, double[] es) {
        DragonPhaseInstance dragonPhaseInstance = this.phaseManager.getCurrentPhase();
        EnderDragonPhase<? extends DragonPhaseInstance> enderDragonPhase = dragonPhaseInstance.getPhase();
        double d = /*remove this*/0;
        if (enderDragonPhase == EnderDragonPhase.LANDING || enderDragonPhase == EnderDragonPhase.TAKEOFF) {
//            BlockPos blockPos = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
//            float f = Math.max(Mth.sqrt(blockPos.distSqr(this.position(), true)) / 4.0F, 1.0F);
//            d = (double)((float)i / f);
        } else if (dragonPhaseInstance.isSitting()) {
            d = (double)i;
        } else if (i == 6) {
            d = 0.0;
        } else {
            d = es[1] - ds[1];
        }

        return (float)d;
    }

    public Vec3 getHeadLookVector(float f) {
        DragonPhaseInstance dragonPhaseInstance = this.phaseManager.getCurrentPhase();
        EnderDragonPhase<? extends DragonPhaseInstance> enderDragonPhase = dragonPhaseInstance.getPhase();
        Vec3 vec3 = null; // TODO THIS ISN'T NULL!
        if (enderDragonPhase == EnderDragonPhase.LANDING || enderDragonPhase == EnderDragonPhase.TAKEOFF) {
//            BlockPos blockPos = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
//            float g = Math.max(Mth.sqrt(blockPos.distSqr(this.position(), true)) / 4.0F, 1.0F);
//            float h = 6.0F / g;
//            float i = this.xRot;
//            float j = 1.5F;
//            this.xRot = -h * 1.5F * 5.0F;
//            vec3 = this.getViewVector(f);
//            this.xRot = i;
        } /*else if (dragonPhaseInstance.isSitting()) {
            float k = this.xRot;
            float g = 1.5F;
            this.xRot = -45.0F;
            vec3 = this.getViewVector(f);
            this.xRot = k;
        } else {
            vec3 = this.getViewVector(f);
        }*/

        return vec3;
    }

//    public void onCrystalDestroyed(EndCrystal endCrystal, BlockPos blockPos, DamageSource damageSource) {
//        Player player;
//        if (damageSource.getEntity() instanceof Player) {
//            player = (Player)damageSource.getEntity();
//        } else {
//            player = this.level.getNearestPlayer(CRYSTAL_DESTROY_TARGETING, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
//        }
//
//        if (endCrystal == this.nearestCrystal) {
//            this.hurt(this.head, DamageSource.explosion(player), 10.0F);
//        }
//
//        this.phaseManager.getCurrentPhase().onCrystalDestroyed(endCrystal, blockPos, damageSource, player);
//    }

//    @Override
//    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
//        if (DATA_PHASE.equals(entityDataAccessor) && this.level.isClientSide) {
//            this.phaseManager.setPhase(EnderDragonPhase.getById(this.getEntityData().get(DATA_PHASE)));
//        }
//
//        super.onSyncedDataUpdated(entityDataAccessor);
//    }

    public EnderDragonPhaseManager getPhaseManager() {
        return this.phaseManager;
    }

    @Nullable
    public EndDragonFight getDragonFight() {
        return this.dragonFight;
    }

//    @Override
//    public boolean addEffect(MobEffectInstance mobEffectInstance) {
//        return false;
//    }

//    @Override
//    protected boolean canRide(Entity entity) {
//        return false;
//    }

//    @Override
//    public boolean canChangeDimensions() {
//        return false;
//    }

    public static double getHorizontalDistanceSqr(Vec3 vec3) {
        return vec3.x * vec3.x + vec3.z * vec3.z;
    }

//    public final Vec3 getViewVector(float f) {
//        return this.calculateViewVector(this.getViewXRot(f), this.getViewYRot(f));
//    }

    public float getViewXRot(float f) {
        return f == 1.0F ? this.xRot : MathHelper.lerp(f, this.xRotO, this.xRot);
    }

    public float getViewYRot(float f) {
        return f == 1.0F ? this.yRot : MathHelper.lerp(f, this.yRotO, this.yRot);
    }

    protected final Vec3 calculateViewVector(float f, float g) {
        float h = f * (float) (Math.PI / 180.0);
        float i = -g * (float) (Math.PI / 180.0);
        float j = Mth.cos(i);
        float k = Mth.sin(i);
        float l = Mth.cos(h);
        float m = Mth.sin(h);
        return new Vec3((double)(k * l), (double)(-m), (double)(j * l));
    }

    public Random getRandom() {
        return this.random;
    }
}