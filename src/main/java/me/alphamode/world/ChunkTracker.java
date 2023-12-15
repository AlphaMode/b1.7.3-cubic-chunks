package me.alphamode.world;

public abstract class ChunkTracker extends DynamicGraphMinFixedPoint {
    protected ChunkTracker(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected boolean isSource(CubicChunkPos pos) {
        return pos == null;
    }

    @Override
    protected void checkNeighborsAfterUpdate(CubicChunkPos chunkPos, int i, boolean bl) {
        int x = chunkPos.x();
        int y = chunkPos.y();
        int z = chunkPos.z();

        for(int xOff = -1; xOff <= 1; ++xOff) {
            for(int yOff = -1; yOff <= 1; ++yOff) {
                for (int zOff = -1; zOff <= 1; ++zOff) {
                    CubicChunkPos o = new CubicChunkPos(x + xOff, y + yOff, z + zOff);
                    if (!o.equals(chunkPos)) {
                        this.checkNeighbor(chunkPos, o, i, bl);
                    }
                }
            }
        }
    }

    @Override
    protected int getComputedLevel(CubicChunkPos pos1, CubicChunkPos pos2, int i) {
        int j = i;
        int x = pos1.x();
        int y = pos1.y();
        int z = pos1.z();

        for(int xOff = -1; xOff <= 1; ++xOff) {
            for(int yOff = -1; yOff <= 1; ++yOff) {
                for (int zOff = -1; zOff <= 1; ++zOff) {
                    CubicChunkPos q = new CubicChunkPos(x + xOff, y + yOff, z + zOff);
                    if (q.equals(pos1)) {
                        q = null;
                    }

                    if (q != pos2) {
                        int r = this.computeLevelFromNeighbor(q, pos1, this.getLevel(q));
                        if (j > r) {
                            j = r;
                        }

                        if (j == 0) {
                            return j;
                        }
                    }
                }
            }
        }

        return j;
    }

    @Override
    protected int computeLevelFromNeighbor(CubicChunkPos l, CubicChunkPos m, int i) {
        return l == null ? this.getLevelFromSource(m) : i + 1;
    }

    protected abstract int getLevelFromSource(CubicChunkPos pos);

    public void update(CubicChunkPos l, int i, boolean bl) {
        this.checkEdge(null, l, i, bl);
    }
}