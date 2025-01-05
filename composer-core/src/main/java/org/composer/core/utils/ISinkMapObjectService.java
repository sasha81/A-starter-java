package org.composer.core.utils;

import org.composer.core.model.FluxMessageContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Map;
import java.util.Objects;

public interface ISinkMapObjectService {
    public void setExtraDoOnCancel(Runnable extraDoOnCancel);
    public void setExtraDoOnCreate(Runnable extraDoOnCreate);
    public Flux<FluxMessageContainer<?>> getNewFluxWithId(String id);
    public FluxSink<FluxMessageContainer<?>> getSink(String id);
    public Map<String, FluxSink<FluxMessageContainer<?>>> getSinkMap();
    public void publish(String id, FluxMessageContainer<?> message);
    public void error(String id, String message);
    public void deleteMap(String id);
}
