package org.composer.adapter.utils;

import org.composer.core.model.FluxMessageContainer;
import org.composer.core.model.ProcessStages;
import reactor.core.publisher.Flux;

import java.util.concurrent.CountDownLatch;

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
