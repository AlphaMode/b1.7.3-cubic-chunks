package me.alphamode;

import me.alphamode.world.TransitionDimension;
import me.alphamode.world.levelgen.BetweenLevelSource;
import net.minecraft.world.Level;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.NetherDimension;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.dimension.SkyDimension;
import net.minecraft.world.gen.HellRandomLevelSource;
import net.minecraft.world.level.levelgen.LevelSource;
import net.minecraft.world.level.levelgen.RandomLevelSource;
import net.minecraft.world.level.levelgen.SkyLevelSource;
import net.minecraft.world.tile.Tile;

import java.util.Map;

public class StackedDimension extends Dimension {
    @Override
    public LevelSource getLevelSource() {
        return new StackedLevelSource(level, Map.of(
                -2, new NetherDimension(),
                -1, new TransitionDimension(Tile.STONE.id, true),
                0, new OverworldDimension(),
                1, new TransitionDimension(0, false),
                2, new SkyDimension()
        ));
    }
}
