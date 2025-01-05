package org.composer.core.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.composer.core.converters.*;
import org.composer.core.model.ModelUser;
import org.composer.core.model.XTaskModel;
import org.composer.core.services.ReactorSinkService;
import org.composer.core.services.RestFutureProcessor;
import org.composer.core.stubs.SyncProcessorStub;
import org.composer.core.utils.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;


import java.util.List;
import java.util.stream.Stream;

import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestRouteTest extends CamelTestSupport {
    @Mock
    BusinessProcessXService businessProcessXService;
    @Mock
    ReactorSinkService reactorSinkService;
    @Spy
    Processor exceptionProcessor = new SyncProcessorStub();
    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new BusinessProcessXRoute(businessProcessXService, reactorSinkService);
    }


    @Test
    public void RstRouteTest() throws Exception {
        RouteDefinition route = context.getRouteDefinition("X_Rest_step");
        String restInput = "buddy!";
        String restOutput = "Hello " + restInput + "!";

        RestModelGroupDto restGroupDto = RestModelGroupDto.builder()
                .groupId("A").groupName("B").userId("ABCD").build();
        RestModelUserDto restUserDto = RestModelUserDto.builder().groups(List.of(restGroupDto))
                .name("A").age(15).userId("ABCD").build();
        adviceWith(route, context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById("Rest_Async_Processor").replace().setBody(exchange -> {

                    return RestFutureProcessor.setBody(exchange, Stream.of( restUserDto).map(GetUserModel::fromDto).toList());
                });
                weaveAddLast().to("mock:finishRestRoute");
            }
        });
        context.start();
        doAnswer((Answer<Void>) invocation -> {
            Exchange exchange = invocation.getArgument(0);
            XTaskModel body = exchange.getMessage().getBody(XTaskModel.class);
            assertEquals(body.getRest_step().getOutput(), restOutput);

            return null;
        }).when(reactorSinkService).notifyAboutRestStep(any());

        MockEndpoint mock = getMockEndpoint("mock:finishRestRoute");
        String taskId = "abcdef";
        String grpcInput = "Ann";

        String amqpInput = "buddy!";

        XTaskModel model = XTaskModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())
                .build();

        mock.setExpectedMessageCount(1);
        template.sendBody("direct:X_Rest_step", model);
        mock.assertIsSatisfied();
        Message message = mock.getExchanges().get(0).getMessage();
        XTaskModel modelOut = message.getBody(XTaskModel.class);
        assertEquals(modelOut.getRest_step().getOutput(), List.of( GetUserModel.fromDto(restUserDto)));
    }

    @Test
    /*
    This test tests if an Rest processor throws an exception, the exception first get caught by the onException
    clause, then propagates down the original route.
     */
    public void RestRouteErrorTest() throws Exception{
        RouteDefinition route = context.getRouteDefinition("X_Rest_step");


        String exceptionMsg = "Ooops!";
        RuntimeException exception = new RuntimeException(exceptionMsg);
        adviceWith(route, context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById("Rest_Async_Processor").replace().process(exchange -> {
                    throw exception;
                });
                weaveById("X_exception_processor").replace().process(exceptionProcessor);


                weaveAddLast().to("mock:finishRestRoute");
            }
        });

        context.start();
        doAnswer((Answer<Void>) invocation->{
            Exchange exchange = invocation.getArgument(0);
            XTaskModel body =  exchange.getMessage().getBody(XTaskModel.class);
            assertNull(body.getRest_step().getOutput());

            return null;
        }).when(reactorSinkService).notifyAboutRestStep(any(Exchange.class));

        MockEndpoint mock = getMockEndpoint("mock:finishRestRoute");
        String taskId="abcdef";
        String grpcInput= "Ann";
        String amqpInput = "buddy!";
        String restInput = "Mark";
        XTaskModel model = XTaskModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())
                .build();

        mock.setExpectedMessageCount(1);
        template.sendBody("direct:X_Rest_step",model );
        mock.assertIsSatisfied();

        verify(reactorSinkService).notifyAboutRestStep(any());

        verify(exceptionProcessor, atLeast(1)).process(any(Exchange.class));
    }

    @Test
    /*
    This test tests if an AMQP processor throws an exception, the exception first get caught by the onException
    clause; then the exchange continues down the original route.

    Next, another exception is thrown as we call a reactorSinkService.notifyAboutAMQPStep(any()) method.

    So, our exception processor should be called twice.
     */
    public void RestRoute2ErrorTest() throws Exception{
        RouteDefinition route = context.getRouteDefinition("X_Rest_step");
        String amqpInput = "buddy!";

        String exceptionMsg = "Ooops!";
        RuntimeException exception = new RuntimeException(exceptionMsg);
        adviceWith(route, context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById("Rest_Async_Processor").replace().process(exchange -> {
                    throw exception;
                });
                weaveById("X_exception_processor").replace().process(exceptionProcessor);


                weaveAddLast().to("mock:finishRestRoute");
            }
        });

        context.start();
        doAnswer((Answer<Void>) invocation->{
            Exchange exchange = invocation.getArgument(0);
            XTaskModel body =  exchange.getMessage().getBody(XTaskModel.class);
            assertNull(body.getRest_step().getOutput());
            throw new RuntimeException();

        }).when(reactorSinkService).notifyAboutRestStep(any(Exchange.class));

        MockEndpoint mock = getMockEndpoint("mock:finishRestRoute");
        String taskId="abcdef";
        String grpcInput= "Ann";
        String grpcOutput= "Hello "+grpcInput;

        String restInput = "Mark";
        XTaskModel model = XTaskModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())
                .build();

        mock.setExpectedMessageCount(1);
        template.sendBody("direct:X_Rest_step",model );
        mock.assertIsSatisfied();
        Exchange outExchange  = mock.getExchanges().get(0);
        Message message = mock.getExchanges().get(0).getMessage();
        XTaskModel modelOut = message.getBody(XTaskModel.class);
        verify(reactorSinkService).notifyAboutRestStep(any());

        verify(exceptionProcessor,atLeast(1)).process(any(Exchange.class));
    }
}
