package org.composer.core.stubs;

import org.composer.core.model.FluxMessageContainer;
import org.composer.core.utils.ISinkMapObjectService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SinkObjectServiceStub implements ISinkMapObjectService {

    private BiConsumer<String,FluxMessageContainer<?>> publishConsumer;

    public void setPublishConsumer(BiConsumer<String,FluxMessageContainer<?>> publishConsumer){
        this.publishConsumer = publishConsumer;
    }
    @Override
    public void setExtraDoOnCancel(Runnable extraDoOnCancel) {

    }

    @Override
    public void setExtraDoOnCreate(Runnable extraDoOnCreate) {

    }

    @Override
    public Flux<FluxMessageContainer<?>> getNewFluxWithId(String id) {
        return null;
    }

    @Override
    public FluxSink<FluxMessageContainer<?>> getSink(String id) {
        return null;
    }

    @Override
    public Map<String, FluxSink<FluxMessageContainer<?>>> getSinkMap() {
        return null;
    }

    @Override
    public void publish(String id, FluxMessageContainer<?> message) {
        this.publishConsumer.accept(id,message);
    }

    @Override
    public void error(String id, String message) {

    }

    @Override
    public void deleteMap(String id) {

    }
}
