package me.alphamode.world.chunk;

public record CubicChunkPos(int x, int y, int z) implements Comparable<CubicChunkPos> {
    @Override
    public int compareTo(CubicChunkPos o) {
        if (this.y == o.y) {
            return this.z == o.z ? this.x - o.x : this.z - o.z;
        } else {
            return this.y - o.y;
        }
    }

    public long getSectionPos() {
        return SectionPos.toLong(x, z);
    }

    public boolean equals(int x, int y, int z) {
        return this.x == x && this.y == y && this.z == z;
    }
}