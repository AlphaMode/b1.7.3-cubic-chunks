package me.alphamode;

import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.HellRandomLevelSource;
import net.minecraft.world.level.levelgen.LevelSource;
import net.minecraft.world.level.levelgen.RandomLevelSource;
import net.minecraft.world.level.levelgen.SkyLevelSource;

public class StackedDimension extends Dimension {
    @Override
    public LevelSource getLevelSource() {
        return new FlatLevelSource(this.level);//new StackedLevelSource(this.level, new LevelSource[]{new AmplifiedRandomLevelSource(this.level, this.level.getSeed()), new SkyLevelSource(this.level, this.level.getSeed())/*, new TheEndLevelSource(this.level, this.level.getSeed()), new HellRandomLevelSource(this.level, this.level.getSeed())*/});
    }
}
