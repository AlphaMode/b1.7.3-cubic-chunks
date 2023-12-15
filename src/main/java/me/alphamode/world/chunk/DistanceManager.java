package me.alphamode.world.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import me.alphamode.util.thread.ProcessorHandle;
import me.alphamode.world.ChunkTaskPriorityQueueSorter;
import me.alphamode.world.ChunkTracker;
import me.alphamode.world.CubicChunkPos;
import net.minecraft.world.entity.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.annotation.Nullable;

import java.util.Set;
import java.util.concurrent.Executor;

public abstract class DistanceManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Object2ObjectMap<CubicChunkPos, ObjectSet<Player>> playersPerChunk = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<CubicChunkPos, ObjectSortedSet<Ticket<?>>> tickets = new Object2ObjectOpenHashMap<>();
    private final DistanceManager.ChunkTicketTracker ticketTracker = new DistanceManager.ChunkTicketTracker();
    private final Set<ChunkHolder> chunksToUpdateFutures = Sets.<ChunkHolder>newHashSet();
    private final ChunkTaskPriorityQueueSorter ticketThrottler;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> ticketThrottlerInput;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Release> ticketThrottlerReleaser;
    private final ObjectSet<CubicChunkPos> ticketsToRelease = new ObjectOpenHashSet<>();
    private final Executor mainThreadExecutor;
    private long ticketTickCounter;

    protected DistanceManager(Executor executor, Executor executor2) {
        ProcessorHandle<Runnable> processorHandle = ProcessorHandle.of("player ticket throttler", executor2::execute);
        ChunkTaskPriorityQueueSorter chunkTaskPriorityQueueSorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(processorHandle), executor, 4);
        this.ticketThrottler = chunkTaskPriorityQueueSorter;
        this.ticketThrottlerInput = chunkTaskPriorityQueueSorter.getProcessor(processorHandle, true);
        this.ticketThrottlerReleaser = chunkTaskPriorityQueueSorter.getReleaseProcessor(processorHandle);
        this.mainThreadExecutor = executor2;
    }

    protected void purgeStaleTickets() {
        ++this.ticketTickCounter;
        ObjectIterator<Entry<CubicChunkPos, ObjectSortedSet<Ticket<?>>>> objectIterator = this.tickets.object2ObjectEntrySet().fastIterator();

        while(objectIterator.hasNext()) {
            Entry<CubicChunkPos, ObjectSortedSet<Ticket<?>>> entry = objectIterator.next();
            if (entry.getValue().removeIf(ticket -> ticket.timedOut(this.ticketTickCounter))) {
                this.ticketTracker.update(entry.getKey(), this.getTicketLevelAt(entry.getValue()), false);
            }

            if (entry.getValue().isEmpty()) {
                objectIterator.remove();
            }
        }
    }

    private int getTicketLevelAt(ObjectSortedSet<Ticket<?>> objectSortedSet) {
        ObjectBidirectionalIterator<Ticket<?>> objectBidirectionalIterator = objectSortedSet.iterator();
        return objectBidirectionalIterator.hasNext() ? objectBidirectionalIterator.next().getTicketLevel() : ChunkMap.MAX_CHUNK_DISTANCE + 1;
    }

    protected abstract boolean isChunkToRemove(CubicChunkPos pos);

    @Nullable
    protected abstract ChunkHolder getChunk(CubicChunkPos pos);

    @Nullable
    protected abstract ChunkHolder updateChunkScheduling(CubicChunkPos pos, int i, @Nullable ChunkHolder chunkHolder, int j);

    public boolean runAllUpdates(ChunkMap chunkMap) {
//        this.naturalSpawnChunkCounter.runAllUpdates();
//        this.playerTicketManager.runAllUpdates();
        int i = Integer.MAX_VALUE - this.ticketTracker.runDistnaceUpdates(Integer.MAX_VALUE);
        boolean bl = i != 0;
        if (bl) {
        }

        if (!this.chunksToUpdateFutures.isEmpty()) {
            this.chunksToUpdateFutures.forEach(chunkHolderx -> chunkHolderx.updateFutures(chunkMap));
            this.chunksToUpdateFutures.clear();
            return true;
        } else {
            if (!this.ticketsToRelease.isEmpty()) {
                ObjectIterator<CubicChunkPos> longIterator = this.ticketsToRelease.iterator();

                while(longIterator.hasNext()) {
                    CubicChunkPos l = longIterator.next();
                    if (this.getTickets(l).stream().anyMatch(ticket -> ticket.getType() == TicketType.PLAYER)) {
                        ChunkHolder chunkHolder = chunkMap.getUpdatingChunkIfPresent(l);
                        if (chunkHolder == null) {
                            throw new IllegalStateException();
                        }

//                        CompletableFuture<Either<Chunk, ChunkHolder.ChunkLoadingFailure>> completableFuture = chunkHolder.getEntityTickingChunkFuture();
//                        completableFuture.thenAccept(
//                                either -> this.mainThreadExecutor.execute(() -> this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
//                                }, l, false)))
//                        );
                    }
                }

                this.ticketsToRelease.clear();
            }

            return true;
        }
    }

    private void addTicket(CubicChunkPos pos, Ticket<?> ticket) {
        ObjectSortedSet<Ticket<?>> objectSortedSet = this.getTickets(pos);
        ObjectBidirectionalIterator<Ticket<?>> objectBidirectionalIterator = objectSortedSet.iterator();
        int i;
        if (objectBidirectionalIterator.hasNext()) {
            i = objectBidirectionalIterator.next().getTicketLevel();
        } else {
            i = ChunkMap.MAX_CHUNK_DISTANCE + 1;
        }

        if (objectSortedSet.add(ticket)) {
        }

        if (ticket.getTicketLevel() < i) {
            this.ticketTracker.update(pos, ticket.getTicketLevel(), true);
        }
    }

    private void removeTicket(CubicChunkPos pos, Ticket<?> ticket) {
        ObjectSortedSet<Ticket<?>> objectSortedSet = this.getTickets(pos);
        if (objectSortedSet.remove(ticket)) {
        }

        if (objectSortedSet.isEmpty()) {
            this.tickets.remove(pos);
        }

        this.ticketTracker.update(pos, this.getTicketLevelAt(objectSortedSet), false);
    }

    public <T> void addTicket(TicketType<T> ticketType, CubicChunkPos chunkPos, int ticketLevel, T object) {
        this.addTicket(chunkPos, new Ticket<>(ticketType, ticketLevel, object, this.ticketTickCounter));
    }

    public <T> void removeTicket(TicketType<T> ticketType, CubicChunkPos chunkPos, int i, T object) {
        Ticket<T> ticket = new Ticket<>(ticketType, i, object, this.ticketTickCounter);
        this.removeTicket(chunkPos, ticket);
    }

    public <T> void addRegionTicket(TicketType<T> ticketType, CubicChunkPos chunkPos, int i, T object) {
        this.addTicket(chunkPos, new Ticket<>(ticketType, 33 - i, object, this.ticketTickCounter));
    }

    public <T> void removeRegionTicket(TicketType<T> ticketType, CubicChunkPos chunkPos, int i, T object) {
        Ticket<T> ticket = new Ticket<>(ticketType, 33 - i, object, this.ticketTickCounter);
        this.removeTicket(chunkPos, ticket);
    }

    private ObjectSortedSet<Ticket<?>> getTickets(CubicChunkPos pos) {
        return this.tickets.computeIfAbsent(pos, lx -> new ObjectAVLTreeSet<>());
    }

    protected void updateChunkForced(CubicChunkPos chunkPos, boolean bl) {
        Ticket<CubicChunkPos> ticket = new Ticket<>(TicketType.FORCED, 31, chunkPos, this.ticketTickCounter);
        if (bl) {
            this.addTicket(chunkPos, ticket);
        } else {
            this.removeTicket(chunkPos, ticket);
        }
    }

    public void addPlayer(CubicChunkPos pos, Player serverPlayer) {
        this.playersPerChunk.computeIfAbsent(pos, lx -> new ObjectOpenHashSet<>()).add(serverPlayer);
    }

    public void removePlayer(CubicChunkPos pos, Player serverPlayer) {
        ObjectSet<Player> objectSet = this.playersPerChunk.get(pos);
        objectSet.remove(serverPlayer);
        if (objectSet.isEmpty()) {
            this.playersPerChunk.remove(pos);
//            this.naturalSpawnChunkCounter.update(pos, Integer.MAX_VALUE, false);
//            this.playerTicketManager.update(pos, Integer.MAX_VALUE, false);
        }
    }

    protected String getTicketDebugString(CubicChunkPos pos) {
        ObjectSortedSet<Ticket<?>> objectSortedSet = this.tickets.get(pos);
        String string;
        if (objectSortedSet != null && !objectSortedSet.isEmpty()) {
            string = objectSortedSet.first().toString();
        } else {
            string = "no_ticket";
        }

        return string;
    }

