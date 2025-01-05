package org.composer.core.services;


import org.composer.core.utils.SinkMapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class SinkServiceTest {
    @Spy
    private SinkMapService sinkMapService= new SinkMapService();

    @Test
    public void createNewSinkAndSendAMsg(){
        CountDownLatch latch = new CountDownLatch(1);
        String taskId="abcdef";
        String msg = "Hello, reactor!";
        Flux<String> flux = sinkMapService.getNewFluxWithId(taskId);
        //The callback is called after a message has been published. So, there is no deadlock here
        flux.subscribe(m->{
            latch.countDown();
            assertEquals(m,msg);
        });
        sinkMapService.publish(taskId,msg);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }




    }
}
