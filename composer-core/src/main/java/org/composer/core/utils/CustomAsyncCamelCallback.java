package org.composer.core.utils;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;

import java.util.function.BiConsumer;

public class CustomAsyncCamelCallback<R> extends AbstractAsyncCameCallback<R> {
    protected BiConsumer<Exchange, R> onNextMethod;
    protected BiConsumer<Exchange, Throwable> onErrorMethod;
    public CustomAsyncCamelCallback(Exchange exchange, AsyncCallback callback, BiConsumer<Exchange, R> onNextMethod, BiConsumer<Exchange, Throwable> onErrorMethod) {
        super(exchange, callback);
        this.onNextMethod = onNextMethod;
        this.onErrorMethod = onErrorMethod;
    }
    @Override
    public void onNext(R value) {
        this.onNextMethod.accept(this.exchange, value);
    }
    @Override
    public  void onError(Throwable t){
        this.onErrorMethod.accept(this.exchange,t);
        super.callback.done(false);
    }
}
