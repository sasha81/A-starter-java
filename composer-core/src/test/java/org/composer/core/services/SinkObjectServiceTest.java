package org.composer.core.services;


import org.composer.core.model.FluxMessageContainer;
import org.composer.core.utils.ISinkMapObjectService;
import org.composer.core.utils.SinkMapObjectService;
import org.composer.core.utils.SinkMapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class SinkObjectServiceTest {
    @Spy
    private ISinkMapObjectService sinkMapService= new SinkMapObjectService();

    @Test
    public void createNewSinkAndSendAMsg(){
        CountDownLatch latch = new CountDownLatch(1);
        String taskId="abcdef";
        String msg = "Hello, reactor!";
        FluxMessageContainer<?> message = FluxMessageContainer.<String>builder().taskId(taskId).content(msg).build();
        Flux<FluxMessageContainer<?>> flux = sinkMapService.getNewFluxWithId(taskId);
        //The callback is called after a message has been published. So, there is no deadlock here
        flux.subscribe(m->{
            latch.countDown();
            assertEquals(m,message);
        });
        sinkMapService.publish(taskId,message);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }




    }
}
