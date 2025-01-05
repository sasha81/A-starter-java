package org.composer.core.utils;

import io.grpc.stub.StreamObserver;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;

public abstract class AbstractCamelAsyncTask<M,I,O> implements Runnable {
    protected Exchange exchange;
    protected AsyncCallback camelCallback;
    public AbstractCamelAsyncTask(Exchange exchange, AsyncCallback camelCallback) {
        this.exchange = exchange;
        this.camelCallback = camelCallback;
    }

    public void run() {
        M metadata = getMetadataFromExchange(this.exchange);
        I input = getInputFromExchange(this.exchange);
        StreamObserver<O> outputCallback = getAsyncCamelCallback(this.exchange, this.camelCallback);
        callTask(metadata,input,outputCallback);
    }

    abstract protected void callTask(M metadata, I input, StreamObserver<O> callback);
    abstract protected M getMetadataFromExchange(Exchange exchange);
    abstract protected I getInputFromExchange(Exchange exchange);
    abstract protected StreamObserver<O> getAsyncCamelCallback(Exchange exchange, AsyncCallback callback);
}
