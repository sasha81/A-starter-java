package org.composer.core.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.composer.core.converters.AMQPModelGroupDto;
import org.composer.core.converters.AMQPModelUserDto;
import org.composer.core.converters.GetUserModel;
import org.composer.core.model.ModelUser;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.services.AMQPFutureProcessor;
import org.composer.core.services.UserProcessService;
import org.composer.core.services.ReactorSinkService;
import org.composer.core.stubs.SyncProcessorStub;
import org.composer.core.utils.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;


import java.util.List;

import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class AMQPRouteTest extends CamelTestSupport {

    @Mock
    UserProcessService businessProcessXService;

    @Mock
    ReactorSinkService reactorSinkService;
    @Spy
    Processor exceptionProcessor = new SyncProcessorStub();

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }


    @Override
    protected RouteBuilder createRouteBuilder() throws Exception{
        return new UserRoutes(businessProcessXService, reactorSinkService);
    }


    @Test
    public void aMQPRouteTest() throws Exception{

        RouteDefinition route = context.getRouteDefinition(UserRouteNames.AMQP.name);
        String amqpInput = "buddy!";

        AMQPModelGroupDto amqpGroupDto = AMQPModelGroupDto.builder().groupId("A").groupName("B").userId("ABCD").build();
        AMQPModelUserDto amqpDto = AMQPModelUserDto.builder().groups(List.of(amqpGroupDto)).name("A").age(15).userId("ABCD").build();
        adviceWith(route, context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById("AMQP_Async_Processor").replace().setBody(exchange->{

                    return AMQPFutureProcessor.setBody(exchange,new  AMQPModelUserDto[] {amqpDto});
                });
                weaveAddLast().to("mock:finishAMQPRoute");
            }
        });
        context.start();

        doAnswer((Answer<Void>) invocation->{
            Exchange exchange = invocation.getArgument(0);
            CompareUsersModel body =  exchange.getMessage().getBody(CompareUsersModel.class);
            assertEquals(body.getAmqp_step().getOutput(),amqpDto);

            return null;
        }).when(reactorSinkService).notifyAboutAMQPStep(any());


        MockEndpoint mock = getMockEndpoint("mock:finishAMQPRoute");
        String taskId="abcdef";
        String grpcInput= "Ann";


        String restInput = "Mark";
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())
                .build();

        mock.setExpectedMessageCount(1);
        template.sendBody("direct:"+UserRouteNames.AMQP.name,model );
        mock.assertIsSatisfied();
        Message message = mock.getExchanges().get(0).getMessage();
        CompareUsersModel modelOut = message.getBody(CompareUsersModel.class);

        verify(reactorSinkService).notifyAboutAMQPStep(any());
        assertEquals(modelOut.getAmqp_step().getOutput(),List.of( GetUserModel.fromDto(amqpDto)));

    }

    @Test
    /*
    This test tests if an AMQP processor throws an exception, the exception first get caught by the onException
    clause, then propagates down the original route.
     */
    public void aMQPRouteErrorTest() throws Exception{
        RouteDefinition route = context.getRouteDefinition(UserRouteNames.AMQP.name);
        String amqpInput = "buddy!";
        String amqpOutput = "Hello "+amqpInput+"!";
        String exceptionMsg = "Ooops!";
        RuntimeException exception = new RuntimeException(exceptionMsg);
        adviceWith(route, context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById("AMQP_Async_Processor").replace().process(exchange -> {
                            throw exception;
                        });
                weaveById("X_exception_processor").replace().process(exceptionProcessor);


                weaveAddLast().to("mock:finishAMQPRoute");
            }
        });

        context.start();
        doAnswer((Answer<Void>) invocation->{
            Exchange exchange = invocation.getArgument(0);
            CompareUsersModel body =  exchange.getMessage().getBody(CompareUsersModel.class);
            assertNull(body.getAmqp_step().getOutput());

            return null;
        }).when(reactorSinkService).notifyAboutAMQPStep(any(Exchange.class));

        MockEndpoint mock = getMockEndpoint("mock:finishAMQPRoute");
        String taskId="abcdef";
        String grpcInput= "Ann";

        String restInput = "Mark";
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())
                .build();

        mock.setExpectedMessageCount(1);
        template.sendBody("direct:"+UserRouteNames.AMQP.name,model );
        mock.assertIsSatisfied();

        verify(reactorSinkService).notifyAboutAMQPStep(any());

        verify(exceptionProcessor).process(any(Exchange.class));
    }
    @Test
    /*
    This test tests if an AMQP processor throws an exception, the exception first get caught by the onException
    clause; then the exchange continues down the original route.

    Next, another exception is thrown as we call a reactorSinkService.notifyAboutAMQPStep(any()) method.

    So, our exception processor should be called twice.
     */
    public void aMQPRoute2ErrorTest() throws Exception{
        RouteDefinition route = context.getRouteDefinition(UserRouteNames.AMQP.name);
        String amqpInput = "buddy!";

        String exceptionMsg = "Ooops!";
        RuntimeException exception = new RuntimeException(exceptionMsg);
        adviceWith(route, context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById("AMQP_Async_Processor").replace().process(exchange -> {
                    throw exception;
                });
                weaveById("X_exception_processor").replace().process(exceptionProcessor);


                weaveAddLast().to("mock:finishAMQPRoute");
            }
        });

        context.start();
        doAnswer((Answer<Void>) invocation->{
            Exchange exchange = invocation.getArgument(0);
            CompareUsersModel body =  exchange.getMessage().getBody(CompareUsersModel.class);
            assertNull(body.getAmqp_step().getOutput());
            throw new RuntimeException();

        }).when(reactorSinkService).notifyAboutAMQPStep(any(Exchange.class));

        MockEndpoint mock = getMockEndpoint("mock:finishAMQPRoute");
        String taskId="abcdef";
        String grpcInput= "Ann";


        String restInput = "Mark";
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())
                .build();

        mock.setExpectedMessageCount(1);
        template.sendBody("direct:"+UserRouteNames.AMQP.name,model );
        mock.assertIsSatisfied();



        verify(reactorSinkService).notifyAboutAMQPStep(any());

        verify(exceptionProcessor,times(2)).process(any(Exchange.class));
    }


}
