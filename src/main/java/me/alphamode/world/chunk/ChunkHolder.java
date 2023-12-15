package me.alphamode.world.chunk;

import me.alphamode.util.Either;
import me.alphamode.world.CubicChunkPos;
import net.minecraft.world.Chunk;

import java.util.concurrent.CompletableFuture;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ChunkHolder {
    public static final Either<Chunk, ChunkHolder.ChunkLoadingFailure> UNLOADED_CHUNK = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
    public static final CompletableFuture<Either<Chunk, ChunkLoadingFailure>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(
            UNLOADED_CHUNK
    );
    public static final Either<Chunk, ChunkLoadingFailure> UNLOADED_LEVEL_CHUNK = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
    private static final CompletableFuture<Either<Chunk, ChunkHolder.ChunkLoadingFailure>> UNLOADED_LEVEL_CHUNK_FUTURE = CompletableFuture.completedFuture(
            UNLOADED_LEVEL_CHUNK
    );

    public CompletableFuture<Either<Chunk, ChunkHolder.ChunkLoadingFailure>> future;
    private final CubicChunkPos pos;
    private final ChunkHolder.LevelChangeListener onLevelChange;
    private int oldTicketLevel;
    private int ticketLevel;
    private int queueLevel;

    public ChunkHolder(CubicChunkPos pos, ChunkHolder.LevelChangeListener levelChangeListener) {
        this.pos = pos;
        this.onLevelChange = levelChangeListener;
    }

    public CubicChunkPos getPos() {
        return this.pos;
    }

    public int getTicketLevel() {
        return this.ticketLevel;
    }

    public int getQueueLevel() {
        return this.queueLevel;
    }

    private void setQueueLevel(int i) {
        this.queueLevel = i;
    }

    public void setTicketLevel(int i) {
        this.ticketLevel = i;
    }

    protected void updateFutures(ChunkMap chunkMap) {
        Either<Chunk, ChunkHolder.ChunkLoadingFailure> either = Either.right(new ChunkHolder.ChunkLoadingFailure() {
            public String toString() {
                return "Unloaded ticket level " + ChunkHolder.this.pos.toString();
            }
        });

        CompletableFuture<Either<Chunk, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.future;
        if (completableFuture != null) {
            completableFuture.complete(either);
        } else {
            this.future = CompletableFuture.completedFuture(either);
        }


        this.onLevelChange.onLevelChange(this.pos, this::getQueueLevel, this.ticketLevel, this::setQueueLevel);
        this.oldTicketLevel = this.ticketLevel;
    }

    public CompletableFuture<Either<Chunk, ChunkHolder.ChunkLoadingFailure>> getOrScheduleFuture(ChunkMap chunkMap) {
        CompletableFuture<Either<Chunk, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.future;
        if (completableFuture != null) {
            Either<Chunk, ChunkHolder.ChunkLoadingFailure> either = completableFuture.getNow(null);
            if (either == null || either.left().isPresent()) {
                return completableFuture;
            }
        }

        CompletableFuture<Either<Chunk, ChunkHolder.ChunkLoadingFailure>> completableFuture2 = chunkMap.schedule(this);
//        this.updateChunkToSave(completableFuture2);
        this.future = completableFuture2;
        return completableFuture2;
    }

    public interface LevelChangeListener {
        void onLevelChange(CubicChunkPos chunkPos, IntSupplier intSupplier, int i, IntConsumer intConsumer);
    }

    public interface ChunkLoadingFailure {
        ChunkHolder.ChunkLoadingFailure UNLOADED = new ChunkHolder.ChunkLoadingFailure() {
            public String toString() {
                return "UNLOADED";
            }
        };
    }
}
