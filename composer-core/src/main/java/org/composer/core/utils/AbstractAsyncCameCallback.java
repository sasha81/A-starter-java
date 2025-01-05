package org.composer.core.utils;

import io.grpc.stub.StreamObserver;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;

public abstract class AbstractAsyncCameCallback<RespData> implements StreamObserver<RespData> {
    protected Exchange exchange;
    protected AsyncCallback callback ;
    public AbstractAsyncCameCallback( Exchange exchange, AsyncCallback callback ){
        this.callback = callback;
        this.exchange = exchange;
    }
    public abstract void onNext(RespData value);
    public abstract void onError(Throwable t);
    @Override
    public void onCompleted() {
        this.callback.done(false);
    }

}
