package org.composer.core.utils;

import io.grpc.stub.StreamObserver;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;

import java.util.function.BiConsumer;

public class GRPCCallback<D> implements StreamObserver<D> {
    private Exchange exchange;
    private AsyncCallback callback ;
    private BiConsumer<Exchange,D> onNextCallback;
    public GRPCCallback( Exchange exchange, AsyncCallback callback ){
        this.callback = callback;
        this.exchange = exchange;
    }
    public GRPCCallback( Exchange exchange, AsyncCallback callback , BiConsumer<Exchange,D> onNextCallback){
       this(exchange,  callback);
       this.onNextCallback = onNextCallback;
    }
    @Override
    public void onNext(D value) {
        this.onNextCallback.accept(this.exchange,value);
    }
    @Override
    public void onError(Throwable t) {
        this.exchange.setException(t);
    }
    @Override
    public void onCompleted() {
        this.callback.done(false);
    }
}
