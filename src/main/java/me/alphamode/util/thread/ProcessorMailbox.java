package me.alphamode.util.thread;

import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessorMailbox<T> implements ProcessorHandle<T>, AutoCloseable, Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final AtomicInteger status = new AtomicInteger(0);
    public final StrictQueue<? super T, ? extends Runnable> queue;
    private final Executor dispatcher;
    private final String name;

    public static ProcessorMailbox<Runnable> create(Executor dispatcher, String name) {
        return new ProcessorMailbox<>(new StrictQueue.QueueStrictQueue<>(new ConcurrentLinkedQueue<>()), dispatcher, name);
    }

    public ProcessorMailbox(StrictQueue<? super T, ? extends Runnable> queue, Executor dispatcher, String name) {
        this.dispatcher = dispatcher;
        this.queue = queue;
        this.name = name;
    }

    private boolean setAsScheduled() {
        int status;
        do {
            status = this.status.get();
            if ((status & 3) != 0) {
                return false;
            }
        } while(!this.status.compareAndSet(status, status | 2));

        return true;
    }

    private void setAsIdle() {
        int i;
        do {
            i = this.status.get();
        } while(!this.status.compareAndSet(i, i & -3));
    }

    private boolean canBeScheduled() {
        if ((this.status.get() & 1) != 0) {
            return false;
        } else {
            return !this.queue.isEmpty();
        }
    }

    @Override
    public void close() {
        int i;
        do {
            i = this.status.get();
        } while(!this.status.compareAndSet(i, i | 1));
    }

    private boolean shouldProcess() {
        return (this.status.get() & 2) != 0;
    }

    private boolean pollTask() {
        if (!this.shouldProcess()) {
            return false;
        } else {
            Runnable runnable = this.queue.pop();
            if (runnable == null) {
                return false;
            } else {
                runnable.run();
                return true;
            }
        }
    }

    public void run() {
        try {
            this.pollUntil(i -> i == 0);
        } finally {
            this.setAsIdle();
            this.registerForExecution();
        }
    }

    @Override
    public void tell(T object) {
        this.queue.push(object);
        this.registerForExecution();
    }

    private void registerForExecution() {
        if (this.canBeScheduled() && this.setAsScheduled()) {
            try {
                this.dispatcher.execute(this);
            } catch (RejectedExecutionException var4) {
                try {
                    this.dispatcher.execute(this);
                } catch (RejectedExecutionException var3) {
                    LOGGER.error("Cound not schedule mailbox", var3);
                }
            }
        }
    }

    private int pollUntil(Int2BooleanFunction int2BooleanFunction) {
        int i = 0;

        while(int2BooleanFunction.get(i) && this.pollTask()) {
            ++i;
        }

        return i;
    }

    public String toString() {
        return this.name + " " + this.status.get() + " " + this.queue.isEmpty();
    }

    @Override
    public String name() {
        return this.name;
    }
}