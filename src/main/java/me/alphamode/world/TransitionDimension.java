package me.alphamode.world;

import me.alphamode.world.levelgen.BetweenLevelSource;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.levelgen.LevelSource;

public class TransitionDimension extends Dimension {
    private final int tile;
    private final boolean generateCaves;

    public TransitionDimension(int tile, boolean generateCaves) {
        this.tile = tile;
        this.generateCaves = generateCaves;
    }

    @Override
    public LevelSource getLevelSource() {
        return new BetweenLevelSource(this.level, this.level.getSeed(), tile, generateCaves);
    }
}
