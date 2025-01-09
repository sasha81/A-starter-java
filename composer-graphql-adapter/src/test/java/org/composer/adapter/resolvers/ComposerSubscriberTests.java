package org.composer.adapter.resolvers;



import org.composer.adapter.dto.TaskInput;
import org.composer.adapter.dto.TaskOutput;

import org.composer.adapter.services.ISendToCamelService;

import org.composer.adapter.utils.CustomFluxUtils;
import org.composer.adapter.utils.FluxProcessingServiceStub;
import org.composer.adapter.utils.SendToCamelServiceStub;
import org.composer.adapter.utils.SinkMapObjectServiceStub;
import org.composer.core.model.FluxMessageContainer;
import org.composer.core.model.ProcessStages;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;


import java.io.IOException;

import java.util.List;


import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class ComposerSubscriberTests {
    private  SinkMapObjectServiceStub sinkMapObjectService= new SinkMapObjectServiceStub();
    private ISendToCamelService sendToCamelService= new SendToCamelServiceStub();
    private final FluxProcessingServiceStub fluxProcessingService = new FluxProcessingServiceStub();
    private ComposerSubscriber xTaskSubscriber = new ComposerSubscriber(sinkMapObjectService,sendToCamelService,fluxProcessingService);

    @Test
    void newXTask(){

        String amqpInput="amqpInput"; String restInput="restInput"; String grpcInput="grpcInput";
        String taskId = "ABCDEF"; String specs = "specs";

        TaskInput xTaskDto = TaskInput.builder()
                .specifics(specs)
                .build();

        FluxMessageContainer<String> AMQP_container = FluxMessageContainer.<String>builder()
                .content(amqpInput).taskId(taskId).stage(ProcessStages.AMQP).build();
        FluxMessageContainer<String> REST_container = FluxMessageContainer.<String>builder()
                .content(restInput).taskId(taskId).stage(ProcessStages.REST).build();
        FluxMessageContainer<String> GRPC_container = FluxMessageContainer.<String>builder()
                .content(grpcInput).taskId(taskId).stage(ProcessStages.GRPC).build();

        FluxMessageContainer[] msgs = {GRPC_container,REST_container,AMQP_container};
        Flux<FluxMessageContainer<?>> flux = CustomFluxUtils.getFluxMessageContainerFlux(msgs);
        fluxProcessingService.setTaskId(taskId);
        sinkMapObjectService.setFlux(flux);


        try {
            Flux<TaskOutput> result =  xTaskSubscriber.compareUsers(xTaskDto);
            List<TaskOutput> outList =  result.collectList().block();
            assertEquals(outList.size(),msgs.length);
            assertEquals(outList.get(0).getStage(),ProcessStages.GRPC);
            assertEquals(outList.get(0).getContent(),GRPC_container.getContent());
        } catch (IOException e) {
            throw new RuntimeException(e); }
    }
    @Test
    void test(){
        String input = "AAA";
        Flux<String> output = (Flux<String>) xTaskSubscriber.test(input);
        List<String> outList =  output.collectList().block();
        assertEquals(outList.size(),3);
    }

    @Test
    void greet(){
        String input = "AAA";
        String output =  xTaskSubscriber.greet(input);

        assertTrue(output.contains(input));
    }

}
