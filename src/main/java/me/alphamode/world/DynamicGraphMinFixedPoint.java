package me.alphamode.world;

import it.unimi.dsi.fastutil.objects.Object2ByteFunction;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import me.alphamode.MathHelper;

public abstract class DynamicGraphMinFixedPoint {
    private final int levelCount;
    private final ObjectLinkedOpenHashSet<CubicChunkPos>[] queues;
    private final Object2ByteFunction<CubicChunkPos> computedLevels;
    private int firstQueuedLevel;
    private volatile boolean hasWork;

    protected DynamicGraphMinFixedPoint(int levelCount, int j, int k) {
        if (levelCount >= 254) {
            throw new IllegalArgumentException("Level count must be < 254.");
        } else {
            this.levelCount = levelCount;
            this.queues = new ObjectLinkedOpenHashSet[levelCount];

            for(int l = 0; l < levelCount; ++l) {
                this.queues[l] = new ObjectLinkedOpenHashSet<>(j, 0.5F) {
                    @Override
                    protected void rehash(int i) {
                        if (i > j) {
                            super.rehash(i);
                        }
                    }
                };
            }

            this.computedLevels = new Object2ByteOpenHashMap<>(k, 0.5F) {
                @Override
                protected void rehash(int i) {
                    if (i > k) {
                        super.rehash(i);
                    }
                }
            };
            this.computedLevels.defaultReturnValue((byte)-1);
            this.firstQueuedLevel = levelCount;
        }
    }

    private int getKey(int i, int j) {
        int k = i;
        if (i > j) {
            k = j;
        }

        if (k > this.levelCount - 1) {
            k = this.levelCount - 1;
        }

        return k;
    }

    private void checkFirstQueuedLevel(int i) {
        int j = this.firstQueuedLevel;
        this.firstQueuedLevel = i;

        for(int k = j + 1; k < i; ++k) {
            if (!this.queues[k].isEmpty()) {
                this.firstQueuedLevel = k;
                break;
            }
        }
    }

    protected void removeFromQueue(CubicChunkPos pos) {
        int i = this.computedLevels.getByte(pos) & 255;
        if (i != 255) {
            int j = this.getLevel(pos);
            int k = this.getKey(j, i);
            this.dequeue(pos, k, this.levelCount, true);
            this.hasWork = this.firstQueuedLevel < this.levelCount;
        }
    }

    private void dequeue(CubicChunkPos pos, int i, int j, boolean bl) {
        if (bl) {
            this.computedLevels.removeByte(pos);
        }

        this.queues[i].remove(pos);
        if (this.queues[i].isEmpty() && this.firstQueuedLevel == i) {
            this.checkFirstQueuedLevel(j);
        }
    }

    private void enqueue(CubicChunkPos pos, int i, int j) {
        this.computedLevels.put(pos, (byte)i);
        this.queues[j].add(pos);
        if (this.firstQueuedLevel > j) {
            this.firstQueuedLevel = j;
        }
    }

    protected void checkNode(CubicChunkPos l) {
        this.checkEdge(l, l, this.levelCount - 1, false);
    }

    protected void checkEdge(CubicChunkPos l, CubicChunkPos m, int i, boolean bl) {
        this.checkEdge(l, m, i, this.getLevel(m), this.computedLevels.getByte(m) & 255, bl);
        this.hasWork = this.firstQueuedLevel < this.levelCount;
    }

    private void checkEdge(CubicChunkPos l, CubicChunkPos m, int i, int j, int k, boolean bl) {
        if (!this.isSource(m)) {
            i = MathHelper.clamp(i, 0, this.levelCount - 1);
            j = MathHelper.clamp(j, 0, this.levelCount - 1);
            boolean bl2;
            if (k == 255) {
                bl2 = true;
                k = j;
            } else {
                bl2 = false;
            }

            int n;
            if (bl) {
                n = Math.min(k, i);
            } else {
                n = MathHelper.clamp(this.getComputedLevel(m, l, i), 0, this.levelCount - 1);
            }

            int o = this.getKey(j, k);
            if (j != n) {
                int p = this.getKey(j, n);
                if (o != p && !bl2) {
                    this.dequeue(m, o, p, false);
                }

                this.enqueue(m, n, p);
            } else if (!bl2) {
                this.dequeue(m, o, this.levelCount, true);
            }
        }
    }

    protected final void checkNeighbor(CubicChunkPos l, CubicChunkPos m, int i, boolean bl) {
        int j = this.computedLevels.getByte(m) & 255;
        int k = MathHelper.clamp(this.computeLevelFromNeighbor(l, m, i), 0, this.levelCount - 1);
        if (bl) {
            this.checkEdge(l, m, k, this.getLevel(m), j, true);
        } else {
            int n;
            boolean bl2;
            if (j == 255) {
                bl2 = true;
                n = MathHelper.clamp(this.getLevel(m), 0, this.levelCount - 1);
            } else {
                n = j;
                bl2 = false;
            }

            if (k == n) {
                this.checkEdge(l, m, this.levelCount - 1, bl2 ? n : this.getLevel(m), j, false);
            }
        }
    }

    protected final boolean hasWork() {
        return this.hasWork;
    }

    protected final int runUpdates(int i) {
        if (this.firstQueuedLevel >= this.levelCount) {
            return i;
        } else {
            while(this.firstQueuedLevel < this.levelCount && i > 0) {
                --i;
                ObjectLinkedOpenHashSet<CubicChunkPos> longLinkedOpenHashSet = this.queues[this.firstQueuedLevel];
                CubicChunkPos l = longLinkedOpenHashSet.removeFirst();
                int j = MathHelper.clamp(this.getLevel(l), 0, this.levelCount - 1);
                if (longLinkedOpenHashSet.isEmpty()) {
                    this.checkFirstQueuedLevel(this.levelCount);
                }

                int k = this.computedLevels.removeByte(l) & 255;
                if (k < j) {
                    this.setLevel(l, k);
                    this.checkNeighborsAfterUpdate(l, k, true);
                } else if (k > j) {
                    this.enqueue(l, k, this.getKey(this.levelCount - 1, k));
                    this.setLevel(l, this.levelCount - 1);
                    this.checkNeighborsAfterUpdate(l, j, false);
                }
            }

            this.hasWork = this.firstQueuedLevel < this.levelCount;
            return i;
        }
    }

    protected abstract boolean isSource(CubicChunkPos l);

    protected abstract int getComputedLevel(CubicChunkPos l, CubicChunkPos m, int i);

    protected abstract void checkNeighborsAfterUpdate(CubicChunkPos pos, int i, boolean bl);

    protected abstract int getLevel(CubicChunkPos pos);

    protected abstract void setLevel(CubicChunkPos pos, int level);

    protected abstract int computeLevelFromNeighbor(CubicChunkPos l, CubicChunkPos m, int i);
}