//    protected void updatePlayerTickets(int i) {
//        this.playerTicketManager.updateViewDistance(i);
//    }

//    public int getNaturalSpawnChunkCount() {
//        this.naturalSpawnChunkCounter.runAllUpdates();
//        return this.naturalSpawnChunkCounter.chunks.size();
//    }

//    public boolean hasPlayersNearby(CubicChunkPos pos) {
//        this.naturalSpawnChunkCounter.runAllUpdates();
//        return this.naturalSpawnChunkCounter.chunks.containsKey(pos);
//    }

    public String getDebugStatus() {
        return this.ticketThrottler.getDebugStatus();
    }

    class ChunkTicketTracker extends ChunkTracker {
        public ChunkTicketTracker() {
            super(ChunkMap.MAX_CHUNK_DISTANCE + 2, 16, 256);
        }

        @Override
        protected int getLevelFromSource(CubicChunkPos pos) {
            ObjectSortedSet<Ticket<?>> objectSortedSet = DistanceManager.this.tickets.get(pos);
            if (objectSortedSet == null) {
                return Integer.MAX_VALUE;
            } else {
                ObjectBidirectionalIterator<Ticket<?>> objectBidirectionalIterator = objectSortedSet.iterator();
                return !objectBidirectionalIterator.hasNext() ? Integer.MAX_VALUE : ((Ticket)objectBidirectionalIterator.next()).getTicketLevel();
            }
        }

        @Override
        protected int getLevel(CubicChunkPos pos) {
            if (!DistanceManager.this.isChunkToRemove(pos)) {
                ChunkHolder chunkHolder = DistanceManager.this.getChunk(pos);
                if (chunkHolder != null) {
                    return chunkHolder.getTicketLevel();
                }
            }

            return ChunkMap.MAX_CHUNK_DISTANCE + 1;
        }

        @Override
        protected void setLevel(CubicChunkPos pos, int level) {
            ChunkHolder chunkHolder = DistanceManager.this.getChunk(pos);
            int j = chunkHolder == null ? ChunkMap.MAX_CHUNK_DISTANCE + 1 : chunkHolder.getTicketLevel();
            if (j != level) {
                chunkHolder = DistanceManager.this.updateChunkScheduling(pos, level, chunkHolder, j);
                if (chunkHolder != null) {
                    DistanceManager.this.chunksToUpdateFutures.add(chunkHolder);
                }
            }
        }

        public int runDistnaceUpdates(int i) {
            return this.runUpdates(i);
        }
    }
}