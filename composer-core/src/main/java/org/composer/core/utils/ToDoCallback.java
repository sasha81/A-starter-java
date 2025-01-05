package org.composer.core.utils;

import io.grpc.stub.StreamObserver;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;

import java.util.function.BiConsumer;

public class ToDoCallback<RT> implements StreamObserver<RT> {
private final BiConsumer<Exchange,RT> onNextCallback;
    private final AsyncCallback camelCallback;
    private final Exchange exchange;
    public ToDoCallback(Exchange exchange,BiConsumer<Exchange,RT> onNextCallback, AsyncCallback camelCallback) {
        this.onNextCallback = onNextCallback;
        this.exchange = exchange;
        this.camelCallback = camelCallback;
    }

    @Override
    public void onNext(RT value) {
        this.onNextCallback.accept(this.exchange,value);
    }
    @Override
    public void onError(Throwable t) {
        this.exchange.setException(t);
    }
    @Override
    public void onCompleted() {
        this.camelCallback.done(false);
    }
}
