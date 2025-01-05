package org.composer.adapter.resolvers;


import org.apache.camel.ProducerTemplate;
import org.composer.adapter.dto.TaskInput;
import org.composer.adapter.dto.TaskOutput;
import org.composer.adapter.services.FluxProcessingService;
import org.composer.adapter.services.ISendToCamelService;
import org.composer.adapter.services.SendToCamelService;
import org.composer.adapter.utils.CustomFluxUtils;
import org.composer.adapter.utils.FluxProcessingServiceStub;
import org.composer.adapter.utils.SendToCamelServiceStub;
import org.composer.adapter.utils.SinkMapObjectServiceStub;
import org.composer.core.model.FluxMessageContainer;
import org.composer.core.model.ProcessStages;
import org.composer.core.utils.ISinkMapObjectService;
import org.composer.core.utils.SinkMapObjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.WebSocketGraphQlTester;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public class XTaskSubscriberTests {
    private  SinkMapObjectServiceStub sinkMapObjectService= new SinkMapObjectServiceStub();
    private ISendToCamelService sendToCamelService= new SendToCamelServiceStub();
    private final FluxProcessingServiceStub fluxProcessingService = new FluxProcessingServiceStub();
    private XTaskSubscriber xTaskSubscriber = new XTaskSubscriber(sinkMapObjectService,sendToCamelService,fluxProcessingService);

    @Test
    void newXTask(){

        String amqpInput="amqpInput"; String restInput="restInput"; String grpcInput="grpcInput";
        String taskId = "ABCDEF";

        TaskInput xTaskDto = TaskInput.builder()
                .amqp_input(amqpInput).grpc_input(grpcInput).rest_input(restInput)
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
            Flux<TaskOutput> result =  xTaskSubscriber.newXtask(xTaskDto);
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
