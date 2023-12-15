package me.alphamode.world;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.alphamode.util.Either;
import me.alphamode.util.Unit;
import me.alphamode.util.Util;
import me.alphamode.util.thread.ProcessorHandle;
import me.alphamode.util.thread.ProcessorMailbox;
import me.alphamode.util.thread.StrictQueue;
import me.alphamode.world.chunk.ChunkHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkTaskPriorityQueueSorter implements AutoCloseable, ChunkHolder.LevelChangeListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<ProcessorHandle<?>, ChunkTaskPriorityQueue<? extends Function<ProcessorHandle<Unit>, ?>>> queues;
    private final Set<ProcessorHandle<?>> sleeping;
    private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;

    public ChunkTaskPriorityQueueSorter(List<ProcessorHandle<?>> list, Executor executor, int i) {
        this.queues = list.stream()
                .collect(Collectors.toMap(Function.identity(), processorHandle -> new ChunkTaskPriorityQueue<>(processorHandle.name() + "_queue", i)));
        this.sleeping = Sets.newHashSet(list);
        this.mailbox = new ProcessorMailbox<>(new StrictQueue.FixedPriorityQueue(4), executor, "sorter");
    }

    public static ChunkTaskPriorityQueueSorter.Message<Runnable> message(Runnable runnable, CubicChunkPos pos, IntSupplier level) {
        return new ChunkTaskPriorityQueueSorter.Message<>(processorHandle -> () -> {
            runnable.run();
            processorHandle.tell(Unit.INSTANCE);
        }, pos, level);
    }

    public static ChunkTaskPriorityQueueSorter.Message<Runnable> message(ChunkHolder chunkHolder, Runnable runnable) {
        return message(runnable, chunkHolder.getPos(), chunkHolder::getQueueLevel);
    }

    public static ChunkTaskPriorityQueueSorter.Release release(Runnable runnable, CubicChunkPos pos, boolean bl) {
        return new ChunkTaskPriorityQueueSorter.Release(runnable, pos, bl);
    }

    public <T> ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<T>> getProcessor(ProcessorHandle<T> processorHandle, boolean bl) {
        return (ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<T>>)this.mailbox
                .ask(
                        processorHandle2 -> new StrictQueue.IntRunnable(
                                0,
                                () -> {
                                    this.getQueue(processorHandle);
                                    processorHandle2.tell(
                                            ProcessorHandle.<ChunkTaskPriorityQueueSorter.Message<T>>of(
                                                    "chunk priority sorter around " + processorHandle.name(), message -> this.submit(processorHandle, message.task, message.pos, message.level, bl)
                                            )
                                    );
                                }
                        )
                )
                .join();
    }

    public ProcessorHandle<ChunkTaskPriorityQueueSorter.Release> getReleaseProcessor(ProcessorHandle<Runnable> processorHandle) {
        return (ProcessorHandle<ChunkTaskPriorityQueueSorter.Release>)this.mailbox
                .ask(
                        processorHandle2 -> new StrictQueue.IntRunnable(
                                0,
                                () -> processorHandle2.tell(
                                        ProcessorHandle.<ChunkTaskPriorityQueueSorter.Release>of(
                                                "chunk priority sorter around " + processorHandle.name(), release -> this.release(processorHandle, release.pos, release.task, release.clearQueue)
                                        )
                                )
                        )
                )
                .join();
    }

    @Override
    public void onLevelChange(CubicChunkPos chunkPos, IntSupplier intSupplier, int i, IntConsumer intConsumer) {
        this.mailbox.tell(new StrictQueue.IntRunnable(0, () -> {
            int level = intSupplier.getAsInt();
            this.queues.values().forEach(chunkTaskPriorityQueue -> chunkTaskPriorityQueue.resortChunkTasks(level, chunkPos, i));
            intConsumer.accept(i);
        }));
    }

    private <T> void release(ProcessorHandle<T> processorHandle, CubicChunkPos pos, Runnable task, boolean clearQueue) {
        this.mailbox.tell(new StrictQueue.IntRunnable(1, () -> {
            ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> chunkTaskPriorityQueue = this.getQueue(processorHandle);
            chunkTaskPriorityQueue.release(pos, clearQueue);
            if (this.sleeping.remove(processorHandle)) {
                this.pollTask(chunkTaskPriorityQueue, processorHandle);
            }

            task.run();
        }));
    }

    private <T> void submit(ProcessorHandle<T> processorHandle, Function<ProcessorHandle<Unit>, T> function, CubicChunkPos pos, IntSupplier levelSupplier, boolean bl) {
        this.mailbox.tell(new StrictQueue.IntRunnable(2, () -> {
            ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> chunkTaskPriorityQueue = this.getQueue(processorHandle);
            int level = levelSupplier.getAsInt();
            chunkTaskPriorityQueue.submit(Optional.of(function), pos, level);
            if (bl) {
                chunkTaskPriorityQueue.submit(Optional.empty(), pos, level);
            }

            if (this.sleeping.remove(processorHandle)) {
                this.pollTask(chunkTaskPriorityQueue, processorHandle);
            }
        }));
    }

    private <T> void pollTask(ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> chunkTaskPriorityQueue, ProcessorHandle<T> processorHandle) {
        this.mailbox.tell(new StrictQueue.IntRunnable(3, () -> {
            Stream<Either<Function<ProcessorHandle<Unit>, T>, Runnable>> stream = chunkTaskPriorityQueue.pop();
            if (stream == null) {
                this.sleeping.add(processorHandle);
            } else {
                Util.sequence(stream.map(either -> either.map(processorHandle::ask, runnable -> {
                    runnable.run();
                    return CompletableFuture.completedFuture(Unit.INSTANCE);
                })).collect(Collectors.toList())).thenAccept(list -> this.pollTask(chunkTaskPriorityQueue, processorHandle));
            }
        }));
    }

    private <T> ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> getQueue(ProcessorHandle<T> processorHandle) {
        ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> chunkTaskPriorityQueue = (ChunkTaskPriorityQueue)this.queues.get(processorHandle);
        if (chunkTaskPriorityQueue == null) {
            throw Util.pauseInIde(new IllegalArgumentException("No queue for: " + processorHandle));
        } else {
            return chunkTaskPriorityQueue;
        }
    }

    @VisibleForTesting
    public String getDebugStatus() {
        return this.queues
                .entrySet()
                .stream()
                .map(
                        entry -> entry.getKey().name()
                                + "=["
                                + entry.getValue()
                                .getAcquired()
                                .stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(","))
                                + "]"
                )
                .collect(Collectors.joining(","))
                + ", s="
                + this.sleeping.size();
    }

    public void close() {
        this.queues.keySet().forEach(ProcessorHandle::close);
    }

    public static final class Message<T> {
        private final Function<ProcessorHandle<Unit>, T> task;
        private final CubicChunkPos pos;
        private final IntSupplier level;

        private Message(Function<ProcessorHandle<Unit>, T> function, CubicChunkPos pos, IntSupplier level) {
            this.task = function;
            this.pos = pos;
            this.level = level;
        }
    }

    public static final class Release {
        private final Runnable task;
        private final CubicChunkPos pos;
        private final boolean clearQueue;

        private Release(Runnable runnable, CubicChunkPos pos, boolean bl) {
            this.task = runnable;
            this.pos = pos;
            this.clearQueue = bl;
        }
    }
}