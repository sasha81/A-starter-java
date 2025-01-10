package org.composer.adapter.services;


import org.composer.adapter.dto.OutputDto;
import org.composer.adapter.utils.CustomFluxUtils;

import org.composer.core.model.FluxMessageContainer;
import org.composer.core.model.ProcessStages;
import org.composer.core.utils.SinkMapObjectService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public class FluxProcessingServiceTest {


    private FluxProcessingService fluxProcessingService= new FluxProcessingService();


    private Runnable onCancel;

    @BeforeEach
    public void buildSpies(){
        onCancel=spy(new Runnable() {
            @Override
            public void run() {
                int a = 1;
            }
        });
    }


    @Test
    public void postProcessFluxWithTaskIdAndNoSTOPTest(){
        String taskId = "ABCDEF"; String amqpInput="amqpInput"; String restInput="restInput"; String grpcInput="grpcInput";
        FluxMessageContainer<String> AMQP_container = FluxMessageContainer.<String>builder()
                .content(amqpInput).taskId(taskId).stage(ProcessStages.AMQP).build();
        FluxMessageContainer<String> REST_container = FluxMessageContainer.<String>builder()
                .content(restInput).taskId(taskId).stage(ProcessStages.REST).build();
        FluxMessageContainer<String> GRPC_container = FluxMessageContainer.<String>builder()
                .content(grpcInput).taskId(taskId).stage(ProcessStages.GRPC).build();

        FluxMessageContainer[] msgs = new FluxMessageContainer[] {GRPC_container,REST_container,AMQP_container};
        Flux<FluxMessageContainer<?>> flux = CustomFluxUtils.getFluxMessageContainerFlux(msgs);
        Flux<ServerSentEvent<OutputDto>> outFlux = fluxProcessingService.postProcessContainerFlux(flux,taskId,onCancel);
        List<ServerSentEvent<OutputDto>> outList = outFlux.collectList().block();
        assertEquals(outList.size(),msgs.length);
        Mockito.verify(onCancel).run();

    }
    @Test
    public void postProcessFluxWithoutCorrectTaskIdAndNoSTOPTest(){
        String taskId = "ABCDEF"; String wrongTaskId="ABC"; String amqpInput="amqpInput"; String restInput="restInput"; String grpcInput="grpcInput";
        FluxMessageContainer<String> AMQP_container = FluxMessageContainer.<String>builder()
                .content(amqpInput).taskId(taskId).stage(ProcessStages.AMQP).build();
        FluxMessageContainer<String> REST_container = FluxMessageContainer.<String>builder()
                .content(restInput).taskId(wrongTaskId).stage(ProcessStages.REST).build();
        FluxMessageContainer<String> GRPC_container = FluxMessageContainer.<String>builder()
                .content(grpcInput).taskId(wrongTaskId).stage(ProcessStages.GRPC).build();

        FluxMessageContainer[] msgs = new FluxMessageContainer[]{GRPC_container,REST_container,AMQP_container};
        Flux<FluxMessageContainer<?>> flux = CustomFluxUtils.getFluxMessageContainerFlux(msgs);
        Flux<ServerSentEvent<OutputDto>> outFlux = fluxProcessingService.postProcessContainerFlux(flux,taskId,onCancel);
        List<ServerSentEvent<OutputDto>> outList = outFlux.collectList().block();

        assertEquals(outList.size(),1);
        Mockito.verify(onCancel).run();

    }

    @Test
    public void postProcessFluxWithTaskIdAndWithSTOPTest() {
        final AtomicBoolean onCancelWasRun = new AtomicBoolean(false);
        Runnable onCancel1=spy(new Runnable() {
            @Override
            public void run() {
                onCancelWasRun.set(true);
            }
        });
        CountDownLatch latch = new CountDownLatch(1);
        String taskId = "ABCDEF"; String amqpInput="amqpInput"; String restInput="restInput"; String grpcInput="grpcInput";
        FluxMessageContainer<String> AMQP_container = FluxMessageContainer.<String>builder()
                .content(amqpInput).taskId(taskId).stage(ProcessStages.AMQP).build();
        FluxMessageContainer<String> REST_container = FluxMessageContainer.<String>builder()
                .content(restInput).taskId(taskId).stage(ProcessStages.STOP).build();
        FluxMessageContainer<String> GRPC_container = FluxMessageContainer.<String>builder()
                .content(grpcInput).taskId(taskId).stage(ProcessStages.GRPC).build();

        FluxMessageContainer[] msgs = new FluxMessageContainer[]{GRPC_container,REST_container,AMQP_container};
        Flux<FluxMessageContainer<?>> flux = CustomFluxUtils.getFluxMessageContainerFlux(msgs);
        Flux<ServerSentEvent<OutputDto>> outFlux = fluxProcessingService.postProcessContainerFlux(flux,taskId,()->
        {   onCancel1.run();
            latch.countDown();
            });

        List<ServerSentEvent<OutputDto>> outList = outFlux.collectList().block();
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(outList.size(),2);
        assertEquals(onCancelWasRun.get(),true);
        Mockito.verify(onCancel1).run();

    }
    @Test
    public void postProcessFluxWithTaskIdAndWithSTOPAndWithRealSinkServiceTest(){
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch subscribeLatch = new CountDownLatch(2);
        String taskId = "ABCDEF"; String amqpInput="amqpInput"; String restInput="restInput"; String grpcInput="grpcInput";
        SinkMapObjectService sinkMapService= new SinkMapObjectService();
        sinkMapService.setExtraDoOnCreate(()->{latch.countDown();});
        Flux<FluxMessageContainer<?>> flux = sinkMapService.getNewFluxWithId(taskId);


        FluxMessageContainer<String> AMQP_container = FluxMessageContainer.<String>builder()
                .content(amqpInput).taskId(taskId).stage(ProcessStages.AMQP).build();
        FluxMessageContainer<String> REST_container = FluxMessageContainer.<String>builder()
                .content(restInput).taskId(taskId).stage(ProcessStages.STOP).build();
        FluxMessageContainer<String> GRPC_container = FluxMessageContainer.<String>builder()
                .content(grpcInput).taskId(taskId).stage(ProcessStages.GRPC).build();

        Flux<ServerSentEvent<OutputDto>> outFlux = fluxProcessingService.postProcessContainerFlux(flux,taskId,onCancel);


        outFlux.subscribe(el->{
            subscribeLatch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sinkMapService.publish(taskId,AMQP_container );
        sinkMapService.publish(taskId,REST_container);


        assertEquals(sinkMapService.getSinkMap().size(),0);
        try {
            subscribeLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        Mockito.verify(onCancel).run();

    }
}
