package me.alphamode.mixin;

import me.alphamode.DimensionManager;
import me.alphamode.FlatLevelSource;
import me.alphamode.StackedLevelSource;
import me.alphamode.ext.DimensionExt;
import net.minecraft.world.Level;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.levelgen.LevelSource;
import net.minecraft.world.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(Dimension.class)
public class DimensionMixin implements DimensionExt {
    @Shadow public Level level;
    private static final Random RANDOM = new Random();
    /**
     * @author
     * @reason
     */
    @Overwrite
    public static Dimension getDimensionFromId(int id) {
        return DimensionManager.DIMENSION_FACTORY.get(id).get();
    }

//    /**
//     * @author
//     * @reason
//     */
//    @Overwrite
//    public LevelSource getLevelSource() {
//        return new FlatLevelSource(this.level);
//    }

    @Override
    public boolean cubic_isValidSpawnPosition(int x, int y, int z) {
        int var3 = this.level.getTopTile(x, z);
        return var3 == Tile.SAND.id;
    }
}
