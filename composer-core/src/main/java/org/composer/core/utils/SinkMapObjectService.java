package org.composer.core.utils;

import org.composer.core.model.FluxMessageContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class SinkMapObjectService implements ISinkMapObjectService{
    Logger logger = LoggerFactory.getLogger(SinkMapService.class);
    private  final Map<String, FluxSink<FluxMessageContainer<?>>> sinkMap = new ConcurrentHashMap<>();
    private Runnable extraDoOnCreate=null;
    private Runnable extraDoOnCancel=null;
    public void setExtraDoOnCancel(Runnable extraDoOnCancel) {
        this.extraDoOnCancel = extraDoOnCancel;
    }
    public void setExtraDoOnCreate(Runnable extraDoOnCreate){
        this.extraDoOnCreate = extraDoOnCreate;
    }
    public Flux<FluxMessageContainer<?>> getNewFluxWithId(String id){
        Flux<FluxMessageContainer<?>> flux = Flux.create(fluxSink -> {
            logger.info("create subscription for " + id);
            fluxSink.onCancel(
                    () -> {
                        this.sinkMap.remove(id);
                        logger.info("subscription " + id + " was closed");
                        if(Objects.nonNull(this.extraDoOnCancel)){this.extraDoOnCancel.run();}
                    }
            );
            this.sinkMap.put(id, fluxSink);
            if(Objects.nonNull(this.extraDoOnCreate)){this.extraDoOnCreate.run();}
        });
        return flux;
    }
    public FluxSink<FluxMessageContainer<?>> getSink(String id){
        return this.sinkMap.get(id);
    }
    public Map<String, FluxSink<FluxMessageContainer<?>>> getSinkMap(){return this.sinkMap;}
    public void publish(String id, FluxMessageContainer<?> message){
        FluxSink<FluxMessageContainer<?>> sink = this.sinkMap.get(id);
        sink.next(message);
    }
    public void error(String id, String message){
        FluxSink<FluxMessageContainer<?>> sink = this.sinkMap.get(id);
        sink.error(new RuntimeException(message));
    }

    public void deleteMap(String id){

        this.sinkMap.remove(id);

    }
}
