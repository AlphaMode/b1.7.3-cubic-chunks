package me.alphamode.world.chunk;

import java.util.Objects;

public final class Ticket<T> implements Comparable<Ticket<T>> {
    private final TicketType<T> type;
    private final int ticketLevel;
    private final T key;
    private final long createdTick;

    protected Ticket(TicketType<T> ticketType, int ticketLevel, T object, long l) {
        this.type = ticketType;
        this.ticketLevel = ticketLevel;
        this.key = object;
        this.createdTick = l;
    }

    public int compareTo(Ticket<T> ticket) {
        int compare = Integer.compare(this.ticketLevel, ticket.ticketLevel);
        if (compare != 0) {
            return compare;
        } else {
            int j = Integer.compare(System.identityHashCode(this.type), System.identityHashCode(ticket.type));
            return j != 0 ? j : this.type.getComparator().compare(this.key, ticket.key);
        }
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof Ticket)) {
            return false;
        } else {
            Ticket<?> ticket = (Ticket)object;
            return this.ticketLevel == ticket.ticketLevel && Objects.equals(this.type, ticket.type) && Objects.equals(this.key, ticket.key);
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.type, this.ticketLevel, this.key});
    }

    public String toString() {
        return "Ticket[" + this.type + " " + this.ticketLevel + " (" + this.key + ")] at " + this.createdTick;
    }

    public TicketType<T> getType() {
        return this.type;
    }

    public int getTicketLevel() {
        return this.ticketLevel;
    }

    public boolean timedOut(long l) {
        long m = this.type.timeout();
        return m != 0L && l - this.createdTick > m;
    }
}
