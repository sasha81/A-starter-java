package org.composer.core.services;

import org.example.common.utils.TriConsumer;
import io.grpc.stub.StreamObserver;
import lombok.NoArgsConstructor;
import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.composer.core.utils.CustomCamelAsyncTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Function;

@NoArgsConstructor
public abstract class RunnableCamelAsyncProcessor<M,I,O> implements AsyncProcessor {
    protected ExecutorService executorService=null;
    protected RunnableCamelAsyncProcessor(ExecutorService executorService) {
        this.executorService = executorService;
    }
    protected abstract  BiConsumer<Exchange, O> getOnNextCamelCallback();
    protected abstract TriConsumer<M, I, StreamObserver<O>> getCallTask();
    protected abstract Function<Exchange,M> getMetadataFromExchange();
    protected abstract Function<Exchange,I> getInputFromExchange();
    protected BiConsumer<Exchange, Throwable> getOnErrorCamelCallback(){
        return Exchange::setException;
    };
    @Override
    public boolean process(Exchange exchange, AsyncCallback camelCallback) {
        ExecutorService executor;
        if(this.executorService==null){
            executor = exchange.getContext().getExecutorServiceManager().newFixedThreadPool(this,"GRPC",2);
        }else{
            executor=this.executorService;
        }
        executor.submit(getTask(exchange, camelCallback));
        return false;

    }

    @Override
    public CompletableFuture<Exchange> processAsync(Exchange exchange) {
        return null;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

    }

    protected Runnable getTask(Exchange exchange, AsyncCallback camelCallback){
        CustomCamelAsyncTask<M,I, O> task =
                CustomCamelAsyncTask.<M,I, O>builder()
                        .exchange(exchange)
                        .camelCallback(camelCallback)
                        .getMetadata(getMetadataFromExchange())
                        .getInput(getInputFromExchange())
                        .callTask(getCallTask())
                        .onNextMethod(getOnNextCamelCallback())
                        .onErrorMethod(getOnErrorCamelCallback())
                        .build();
        return  task;
    }
}
