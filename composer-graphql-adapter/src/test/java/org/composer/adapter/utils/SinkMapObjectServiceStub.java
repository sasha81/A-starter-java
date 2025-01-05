package org.composer.adapter.utils;

import org.composer.core.model.FluxMessageContainer;
import org.composer.core.utils.ISinkMapObjectService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Map;


public class SinkMapObjectServiceStub implements ISinkMapObjectService {
    private Flux<FluxMessageContainer<?>> flux;
    public void setFlux(Flux<FluxMessageContainer<?>> flux){
        this.flux = flux;
    };
    @Override
    public void setExtraDoOnCancel(Runnable extraDoOnCancel) {

    }

    @Override
    public void setExtraDoOnCreate(Runnable extraDoOnCreate) {

    }

    @Override
    public Flux<FluxMessageContainer<?>> getNewFluxWithId(String id) {
        return this.flux;
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

    }

    @Override
    public void error(String id, String message) {

    }

    @Override
    public void deleteMap(String id) {

    }
}
