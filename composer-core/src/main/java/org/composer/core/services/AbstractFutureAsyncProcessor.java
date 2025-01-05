package org.composer.core.services;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class AbstractFutureAsyncProcessor<M,I,O> implements AsyncProcessor {

    protected abstract CompletableFuture<O> getOutputFutureMethod(Exchange exchange, I input, M metadata);
    protected abstract I getInputFromExchange(Exchange exchange);
    protected abstract M getMetadataFromExchange(Exchange exchange);

    protected abstract Function<O,Exchange> getExchangeFutureMethod(Exchange exchange);

    protected Function<Throwable, Exchange> getErrorHandler(Exchange exchange){
        return err->{
            exchange.setException(err);
            return exchange;
        };
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        CompletableFuture<Exchange> future = this.processAsync(exchange);
        future.whenComplete((res,err)->{
            callback.done(false);
        });

        return false;
    }

    @Override
    public CompletableFuture<Exchange> processAsync(Exchange exchange) {
        M md = getMetadataFromExchange(exchange);
       I input = getInputFromExchange(exchange);
        CompletableFuture<O> rawResult = getOutputFutureMethod(exchange, input,md);
        CompletableFuture<Exchange> result = rawResult
                .thenApply(getExchangeFutureMethod(exchange))
                .exceptionally(getErrorHandler(exchange)
                );
        return result;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

    }
}
