package me.alphamode.mixin;

import me.alphamode.world.CubicChunkPos;
import net.minecraft.SpawnData;
import net.minecraft.Vec3i;
import net.minecraft.class_35;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.ChunkPos;
import net.minecraft.world.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Player;
import net.minecraft.world.level.levelgen.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Mixin(class_35.class)
public abstract class class_35Mixin {
    @Shadow private static Set<CubicChunkPos> field_105;

    @Shadow
    protected static boolean method_1382(MobCategory arg, Level level, int i, int j, int k) {
        return false;
    }

    @Shadow
    protected static void method_1383(LivingEntity livingEntity, Level level, float f, float g, float h) {
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
    public static final int method_1381(Level level, boolean bl, boolean bl2) {
        if (!bl && !bl2) {
            return 0;
        } else {
            field_105.clear();

            int var3;
            int z;
            int range;
            for(var3 = 0; var3 < level.players.size(); ++var3) {
                Player var4 = (Player)level.players.get(var3);
                int x = Mth.floor(var4.x / 16.0);
                int y = Mth.floor(var4.y / 16.0);
                z = Mth.floor(var4.z / 16.0);
                range = 8;

                for(int chunkX = -range; chunkX <= range; ++chunkX) {
                    for(int chunkY = -range; chunkY <= range; ++chunkY) {
                        for (int chunkZ = -range; chunkZ <= range; ++chunkZ) {
                            field_105.add(new CubicChunkPos(chunkX + x, chunkY + y, chunkZ + z));
                        }
                    }
                }
            }

            var3 = 0;
            Vec3i var35 = level.method_1599();
            MobCategory[] var36 = MobCategory.values();
            z = var36.length;

            label133:
            for(range = 0; range < z; ++range) {
                MobCategory var37 = var36[range];
                if ((!var37.isFriendly() || bl2) && (var37.isFriendly() || bl) && level.method_269(var37.getEntityClass()) <= var37.getMaxInstancesPerChunk() * field_105.size() / 256) {
                    Iterator var38 = field_105.iterator();

                    label130:
                    while(true) {
                        SpawnData var15;
                        int var18;
                        int var19;
                        int var41;
                        do {
                            do {
                                CubicChunkPos var10;
                                List var12;
                                do {
                                    do {
                                        if (!var38.hasNext()) {
                                            continue label133;
                                        }

                                        var10 = (CubicChunkPos)var38.next();
                                        Biome var11 = level.getBiomeProvider().method_1225(new ChunkPos(var10.x(), var10.z()));
                                        var12 = var11.method_564(var37);
                                    } while(var12 == null);
                                } while(var12.isEmpty());

                                int var13 = 0;

                                for(Iterator var14 = var12.iterator(); var14.hasNext(); var13 += var15.weight) {
                                    var15 = (SpawnData)var14.next();
                                }

                                int var39 = level.random.nextInt(var13);
                                var15 = (SpawnData)var12.get(0);
                                Iterator var16 = var12.iterator();

                                while(var16.hasNext()) {
                                    SpawnData var17 = (SpawnData)var16.next();
                                    var39 -= var17.weight;
                                    if (var39 < 0) {
                                        var15 = var17;
                                        break;
                                    }
                                }

                                BlockPos var40 = getRandomPos(level, var10.x() * 16, var10.y() * 16, var10.z() * 16);
                                var41 = var40.x;
                                var18 = var40.y;
                                var19 = var40.z;
                            } while(level.isViewBlocking(var41, var18, var19));
                        } while(level.getMaterial(var41, var18, var19) != var37.getMaterial());

                        int var20 = 0;

                        for(int var21 = 0; var21 < 3; ++var21) {
                            int var22 = var41;
                            int var23 = var18;
                            int var24 = var19;
                            byte var25 = 6;

                            for(int var26 = 0; var26 < 4; ++var26) {
                                var22 += level.random.nextInt(var25) - level.random.nextInt(var25);
                                var23 += level.random.nextInt(1) - level.random.nextInt(1);
                                var24 += level.random.nextInt(var25) - level.random.nextInt(var25);
                                if (method_1382(var37, level, var22, var23, var24)) {
                                    float var27 = (float)var22 + 0.5F;
                                    float var28 = (float)var23;
                                    float var29 = (float)var24 + 0.5F;
                                    if (level.method_210((double)var27, (double)var28, (double)var29, 24.0) == null) {
                                        float var30 = var27 - (float)var35.x;
                                        float var31 = var28 - (float)var35.y;
                                        float var32 = var29 - (float)var35.z;
                                        float var33 = var30 * var30 + var31 * var31 + var32 * var32;
                                        if (!(var33 < 576.0F)) {
                                            LivingEntity var42;
                                            try {
                                                var42 = (LivingEntity)var15.entity.getConstructor(Level.class).newInstance(level);
                                            } catch (Exception var34) {
                                                var34.printStackTrace();
                                                return var3;
                                            }

                                            var42.moveTo((double)var27, (double)var28, (double)var29, level.random.nextFloat() * 360.0F, 0.0F);
                                            if (var42.isDarkEnoughToSpawn()) {
                                                ++var20;
                                                level.addEntity(var42);
                                                method_1383(var42, level, var27, var28, var29);
                                                if (var20 >= var42.method_680()) {
                                                    continue label130;
                                                }
                                            }

                                            var3 += var20;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return var3;
        }
    }
}
