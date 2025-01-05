package org.composer.adapter.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.composer.adapter.dto.OutputDto;
import org.composer.adapter.dto.XTaskDto;
import org.composer.adapter.services.FluxProcessingService;
import org.composer.adapter.services.SendToCamelService;
import org.composer.adapter.utils.CustomFluxUtils;

import org.composer.core.model.FluxMessageContainer;
import org.composer.core.model.ProcessStages;
import org.composer.core.utils.SinkMapObjectService;
import org.composer.core.utils.SinkMapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers= {FluxController.class})
@ContextConfiguration(classes= {FluxController.class, SendToCamelService.class, SinkMapService.class})
public class FluxControllerWebFluxTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SendToCamelService sendToCamelService;

    @MockBean
    private SinkMapObjectService sinkMapService;

    @MockBean
    private FluxProcessingService fluxProcessingService;

    @Test
    public void givenXTaskDto_whenCollectDataFromSources_thenReturnResponseStream() throws Exception{
        String amqpInput="amqpInput"; String restInput="restInput"; String grpcInput="grpcInput";
        String taskId = "ABCDEF";
        String url = "/x-tasks/new-x-task";
        XTaskDto xTaskDto = XTaskDto.builder()
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
        Flux<ServerSentEvent<OutputDto>> outFlux = flux
                .map(msg->  ServerSentEvent.<OutputDto> builder()
                        .id(taskId)
                        .data(OutputDto.builder().taskId(taskId).stage(msg.getStage()).content(msg.getContent()).build())
                        .event(msg.getStage().name())
                        .build())
                .doOnCancel(()->{});
        BDDMockito.given(fluxProcessingService.getTaskId()).willReturn(taskId);
        BDDMockito.given(fluxProcessingService.getXModelFromDto(xTaskDto,taskId)).willCallRealMethod();
        BDDMockito.given(fluxProcessingService.postProcessContainerFlux(ArgumentMatchers.any(),ArgumentMatchers.anyString(),ArgumentMatchers.any()))
                .willReturn(outFlux);
        BDDMockito.given(sinkMapService.getNewFluxWithId(ArgumentMatchers.anyString())).willReturn(flux);
        BDDMockito.given(sendToCamelService.sendBodyToCamel(ArgumentMatchers.anyString(),ArgumentMatchers.any())).willReturn(new CompletableFuture<>());

        WebTestClient.ResponseSpec response = webTestClient.post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(xTaskDto)
                .exchange();

        ServerSentEvent<String> sse = ServerSentEvent.<String>builder().build();
        response.expectStatus().isOk()

                .expectBodyList(sse.getClass()).hasSize(msgs.length)
                .consumeWith(System.out::println)
        ;

    }
}
