package me.alphamode.world;

public record CubicChunkPos(int x, int y, int z) implements Comparable<CubicChunkPos> {
    @Override
    public int compareTo(CubicChunkPos o) {
        if (this.y == o.y) {
            return this.z == o.z ? this.x - o.x : this.z - o.z;
        } else {
            return this.y - o.y;
        }
    }
}