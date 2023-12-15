package me.alphamode.world.chunk;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import me.alphamode.ReportedException;
import me.alphamode.util.Either;
import me.alphamode.util.thread.BlockableEventLoop;
import me.alphamode.util.thread.ProcessorHandle;
import me.alphamode.util.thread.ProcessorMailbox;
import me.alphamode.world.ChunkTaskPriorityQueueSorter;
import me.alphamode.world.CubicChunk;
import me.alphamode.world.CubicChunkPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.level.levelgen.LevelSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ChunkMap {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int MAX_CHUNK_DISTANCE = 33;
    private final Object2ObjectLinkedOpenHashMap<CubicChunkPos, ChunkHolder> updatingChunkMap = new Object2ObjectLinkedOpenHashMap<>();
    private volatile Object2ObjectLinkedOpenHashMap<CubicChunkPos, ChunkHolder> visibleChunkMap = this.updatingChunkMap.clone();
    private final Object2ObjectLinkedOpenHashMap<CubicChunkPos, ChunkHolder> pendingUnloads = new Object2ObjectLinkedOpenHashMap<>();
    private final BlockableEventLoop<Runnable> mainThreadExecutor;
    private final ChunkTaskPriorityQueueSorter queueSorter;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> worldgenMailbox;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;
    private final ObjectSet<CubicChunkPos> toDrop = new ObjectOpenHashSet<>();
    private final ChunkMap.DistanceManager distanceManager;
    private boolean modified;

    private final Level level;
    private final LevelSource generator;

    public ChunkMap(
            Level level,
            File file,
            Executor executor,
            BlockableEventLoop<Runnable> blockableEventLoop,
            LevelSource chunkGenerator
    ) {
        this.level = level;
        this.generator = chunkGenerator;
        this.mainThreadExecutor = blockableEventLoop;
        ProcessorMailbox<Runnable> processorMailbox = ProcessorMailbox.create(executor, "worldgen");
        ProcessorHandle<Runnable> processorHandle = ProcessorHandle.of("main", blockableEventLoop::tell);
        ProcessorMailbox<Runnable> processorMailbox2 = ProcessorMailbox.create(executor, "light");
        this.queueSorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(processorMailbox, processorHandle, processorMailbox2), executor, Integer.MAX_VALUE);
        this.worldgenMailbox = this.queueSorter.getProcessor(processorMailbox, false);
        this.mainThreadMailbox = this.queueSorter.getProcessor(processorHandle, false);
        this.distanceManager = new ChunkMap.DistanceManager(executor, blockableEventLoop);
    }

    @Nullable
    public ChunkHolder getVisibleChunkIfPresent(CubicChunkPos pos) {
        return this.visibleChunkMap.get(pos);
    }

    @Nullable
    protected ChunkHolder getUpdatingChunkIfPresent(CubicChunkPos pos) {
        return this.updatingChunkMap.get(pos);
    }

    public boolean promoteChunkMap() {
        if (!this.modified) {
            return false;
        } else {
            this.visibleChunkMap = this.updatingChunkMap.clone();
            this.modified = false;
            return true;
        }
    }

    public CompletableFuture<Either<Chunk, ChunkHolder.ChunkLoadingFailure>> schedule(ChunkHolder chunkHolder) {
        return scheduleChunkGeneration(chunkHolder);//this.scheduleChunkLoad(chunkHolder.getPos());
    }

    private CompletableFuture<Either<Chunk, ChunkHolder.ChunkLoadingFailure>> scheduleChunkLoad(CubicChunkPos chunkPos) {
        return CompletableFuture.supplyAsync(() -> {
            try {
//                CompoundTag compoundTag = this.readChunk(chunkPos);
//                if (compoundTag != null) {
//                    boolean bl = compoundTag.contains("Level", 10) && compoundTag.getCompound("Level").contains("Status", 8);
//                    if (bl) {
//                        ChunkAccess chunkAccess = ChunkSerializer.read(this.level, this.structureManager, this.poiManager, chunkPos, compoundTag);
//                        chunkAccess.setLastSaveTime(this.level.getGameTime());
//                        return Either.left(chunkAccess);
//                    }
//
//                    LOGGER.error("Chunk file at {} is missing level data, skipping", chunkPos);
//                }
            } catch (ReportedException var5) {
                Throwable throwable = var5.getCause();
                if (!(throwable instanceof IOException)) {
                    throw var5;
                }

                LOGGER.error("Couldn't load chunk {}", chunkPos, throwable);
            } catch (Exception var6) {
                LOGGER.error("Couldn't load chunk {}", chunkPos, var6);
            }

            return Either.left(new CubicChunk(this.level, new byte[32768], chunkPos.x(), chunkPos.y(), chunkPos.z()));
        }, this.mainThreadExecutor);
    }

    private CompletableFuture<Either<Chunk, ChunkHolder.ChunkLoadingFailure>> scheduleChunkGeneration(ChunkHolder chunkHolder) {
        CubicChunkPos chunkPos = chunkHolder.getPos();
        return chunkHolder.future.thenComposeAsync(
                either -> either.map(
                        list -> CompletableFuture.completedFuture(Either.left(this.generator.getChunk(chunkPos.x(), chunkPos.y(), chunkPos.z()))),
                        chunkLoadingFailure -> {
//                            this.releaseLightTicket(chunkPos);
                            return CompletableFuture.completedFuture(Either.right(chunkLoadingFailure));
                        }
                ),
                runnable -> this.worldgenMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkHolder, runnable))
        );
    }

    @Nullable
    private ChunkHolder updateChunkScheduling(CubicChunkPos pos, int i, @Nullable ChunkHolder chunkHolder, int j) {
        if (j > MAX_CHUNK_DISTANCE && i > MAX_CHUNK_DISTANCE) {
            return chunkHolder;
        } else {
            if (chunkHolder != null) {
                chunkHolder.setTicketLevel(i);
            }

            if (chunkHolder != null) {
                if (i > MAX_CHUNK_DISTANCE) {
                    this.toDrop.add(pos);
                } else {
                    this.toDrop.remove(pos);
                }
            }

            if (i <= MAX_CHUNK_DISTANCE && chunkHolder == null) {
                chunkHolder = this.pendingUnloads.remove(pos);
                if (chunkHolder != null) {
                    chunkHolder.setTicketLevel(i);
                } else {
                    chunkHolder = new ChunkHolder(pos, this.queueSorter);
                }

                this.updatingChunkMap.put(pos, chunkHolder);
                this.modified = true;
            }

            return chunkHolder;
        }
    }

    public int size() {
        return this.visibleChunkMap.size();
    }

    public DistanceManager getDistanceManager() {
        return this.distanceManager;
    }

    class DistanceManager extends me.alphamode.world.chunk.DistanceManager {
        protected DistanceManager(Executor executor, Executor executor2) {
            super(executor, executor2);
        }

        @Override
        protected boolean isChunkToRemove(CubicChunkPos pos) {
            return ChunkMap.this.toDrop.contains(pos);
        }

        @Nullable
        @Override
        protected ChunkHolder getChunk(CubicChunkPos pos) {
            return ChunkMap.this.getUpdatingChunkIfPresent(pos);
        }

        @Nullable
        @Override
        protected ChunkHolder updateChunkScheduling(CubicChunkPos pos, int i, @Nullable ChunkHolder chunkHolder, int j) {
            return ChunkMap.this.updateChunkScheduling(pos, i, chunkHolder, j);
        }
    }
}
