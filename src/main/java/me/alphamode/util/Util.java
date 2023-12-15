package me.alphamode.util;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import me.alphamode.MathHelper;
import me.alphamode.ReportedException;
import me.alphamode.wisp.loader.api.WispLoader;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Util {
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
    private static final ExecutorService BACKGROUND_EXECUTOR = makeBackgroundExecutor();
    private static final Logger LOGGER = LogManager.getLogger();

    public static <T extends Throwable> T pauseInIde(T throwable) {
        if (WispLoader.get().isDevelopment()) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", throwable);

            while (true) {
                try {
                    Thread.sleep(1000L);
                    LOGGER.error("paused");
                } catch (InterruptedException var2) {
                    return throwable;
                }
            }
        } else {
            return throwable;
        }
    }

    public static <V> CompletableFuture<List<V>> sequence(List<? extends CompletableFuture<? extends V>> list) {
        List<V> list2 = Lists.<V>newArrayListWithCapacity(list.size());
        CompletableFuture<?>[] completableFutures = new CompletableFuture[list.size()];
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        list.forEach(completableFuture2 -> {
            int i = list2.size();
            list2.add(null);
            completableFutures[i] = completableFuture2.whenComplete((object, throwable) -> {
                if (throwable != null) {
                    completableFuture.completeExceptionally(throwable);
                } else {
                    list2.set(i, object);
                }
            });
        });
        return CompletableFuture.allOf(completableFutures).applyToEither(completableFuture, void_ -> list2);
    }

    private static ExecutorService makeBackgroundExecutor() {
        int processors = MathHelper.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, 7);
        ExecutorService executorService;
        if (processors <= 0) {
            executorService = MoreExecutors.newDirectExecutorService();
        } else {
            executorService = new ForkJoinPool(processors, forkJoinPool -> {
                ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(forkJoinPool) {
                    protected void onTermination(Throwable throwable) {
                        if (throwable != null) {
                            Util.LOGGER.warn("{} died", this.getName(), throwable);
                        } else {
                            Util.LOGGER.debug("{} shutdown", this.getName());
                        }

                        super.onTermination(throwable);
                    }
                };
                forkJoinWorkerThread.setName("Server-Worker-" + WORKER_COUNT.getAndIncrement());
                return forkJoinWorkerThread;
            }, (thread, throwable) -> {
                pauseInIde(throwable);
                if (throwable instanceof CompletionException) {
                    throwable = throwable.getCause();
                }

                if (throwable instanceof ReportedException reportedException) {
                    System.out.println(reportedException.getReport());
                    System.exit(-1);
                }

                LOGGER.error(String.format("Caught exception in thread %s", thread), throwable);
            }, true);
        }

        return executorService;
    }

    public static Executor backgroundExecutor() {
        return BACKGROUND_EXECUTOR;
    }

    public static void shutdownBackgroundExecutor() {
        BACKGROUND_EXECUTOR.shutdown();

        boolean bl;
        try {
            bl = BACKGROUND_EXECUTOR.awaitTermination(3L, TimeUnit.SECONDS);
        } catch (InterruptedException var2) {
            bl = false;
        }

        if (!bl) {
            BACKGROUND_EXECUTOR.shutdownNow();
        }
    }
}
