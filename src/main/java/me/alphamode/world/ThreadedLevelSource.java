package me.alphamode.world;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import me.alphamode.ext.CubicSaveHandler;
import me.alphamode.util.Either;
import me.alphamode.util.Util;
import me.alphamode.util.thread.BlockableEventLoop;
import me.alphamode.world.chunk.ChunkHolder;
import me.alphamode.world.chunk.ChunkMap;
import me.alphamode.world.chunk.DistanceManager;
import me.alphamode.world.chunk.TicketType;
import net.minecraft.class_441;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.level.levelgen.LevelSource;
import net.minecraft.world.save.SaveHandler;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ThreadedLevelSource implements LevelSource {
    private final Level level;
    private final DistanceManager distanceManager;
    private final Thread mainThread;
    private final MainThreadExecutor mainThreadProcessor;
    private final ChunkMap chunkMap;
    private final CubicChunkPos[] lastChunkPos = new CubicChunkPos[8];
    private final Chunk[] lastChunk = new Chunk[8];
    private Object2ObjectMap<CubicChunkPos, Chunk> cache = new Object2ObjectLinkedOpenHashMap<>();
    private ObjectSet<CubicChunkPos> toDrop = new ObjectOpenHashSet<>();
    private Chunk emptyChunk;
    private List<Chunk> chunks = new ArrayList<>();
    private final SaveHandler saveHandler;
    private final LevelSource wrapped;


    public ThreadedLevelSource(Level level, SaveHandler saveHandler, LevelSource levelSource) {
        this.level = level;
        this.emptyChunk = new CubicEmptyChunk(this.level, 0, 0, 0);
        this.mainThread = Thread.currentThread();
        this.mainThreadProcessor = new MainThreadExecutor(level);
        this.saveHandler = saveHandler;
        this.wrapped = levelSource;
        this.chunkMap = new ChunkMap(level, null, Util.backgroundExecutor(), this.mainThreadProcessor, levelSource);
        this.distanceManager = this.chunkMap.getDistanceManager();
    }

    public ChunkMap getChunkMap() {
        return this.chunkMap;
    }

    @Nullable
    private ChunkHolder getVisibleChunkIfPresent(CubicChunkPos pos) {
        return this.chunkMap.getVisibleChunkIfPresent(pos);
    }

    @Override
    public boolean hasChunk(int x, int z) {
        return false;
    }

    @Override
    public Chunk getChunk(int x, int z) {
        return null;
    }

    @Override
    public Chunk loadChunk(int x, int z) {
        return null;
    }

    @Override
    public void generate(LevelSource levelSource, int x, int z) {

    }

    @Override
    public boolean method_74(boolean bl, class_441 arg) {
        return false;
    }

    @Override
    public boolean method_71() {
        return false;
    }

    @Override
    public boolean supportsSaving() {
        return true;
    }

    @Override
    public String getDebugInfo() {
        return "ThreadedLevelSource: " + getLoadedChunksCount();
    }

    public int getLoadedChunksCount() {
        return this.chunkMap.size();
    }

    @Override
    public boolean hasChunk(int x, int y, int z) {
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(new CubicChunkPos(x, y, z));
        return !this.chunkAbsent(chunkHolder);
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        Chunk chunk = this.cache.get(new CubicChunkPos(x, y, z));
        return chunk == null ? loadChunk(x, y, z) : chunk;
    }

    private void storeInCache(CubicChunkPos pos, Chunk chunk) {
        for(int i = 7; i > 0; --i) {
            this.lastChunkPos[i] = this.lastChunkPos[i - 1];
            this.lastChunk[i] = this.lastChunk[i - 1];
        }

        this.lastChunkPos[0] = pos;
        this.lastChunk[0] = chunk;
    }

    private boolean chunkAbsent(@Nullable ChunkHolder chunkHolder) {
        return chunkHolder == null;
    }

    private boolean runDistanceManagerUpdates() {
        boolean bl = this.distanceManager.runAllUpdates(this.chunkMap);
        boolean bl2 = this.chunkMap.promoteChunkMap();
        if (!bl && !bl2) {
            return false;
        } else {
            this.clearCache();
            return true;
        }
    }

    private CompletableFuture<Either<Chunk, ChunkHolder.ChunkLoadingFailure>> getChunkFutureMainThread(int x, int y, int z, boolean load) {
        CubicChunkPos pos = new CubicChunkPos(x, y, z);
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(pos);
        if (load) {
            this.distanceManager.addTicket(TicketType.UNKNOWN, pos, 0, pos);
            if (this.chunkAbsent(chunkHolder)) {
                this.runDistanceManagerUpdates();
                chunkHolder = this.getVisibleChunkIfPresent(pos);
                if (this.chunkAbsent(chunkHolder)) {
                    throw Util.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
                }
            }
        }

        return this.chunkAbsent(chunkHolder) ? ChunkHolder.UNLOADED_CHUNK_FUTURE : chunkHolder.getOrScheduleFuture(this.chunkMap);
    }

    private void clearCache() {
        Arrays.fill(this.lastChunkPos, null);
        Arrays.fill(this.lastChunk, null);
    }

    @Override
    public Chunk loadChunk(int x, int y, int z) {
        if (Thread.currentThread() != this.mainThread) {
            return CompletableFuture.supplyAsync(() -> this.getChunk(x, y, z), this.mainThreadProcessor).join();
        } else {
            CubicChunkPos pos = new CubicChunkPos(x, y, z);
            this.toDrop.remove(pos);
            Chunk foundChunk = this.cache.get(pos);
            if (foundChunk == null) {
                foundChunk = this.cubic_method_2231(x, y, z);
                if (foundChunk == null) {
                    if (this.wrapped == null) {
                        foundChunk = this.emptyChunk;
                    } else {
                        foundChunk = this.wrapped.getChunk(x, y, z);
                    }
                }

                this.cache.put(pos, foundChunk);
                this.chunks.add(foundChunk);
                if (foundChunk != null) {
                    foundChunk.method_641();
                    foundChunk.method_643();
                }

                if (!foundChunk.terrainPopulated &&
                        this.hasChunk(x + 1, y + 1, z + 1) && this.hasChunk(x, y + 1, z + 1) && this.hasChunk(x + 1, y + 1, z) &&
                        this.hasChunk(x + 1, y, z + 1) && this.hasChunk(x, y, z + 1) && this.hasChunk(x + 1, y, z)
                ) {
                    this.generate(this, x, y, z);
                }

                if (this.hasChunk(x - 1, y, z)
                        && !this.getChunk(x - 1, y, z).terrainPopulated
                        && this.hasChunk(x - 1, y, z + 1)
                        && this.hasChunk(x, y, z + 1)
                        && this.hasChunk(x - 1, y, z)
                        && this.hasChunk(x - 1, y + 1, z + 1)
                        && this.hasChunk(x, y + 1, z + 1)
                        && this.hasChunk(x - 1, y + 1, z)
                ) {
                    this.generate(this, x - 1, y, z);
                }

                if (this.hasChunk(x, y - 1, z)
                        && !this.getChunk(x, y - 1, z).terrainPopulated
                        && this.hasChunk(x + 1, y - 1, z)
                        && this.hasChunk(x, y - 1, z)
                        && this.hasChunk(x + 1, y, z)
                        && this.hasChunk(x + 1, y - 1, z - 1)
                        && this.hasChunk(x, y - 1, z - 1)
                        && this.hasChunk(x + 1, y, z + 1)
                ) {
                    this.generate(this, x, y - 1, z);
                }

                if (this.hasChunk(x, y, z - 1)
                        && !this.getChunk(x, y, z - 1).terrainPopulated
                        && this.hasChunk(x + 1, y, z - 1)
                        && this.hasChunk(x, y, z - 1)
                        && this.hasChunk(x + 1, y, z)
                        && this.hasChunk(x + 1, y - 1, z - 1)
                        && this.hasChunk(x, y - 1, z - 1)
                        && this.hasChunk(x + 1, y - 1, z)
                ) {
                    this.generate(this, x, y, z - 1);
                }

                if (this.hasChunk(x - 1, y, z - 1)
                        && !this.getChunk(x - 1, y, z - 1).terrainPopulated
                        && this.hasChunk(x - 1, y, z - 1)
                        && this.hasChunk(x, y, z - 1)
                        && this.hasChunk(x - 1, y, z)
                        && this.hasChunk(x - 1, y + 1, z - 1)
                        && this.hasChunk(x, y + 1, z - 1)
                        && this.hasChunk(x - 1, y + 1, z)
                ) {
                    this.generate(this, x - 1, y, z - 1);
                }

                if (this.hasChunk(x - 1, y - 1, z - 1)
                        && !this.getChunk(x - 1, y - 1, z - 1).terrainPopulated
                        && this.hasChunk(x - 1, y - 1, z - 1)
                        && this.hasChunk(x, y, z - 1)
                        && this.hasChunk(x - 1, y, z)
                        && this.hasChunk(x - 1, y - 1, z)
                        && this.hasChunk(x, y, z - 1)
                        && this.hasChunk(x - 1, y, z)
                ) {
                    this.generate(this, x - 1, y - 1, z - 1);
                }
            }

            return foundChunk;
        }
    }

    @Override
    public void generate(LevelSource chunkGenerator, int x, int y, int z) {

    }

    private Chunk cubic_method_2231(int x, int y, int z) {
        if (this.saveHandler == null) {
            return null;
        } else {
            try {
                Chunk var3 = ((CubicSaveHandler) this.saveHandler).getChunk(this.level, x, y, z);
                if (var3 != null) {
                    var3.field_858 = this.level.getTime();
                }

                return var3;
            } catch (Exception var4) {
                var4.printStackTrace();
                return null;
            }
        }
    }

    final class MainThreadExecutor extends BlockableEventLoop<Runnable> {
        private MainThreadExecutor(Level level) {
            super("Chunk source main thread executor for dimension: " + level.dimension.id);
        }

        @Override
        protected Runnable wrapRunnable(Runnable runnable) {
            return runnable;
        }

        @Override
        protected boolean shouldRun(Runnable runnable) {
            return true;
        }

        @Override
        protected boolean scheduleExecutables() {
            return true;
        }

        @Override
        protected Thread getRunningThread() {
            return ThreadedLevelSource.this.mainThread;
        }

        @Override
        protected boolean pollTask() {
            if (ThreadedLevelSource.this.runDistanceManagerUpdates()) {
                return true;
            } else {
//                ThreadedLevelSource.this.lightEngine.tryScheduleUpdate();
                return super.pollTask();
            }
        }
    }
}
