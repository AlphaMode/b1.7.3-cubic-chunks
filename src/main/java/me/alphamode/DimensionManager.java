package me.alphamode;

import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.NetherDimension;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.dimension.SkyDimension;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DimensionManager {
    public static final Map<Integer, Supplier<Dimension>> DIMENSION_FACTORY = new HashMap<>();

    static {
        DIMENSION_FACTORY.put(-1, NetherDimension::new);
        DIMENSION_FACTORY.put(0, StackedDimension::new);
    }
}
