package me.alphamode;

import me.alphamode.ext.DimensionExt;
import me.alphamode.fight.EndDragonFight;
import net.minecraft.NoiseBiomeProvider;
import net.minecraft.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.levelgen.LevelSource;
import net.minecraft.world.level.levelgen.biome.Biome;
import net.minecraft.world.tile.Tile;
import javax.annotation.Nullable;

public class TheEndDimension extends Dimension implements DimensionExt {
    private EndDragonFight dragonFight;

    public static void breakpointME() {
//        System.out.println("OH NO");
//        Minecraft.field_3339 = System.currentTimeMillis();
    }

    @Override
    public void setup() {
        this.biomeProvider = new NoiseBiomeProvider(Biome.SKY, 0.5, 0.0);
        this.id = 1;
        this.natural = true;

        this.dragonFight = new EndDragonFight(level, /*compoundTag.getCompound("DragonFight")*/new CompoundTag());
    }

    @Override
    public float getTimeOfDay(long l, float f) {
        return 0.0F;
    }

    @Override
    public float[] getSunriseColor(float f, float g) {
        return null;
    }

    @Override
    public Vec3 getFogColor(float f, float g) {
        int i = 10518688;
        float h = Mth.cos(f * (float) (Math.PI * 2)) * 2.0F + 0.5F;
        h = MathHelper.clamp(h, 0.0F, 1.0F);
        float j = 0.627451F;
        float k = 0.5019608F;
        float l = 0.627451F;
        j *= h * 0.0F + 0.15F;
        k *= h * 0.0F + 0.15F;
        l *= h * 0.0F + 0.15F;
        return Vec3.newTemp(j, k, l);
    }

    @Override
    public float getCloudHeight() {
        return 8.0F;
    }

    @Override
    public LevelSource getLevelSource() {
        return new TheEndLevelSource(this.level, this.level.getSeed());
    }

    @Override
    public boolean method_1205(int i, int j) {
        int var3 = this.level.getTopBlockState(i, j);
        return var3 == 0 ? false : Tile.tiles[var3].material.method_651();
    }

    @Override
    public void tick() {
        this.dragonFight.tick();
    }

    @Nullable
    public EndDragonFight getDragonFight() {
        return this.dragonFight;
    }
}
