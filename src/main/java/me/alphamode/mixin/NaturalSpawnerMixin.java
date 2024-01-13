package me.alphamode.mixin;

import me.alphamode.world.chunk.CubicChunkPos;
import net.minecraft.SpawnData;
import net.minecraft.Vec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.ChunkPos;
import net.minecraft.world.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Player;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
    @Shadow private static Set<CubicChunkPos> chunks;

    @Shadow
    protected static boolean isSpawnPositionOk(MobCategory arg, Level level, int i, int j, int k) {
        return false;
    }

    @Shadow
    protected static void finalizeSpawn(LivingEntity livingEntity, Level level, float f, float g, float h) {
    }

    @Shadow
    protected static BlockPos getRandomPos(Level level, int i, int j) {
        return null;
    }

    private static BlockPos getRandomPos(Level level, int chunkX, int chunkY, int chunkZ) {
        int x = chunkX + level.random.nextInt(16);
        int y = chunkY + level.random.nextInt(16);
        int z = chunkZ + level.random.nextInt(16);
        return new BlockPos(x, y, z);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static final int spawnMobsAroundPlayer(Level level, boolean bl, boolean bl2) {
//        if (!bl && !bl2) {
//            return 0;
//        } else {
//            chunks.clear();
//
//            int var3;
//            int range;
//            for(var3 = 0; var3 < level.players.size(); ++var3) {
//                Player var4 = (Player)level.players.get(var3);
//                int x = Mth.floor(var4.x / 16.0);
//                int y = Mth.floor(var4.y / 16.0);
//                int z = Mth.floor(var4.z / 16.0);
//                range = 8;
//
//                for(int chunkX = -range; chunkX <= range; ++chunkX) {
//                    for(int chunkY = -range; chunkY <= range; ++chunkY) {
//                        for (int chunkZ = -range; chunkZ <= range; ++chunkZ) {
//                            chunks.add(new CubicChunkPos(chunkX + x, chunkY + y, chunkZ + z));
//                        }
//                    }
//                }
//            }
//
//            var3 = 0;
//            Vec3i var35 = level.getSpawnPos();
//            MobCategory[] categories = MobCategory.values();
//            int length = categories.length;
//
//            label133:
//            for(range = 0; range < length; ++range) {
//                MobCategory category = categories[range];
//                if ((!category.isFriendly() || bl2) && (category.isFriendly() || bl) && level.getMobCountForClass(category.getEntityClass()) <= category.getMaxInstancesPerChunk() * chunks.size() / 256) {
//                    Iterator var38 = chunks.iterator();
//
//                    label130:
//                    while(true) {
//                        SpawnData spawnData;
//                        int tileY;
//                        int tileZ;
//                        int tileX;
//                        do {
//                            do {
//                                CubicChunkPos var10;
//                                List<SpawnData> mobs;
//                                do {
//                                    do {
//                                        if (!var38.hasNext()) {
//                                            continue label133;
//                                        }
//
//                                        var10 = (CubicChunkPos)var38.next();
//                                        Biome biome = level.getBiomeProvider(var10.y()).getBiome(new ChunkPos(var10.x(), var10.z()));
//                                        mobs = biome.getMobs(category);
//                                    } while(mobs == null);
//                                } while(mobs.isEmpty());
//
//                                int var13 = 0;
//
//                                for(Iterator var14 = mobs.iterator(); var14.hasNext(); var13 += spawnData.weight) {
//                                    spawnData = (SpawnData)var14.next();
//                                }
//
//                                int var39 = level.random.nextInt(var13);
//                                spawnData = mobs.get(0);
//                                Iterator<SpawnData> var16 = mobs.iterator();
//
//                                while(var16.hasNext()) {
//                                    SpawnData var17 = (SpawnData)var16.next();
//                                    var39 -= var17.weight;
//                                    if (var39 < 0) {
//                                        spawnData = var17;
//                                        break;
//                                    }
//                                }
//
//                                BlockPos pos = getRandomPos(level, var10.x() * 16, var10.y() * 16, var10.z() * 16);
//                                tileX = pos.x;
//                                tileY = pos.y;
//                                tileZ = pos.z;
//                            } while(level.isViewBlocking(tileX, tileY, tileZ));
//                        } while(level.getMaterial(tileX, tileY, tileZ) != category.getMaterial());
//
//                        int var20 = 0;
//
//                        for(int var21 = 0; var21 < 3; ++var21) {
//                            int var22 = tileX;
//                            int var23 = tileY;
//                            int var24 = tileZ;
//                            byte var25 = 6;
//
//                            for(int var26 = 0; var26 < 4; ++var26) {
//                                var22 += level.random.nextInt(var25) - level.random.nextInt(var25);
//                                var23 += level.random.nextInt(1) - level.random.nextInt(1);
//                                var24 += level.random.nextInt(var25) - level.random.nextInt(var25);
//                                if (isSpawnPositionOk(category, level, var22, var23, var24)) {
//                                    float var27 = (float)var22 + 0.5F;
//                                    float var28 = (float)var23;
//                                    float var29 = (float)var24 + 0.5F;
//                                    if (level.getNearestPlayer((double)var27, (double)var28, (double)var29, 24.0) == null) {
//                                        float var30 = var27 - (float)var35.x;
//                                        float var31 = var28 - (float)var35.y;
//                                        float var32 = var29 - (float)var35.z;
//                                        float var33 = var30 * var30 + var31 * var31 + var32 * var32;
//                                        if (!(var33 < 576.0F)) {
//                                            LivingEntity entity;
//                                            try {
//                                                entity = (LivingEntity)spawnData.entity.getConstructor(Level.class).newInstance(level);
//                                            } catch (Exception var34) {
//                                                var34.printStackTrace();
//                                                return var3;
//                                            }
//
//                                            entity.moveTo((double)var27, (double)var28, (double)var29, level.random.nextFloat() * 360.0F, 0.0F);
//                                            if (entity.isDarkEnoughToSpawn()) {
//                                                ++var20;
//                                                level.addEntity(entity);
//                                                finalizeSpawn(entity, level, var27, var28, var29);
//                                                if (var20 >= entity.method_680()) {
//                                                    continue label130;
//                                                }
//                                            }
//
//                                            var3 += var20;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            return var3;
//        }
        return 0;
    }
}
