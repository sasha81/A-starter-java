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
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;


@WebMvcTest
@ContextConfiguration(classes= {FluxController.class, SendToCamelService.class, SinkMapService.class})
public class FluxControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SendToCamelService sendToCamelService;

    @MockBean
    private SinkMapObjectService sinkMapService;

    @MockBean
    private FluxProcessingService fluxProcessingService;

    @Test
    public void givenXTaskDto_whenCollectDataFromSources_thenReturnResponseStream() throws Exception {
        String amqpInput="amqpInput"; String restInput="restInput"; String grpcInput="grpcInput";
        String taskId = "ABCDEF";
        String url = "/x-tasks/new-x-task";
        XTaskDto xTaskDto = XTaskDto.builder()
                .amqp_input(amqpInput).grpc_input(grpcInput).rest_input(restInput)
                .build();
        FluxMessageContainer<String> AMQP_container = FluxMessageContainer.<String>builder()
                .content(amqpInput).taskId(taskId).stage(ProcessStages.AMQP).build();
        FluxMessageContainer<String> REST_container = FluxMessageContainer.<String>builder()
                .content(amqpInput).taskId(taskId).stage(ProcessStages.REST).build();
        FluxMessageContainer<String> GRPC_container = FluxMessageContainer.<String>builder()
                .content(amqpInput).taskId(taskId).stage(ProcessStages.GRPC).build();

        FluxMessageContainer[] msgs = new FluxMessageContainer[]{GRPC_container,REST_container,AMQP_container};
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
        BDDMockito.given(sendToCamelService.sendBodyToCamel(ArgumentMatchers.anyString(),ArgumentMatchers.any()))
                .willReturn(new CompletableFuture<>());


        ResultActions resultActions =mockMvc.perform(MockMvcRequestBuilders
                .post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(xTaskDto)));

        resultActions.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())

        ;

    }




}
