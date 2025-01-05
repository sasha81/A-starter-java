package org.composer.adapter.utils;

import org.composer.core.model.FluxMessageContainer;
import org.composer.core.utils.ISinkMapObjectService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

public class CustomFluxUtils {

    public static Flux<String> getStringFlux(String[] input) {
        return Flux.fromArray(input);
    }
    public static Flux<FluxMessageContainer<?>> getFluxMessageContainerFlux(FluxMessageContainer<?>[] input) {
        return Flux.fromArray(input);
    }
    public static Flux<Object> getObjectFlux(String[] input) {
        return Flux.fromArray(input);
    }

    public static ISinkMapObjectService getSinkMapService (Flux<FluxMessageContainer<?>> flux, BiConsumer<String, FluxMessageContainer<?>> callback){
        return new ISinkMapObjectService(){

            @Override
            public void setExtraDoOnCancel(Runnable extraDoOnCancel) {

            }

            @Override
            public void setExtraDoOnCreate(Runnable extraDoOnCreate) {

            }

            @Override
            public Flux<FluxMessageContainer<?>> getNewFluxWithId(String id) {
               return flux;
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
                callback.accept(id,message);
            }

            @Override
            public void error(String id, String message) {

            }

            @Override
            public void deleteMap(String id) {

            }
        };
    }

    public static Runnable getRunnableStub() {
        return new Runnable() {
            @Override
            public void run() {
                int a = 1;
            }
        };
    }

    public static Runnable getRunnableStub(CountDownLatch latch) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        };
        return runnable;
    }
}
