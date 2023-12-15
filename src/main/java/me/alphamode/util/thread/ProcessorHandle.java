package me.alphamode.util.thread;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ProcessorHandle<Msg> extends AutoCloseable {
    String name();

    void tell(Msg object);

    default void close() {
    }

    default <Source> CompletableFuture<Source> ask(Function<? super ProcessorHandle<Source>, ? extends Msg> function) {
        CompletableFuture<Source> completableFuture = new CompletableFuture<>();
        Msg msg = function.apply(of("ask future procesor handle", completableFuture::complete));
        this.tell(msg);
        return completableFuture;
    }

    static <Msg> ProcessorHandle<Msg> of(String string, Consumer<Msg> consumer) {
        return new ProcessorHandle<Msg>() {
            @Override
            public String name() {
                return string;
            }

            @Override
            public void tell(Msg object) {
                consumer.accept(object);
            }

            public String toString() {
                return string;
            }
        };
    }
}
