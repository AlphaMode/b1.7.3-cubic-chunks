package me.alphamode.world;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import me.alphamode.util.Either;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ChunkTaskPriorityQueue<T> {
    public static final int PRIORITY_LEVEL_COUNT = 2;//ChunkMap.MAX_CHUNK_DISTANCE + 2;
    private final List<Object2ObjectLinkedOpenHashMap<CubicChunkPos, List<Optional<T>>>> taskQueue = IntStream.range(
                    0, PRIORITY_LEVEL_COUNT
            )
            .mapToObj(ix -> new Object2ObjectLinkedOpenHashMap<CubicChunkPos,List<Optional<T>>>())
            .collect(Collectors.toList());
    private volatile int firstQueue = PRIORITY_LEVEL_COUNT;
    private final String name;
    private final ObjectSet<CubicChunkPos> acquired = new ObjectOpenHashSet<>();
    private final int maxTasks;

    public ChunkTaskPriorityQueue(String string, int i) {
        this.name = string;
        this.maxTasks = i;
    }

    protected void resortChunkTasks(int level, CubicChunkPos chunkPos, int j) {
        if (level < PRIORITY_LEVEL_COUNT) {
            Object2ObjectLinkedOpenHashMap<CubicChunkPos, List<Optional<T>>> long2ObjectLinkedOpenHashMap = this.taskQueue.get(level);
            List<Optional<T>> list = long2ObjectLinkedOpenHashMap.remove(chunkPos);
            if (level == this.firstQueue) {
                while(this.firstQueue < PRIORITY_LEVEL_COUNT && this.taskQueue.get(this.firstQueue).isEmpty()) {
                    ++this.firstQueue;
                }
            }

            if (list != null && !list.isEmpty()) {
                ((List)((Object2ObjectLinkedOpenHashMap)this.taskQueue.get(j)).computeIfAbsent(chunkPos, l -> Lists.newArrayList())).addAll(list);
                this.firstQueue = Math.min(this.firstQueue, j);
            }
        }
    }

    protected void submit(Optional<T> optional, CubicChunkPos pos, int i) {
        this.taskQueue.get(i).computeIfAbsent(pos, lx -> Lists.newArrayList()).add(optional);
        this.firstQueue = Math.min(this.firstQueue, i);
    }

    protected void release(CubicChunkPos pos, boolean clearQueue) {
        for(Object2ObjectLinkedOpenHashMap<CubicChunkPos, List<Optional<T>>> long2ObjectLinkedOpenHashMap : this.taskQueue) {
            List<Optional<T>> queue = long2ObjectLinkedOpenHashMap.get(pos);
            if (queue != null) {
                if (clearQueue) {
                    queue.clear();
                } else {
                    queue.removeIf(Optional::isEmpty);
                }

                if (queue.isEmpty()) {
                    long2ObjectLinkedOpenHashMap.remove(pos);
                }
            }
        }

        while(this.firstQueue < PRIORITY_LEVEL_COUNT && this.taskQueue.get(this.firstQueue).isEmpty()) {
            ++this.firstQueue;
        }

        this.acquired.remove(pos);
    }

    private Runnable acquire(CubicChunkPos pos) {
        return () -> this.acquired.add(pos);
    }

    @Nullable
    public Stream<Either<T, Runnable>> pop() {
        if (this.acquired.size() >= this.maxTasks) {
            return null;
        } else if (this.firstQueue >= PRIORITY_LEVEL_COUNT) {
            return null;
        } else {
            int first = this.firstQueue;
            Object2ObjectLinkedOpenHashMap<CubicChunkPos, List<Optional<T>>> long2ObjectLinkedOpenHashMap = this.taskQueue.get(first);
            CubicChunkPos pos = long2ObjectLinkedOpenHashMap.firstKey();
            List<Optional<T>> list = long2ObjectLinkedOpenHashMap.removeFirst();

            while(this.firstQueue < PRIORITY_LEVEL_COUNT && this.taskQueue.get(this.firstQueue).isEmpty()) {
                ++this.firstQueue;
            }

            return list.stream().map(optional -> (Either)optional.map(Either::left).orElseGet(() -> Either.right(this.acquire(pos))));
        }
    }

    public String toString() {
        return this.name + " " + this.firstQueue + "...";
    }

    @VisibleForTesting
    ObjectSet<CubicChunkPos> getAcquired() {
        return new ObjectOpenHashSet<>(this.acquired);
    }
}