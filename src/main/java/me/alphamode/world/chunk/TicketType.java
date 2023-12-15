package me.alphamode.world.chunk;

import java.util.Comparator;

import me.alphamode.util.Unit;
import me.alphamode.util.Vec3i;
import me.alphamode.world.CubicChunkPos;
import net.minecraft.core.BlockPos;

public class TicketType<T> {
    private final String name;
    private final Comparator<T> comparator;
    private final long timeout;
    public static final TicketType<Unit> START = create("start", (unit, unit2) -> 0);
    public static final TicketType<Unit> DRAGON = create("dragon", (unit, unit2) -> 0);
    public static final TicketType<CubicChunkPos> PLAYER = create("player", CubicChunkPos::compareTo);
    public static final TicketType<CubicChunkPos> FORCED = create("forced", CubicChunkPos::compareTo);
    public static final TicketType<CubicChunkPos> LIGHT = create("light", CubicChunkPos::compareTo);
    public static final TicketType<Vec3i> PORTAL = create("portal", Vec3i::compareTo, 300);
    public static final TicketType<Integer> POST_TELEPORT = create("post_teleport", Integer::compareTo, 5);
    public static final TicketType<CubicChunkPos> UNKNOWN = create("unknown", CubicChunkPos::compareTo, 1);

    public static <T> TicketType<T> create(String string, Comparator<T> comparator) {
        return new TicketType<>(string, comparator, 0L);
    }

    public static <T> TicketType<T> create(String string, Comparator<T> comparator, int i) {
        return new TicketType<>(string, comparator, (long)i);
    }

    protected TicketType(String string, Comparator<T> comparator, long l) {
        this.name = string;
        this.comparator = comparator;
        this.timeout = l;
    }

    public String toString() {
        return this.name;
    }

    public Comparator<T> getComparator() {
        return this.comparator;
    }

    public long timeout() {
        return this.timeout;
    }
}