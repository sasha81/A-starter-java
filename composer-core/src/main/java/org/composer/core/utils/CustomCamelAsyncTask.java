package org.composer.core.utils;

import org.example.common.utils.TriConsumer;
import io.grpc.stub.StreamObserver;
import lombok.Builder;
import lombok.Data;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;

import java.util.function.BiConsumer;
import java.util.function.Function;


@Data
@Builder
public class CustomCamelAsyncTask<M,I,O> implements Runnable {
    private Exchange exchange;
    private AsyncCallback camelCallback;
    private M metadata;
    private Function<Exchange,M> getMetadata;
    private I input;
    private Function<Exchange,I> getInput;
    private StreamObserver<O> outputCallback;

    private TriConsumer<M, I, StreamObserver<O>> callTask;
    private BiConsumer<Exchange, O> onNextMethod;
    private BiConsumer<Exchange, Throwable> onErrorMethod;
    @Override
    public void run() {
        this.callTask.accept(this.metadata,this.input,this.outputCallback);
    }
    public static <M,I,O> CustomCamelAsyncTaskBuilder<M,I,O> builder() {
        return new CustomTaskBuilder<M,I,O>();
    }
    /**
     * Custom  builder class
     */
    private static class  CustomTaskBuilder<M,I,O> extends CustomCamelAsyncTaskBuilder<M,I,O> {
        @Override
        public CustomCamelAsyncTask<M,I,O> build() {
            super.metadata = super.getMetadata.apply(super.exchange);
            super.input = super.getInput.apply(super.exchange);
            super.outputCallback = new CustomAsyncCamelCallback<O>(super.exchange, super.camelCallback,super.onNextMethod,super.onErrorMethod);

            return super.build();
        }
    }
}
