package org.composer.core.processors;


import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.engine.SimpleCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.EmptyAsyncCallback;
import org.composer.core.converters.*;
import org.composer.core.model.ModelUser;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.services.RestFutureProcessor;
import org.composer.core.utils.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestProcessorTest {

    @Test
    public void restProcessorTest(){
        String uri = "/users/all";
        CountDownLatch latch = new CountDownLatch(1);
        CamelContext camelContext = new SimpleCamelContext();

        AsyncCallback asyncCamelCallback = new EmptyAsyncCallback();
        WebClientMocks<String> mocks = new WebClientMocks<>();
        WebClient webClient = mocks.getWebClientMock();
        Mono<String> monoMock = mocks.getMonoMock();

        RestFutureProcessor processor = new RestFutureProcessor(webClient,uri);
        RestModelGroupDto group = RestModelGroupDto.builder().groupId("12345")
                .groupName("Physics").userId("ABC").build();
        RestModelUserDto userViewDto = RestModelUserDto.builder()
                .userId("ABC").name("Sasha")
                .age(30).groups(List.of(group)).build();

        RestModelUserDto[] userArrayDto = new RestModelUserDto[] {userViewDto};

        doAnswer(invocation->{

            CompletableFuture<RestModelUserDto[]> future = CompletableFuture.completedFuture(userArrayDto);
            latch.countDown();
          return future;
        }).when(monoMock).toFuture();

        Exchange exchange = new DefaultExchange(camelContext);

        String taskId="abcdef";
        String grpcInput= "Ann";

        String amqpInput = "John";
        String restInput = "Mark";
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())
                .build();

        exchange.getMessage().setBody(model);

        processor.process(exchange,asyncCamelCallback);
        CompareUsersModel modelOut = exchange.getMessage().getBody(CompareUsersModel.class);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertNotNull(modelOut.getRest_step().getOutput());
        assertEquals(modelOut.getRest_step().getOutput().get(0).getUserId(),userViewDto.getUserId());
        assertEquals(modelOut.getRest_step().getOutput().get(0).getUsername(),userViewDto.getName());
        assertEquals(modelOut.getRest_step().getOutput().get(0).getUserage(),userViewDto.getAge());
        assertEquals(modelOut.getRest_step().getOutput().get(0).getGroups().get(0).getGroupId()
                ,userViewDto.getGroups().get(0).getGroupId());
    }
    @Test
    public void restProcessorErrorTest(){
        String uri = "/users/all";
        CountDownLatch latch = new CountDownLatch(1);
        CamelContext camelContext = new SimpleCamelContext();
        String errorMsg = "ERROR OCCURRED!";
        AsyncCallback asyncCamelCallback = new EmptyAsyncCallback();
        Throwable exception = new RuntimeException(errorMsg);
        WebClientMocks<String> mocks = new WebClientMocks<>();
        WebClient webClient = mocks.getWebClientMock();
        Mono<String> monoMock = mocks.getMonoMock();

        RestFutureProcessor processor = new RestFutureProcessor(webClient, uri);

        doAnswer(invocation->{

            CompletableFuture<String> future = CompletableFuture.failedFuture(exception);
            latch.countDown();
            return future;
        }).when(monoMock).toFuture();

        Exchange exchange = new DefaultExchange(camelContext);

        String taskId="abcdef";
        String grpcInput= "Ann";

        String amqpInput = "John";
        String restInput = "Mark";
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())
                .build();

        exchange.getMessage().setBody(model);

        processor.process(exchange,asyncCamelCallback);
        CompareUsersModel modelOut = exchange.getMessage().getBody(CompareUsersModel.class);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertNull(modelOut.getRest_step().getOutput());
        assertTrue(modelOut.getRest_step().getErrorMessage().contains(RestFutureProcessor.getErrorMessage(exception)));
    }
private class WebClientMocks<R>{
    final WebClient mock;
    final WebClient.RequestHeadersUriSpec uriSpecMock;
    final WebClient.RequestHeadersSpec headersSpecMock;
    final WebClient.ResponseSpec responseSpecMock;
    final Mono monoSpecMock;



    public WebClientMocks() {
        mock = Mockito.mock(WebClient.class);
        uriSpecMock = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        headersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
        responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);
        monoSpecMock = Mockito.mock(Mono.class);

        when(mock.get()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri(ArgumentMatchers.<String>notNull())).thenReturn(headersSpecMock);
//        when(headersSpecMock.header(notNull(), notNull())).thenReturn(headersSpecMock);
        when(headersSpecMock.headers(notNull())).thenReturn(headersSpecMock);
        when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(ArgumentMatchers.<Class<String>>notNull())).thenReturn( monoSpecMock);
    }

    public  WebClient getWebClientMock(){
        return mock;
    }

    public Mono<R> getMonoMock(){
        return monoSpecMock;
    }
}
    private static WebClient getWebClientMock(final String resp) {
        final var mock = Mockito.mock(WebClient.class);
        final var uriSpecMock = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        final var headersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
        final var responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);
        final var monoSpecMock = Mockito.mock(Mono.class);

        when(mock.get()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri(ArgumentMatchers.<String>notNull())).thenReturn(headersSpecMock);
        when(headersSpecMock.header(notNull(), notNull())).thenReturn(headersSpecMock);
        when(headersSpecMock.headers(notNull())).thenReturn(headersSpecMock);
        when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(ArgumentMatchers.<Class<String>>notNull())).thenReturn(monoSpecMock);
//        when(monoSpecMock.toFuture())
//                .thenReturn(CompletableFuture.completedFuture(resp));

        return mock;
    }
}
