package org.composer.core.utils;

import org.example.common.utils.FourConsumer;
import org.example.common.utils.TriFunction;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;

import java.util.function.BiConsumer;
import java.util.function.Function;


public class AsyncThreadTask<IT,RT> implements Runnable{
   private final FourConsumer<IT, TriFunction<Exchange,
                     BiConsumer<Exchange,RT>,
                     AsyncCallback,
                     StreamObserver<RT>>,
                  AsyncCallback,
                 Metadata> runCallback;
   private final Function<Exchange,IT> getArgumentFromExchange;
   private final AsyncCallback camelCallback;
   private final Function<Exchange, Metadata> getMetadataFromExchange;
   private final TriFunction<Exchange,
           BiConsumer<Exchange,RT>,
           AsyncCallback,
           StreamObserver<RT>> toDoCallback= ToDoCallback<RT>::new;
   private final Exchange exchange;

    public AsyncThreadTask(FourConsumer<IT, TriFunction<Exchange,
            BiConsumer<Exchange,RT>,
            AsyncCallback,
            StreamObserver<RT>>,
            AsyncCallback,
            Metadata>  runCallback,
                           Exchange exchange,
                           Function<Exchange, IT> getArgumentFromExchange,
                           AsyncCallback camelCallback,
                           Function<Exchange, Metadata> getMetadataFromExchange
                          ) {
        this.runCallback = runCallback;

        this.exchange = exchange;
        this.getArgumentFromExchange = getArgumentFromExchange;
        this.camelCallback = camelCallback;
        this.getMetadataFromExchange = getMetadataFromExchange;
    }

    @Override
    public void run() {
        this.runCallback.accept(this.getArgumentFromExchange.apply(this.exchange),
                this.toDoCallback,
                this.camelCallback,
                this.getMetadataFromExchange.apply(this.exchange));
    }
}
