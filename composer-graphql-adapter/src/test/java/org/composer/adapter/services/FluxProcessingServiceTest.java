package org.composer.adapter.services;



import org.composer.adapter.dto.TaskOutput;
import org.composer.adapter.utils.CustomFluxUtils;
import org.composer.core.model.FluxMessageContainer;
import org.composer.core.model.ModelGroup;
import org.composer.core.model.ModelUser;
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
        String userId="ABCDE";
        String taskId = "ABCDEF123";
        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        FluxMessageContainer AMQP_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId(taskId).stage(ProcessStages.AMQP).build();
        FluxMessageContainer REST_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId(taskId).stage(ProcessStages.REST).build();
        FluxMessageContainer GRPC_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId(taskId).stage(ProcessStages.GRPC).build();

        FluxMessageContainer[] msgs = new FluxMessageContainer[] {GRPC_container,REST_container,AMQP_container};
        Flux<FluxMessageContainer<?>> flux = CustomFluxUtils.getFluxMessageContainerFlux(msgs);
        Flux<TaskOutput> outFlux = fluxProcessingService.postProcessContainerFlux(flux,taskId,onCancel);
        List<TaskOutput> outList = outFlux.collectList().block();
        assertEquals(outList.size(),msgs.length);
        Mockito.verify(onCancel).run();

    }
    @Test
    public void postProcessFluxWithoutCorrectTaskIdAndNoSTOPTest(){
        String userId="ABCDE";
        String taskId = "ABCDEF123";
        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        FluxMessageContainer AMQP_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId(taskId).stage(ProcessStages.AMQP).build();
        FluxMessageContainer REST_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId("A").stage(ProcessStages.REST).build();
        FluxMessageContainer GRPC_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId("B").stage(ProcessStages.GRPC).build();

        FluxMessageContainer[] msgs = new FluxMessageContainer[]{GRPC_container,REST_container,AMQP_container};
        Flux<FluxMessageContainer<?>> flux = CustomFluxUtils.getFluxMessageContainerFlux(msgs);
        Flux<TaskOutput> outFlux = fluxProcessingService.postProcessContainerFlux(flux,taskId,onCancel);
        List<TaskOutput> outList = outFlux.collectList().block();

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
        String userId="ABCDE";
        String taskId = "ABCDEF123";
        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        FluxMessageContainer AMQP_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId(taskId).stage(ProcessStages.AMQP).build();
        FluxMessageContainer REST_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId(taskId).stage(ProcessStages.STOP).build();
        FluxMessageContainer GRPC_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId(taskId).stage(ProcessStages.GRPC).build();

        FluxMessageContainer[] msgs = new FluxMessageContainer[]{GRPC_container,REST_container,AMQP_container};
        Flux<FluxMessageContainer<?>> flux = CustomFluxUtils.getFluxMessageContainerFlux(msgs);
        Flux<TaskOutput> outFlux = fluxProcessingService.postProcessContainerFlux(flux,taskId,()->
        {   onCancel1.run();
            latch.countDown();
            });

        List<TaskOutput> outList = outFlux.collectList().block();
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
        String taskId = "ABCDEF123";
        SinkMapObjectService sinkMapService= new SinkMapObjectService();
        sinkMapService.setExtraDoOnCreate(()->{latch.countDown();});
        Flux<FluxMessageContainer<?>> flux = sinkMapService.getNewFluxWithId(taskId);

        String userId="ABCDE";

        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        FluxMessageContainer AMQP_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId(taskId).stage(ProcessStages.AMQP).build();
        FluxMessageContainer REST_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId(taskId).stage(ProcessStages.STOP).build();
        FluxMessageContainer GRPC_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId(taskId).stage(ProcessStages.GRPC).build();

        Flux<TaskOutput> outFlux = fluxProcessingService.postProcessContainerFlux(flux,taskId,()->sinkMapService.deleteMap(taskId));


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

        try {
            subscribeLatch.await();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //With a STOP, the taskId map entry is deleted
        assertEquals(sinkMapService.getSinkMap().size(),0);



    }
    @Test
    public void postProcessFluxWithTaskIdAndWithoutSTOPAndWithRealSinkServiceTest(){
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch subscribeLatch = new CountDownLatch(2);
        String taskId = "ABCDEF123";
        SinkMapObjectService sinkMapService= new SinkMapObjectService();
        sinkMapService.setExtraDoOnCreate(()->{latch.countDown();});
        Flux<FluxMessageContainer<?>> flux = sinkMapService.getNewFluxWithId(taskId);

        String userId="ABCDE";

        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        FluxMessageContainer AMQP_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId(taskId).stage(ProcessStages.AMQP).build();
        FluxMessageContainer REST_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId(taskId).stage(ProcessStages.REST).build();
        FluxMessageContainer GRPC_container = FluxMessageContainer.<List<ModelUser>>builder()
                .content(userList).taskId(taskId).stage(ProcessStages.GRPC).build();

        Flux<TaskOutput> outFlux = fluxProcessingService.postProcessContainerFlux(flux,taskId,()->sinkMapService.deleteMap(taskId));


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
        sinkMapService.publish(taskId,GRPC_container);

        try {
            subscribeLatch.await();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //Without a STOP, the taskId map entry remains
        assertEquals(sinkMapService.getSinkMap().size(),1);



    }
}
