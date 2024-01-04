package me.alphamode.ext;

public interface DimensionExt {
    default boolean cubic_isValidSpawnPosition(int x, int y, int z) {
        return false;
    }
    default void tick() {}
}
