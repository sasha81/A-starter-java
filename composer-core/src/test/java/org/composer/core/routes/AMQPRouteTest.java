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
import org.composer.core.model.Specs;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.services.*;
import org.composer.core.stubs.SyncProcessorStub;
import org.composer.core.stubs.UtilModelFromSpec;
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
    BusinessProcessXService businessProcessXService;

    @Mock
    ReactorSinkService reactorSinkService;
    @Spy
    Processor exceptionProcessor = new SyncProcessorStub();



    private  final ISpecToModel specToModel= new SpecToModel();
    @Override
    public boolean isUseAdviceWith() {
        return true;
    }


    @Override
    protected RouteBuilder createRouteBuilder() throws Exception{
        return new UserRoutes(businessProcessXService, reactorSinkService, specToModel);
    }


    @Test
    public void aMQPRouteTest() throws Exception{

        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();

        RouteDefinition route = context.getRouteDefinition(UserRouteNames.AMQP.name);

        AMQPModelGroupDto amqpGroupDto = AMQPModelGroupDto.builder().groupId("A").groupName("B").userId("ABCD").build();
        AMQPModelUserDto amqpDto = AMQPModelUserDto.builder().groups(List.of(amqpGroupDto)).name("A").age(15).userId("ABCD").build();
        adviceWith(route, context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById("AMQP_Async_Processor").replace().setBody(exchange->{

                    CompareUsersModel model = AMQPFutureProcessor.setBody(exchange,new  AMQPModelUserDto[] {amqpDto});
                    return model;
                });
                weaveAddLast().to("mock:finishAMQPRoute");
            }
        });
        context.start();

        doAnswer((Answer<Void>) invocation->{
            Exchange exchange = invocation.getArgument(0);
            CompareUsersModel body =  exchange.getMessage().getBody(CompareUsersModel.class);
            var  currentTask = (Task<String, String, List<ModelUser>>)body.getCurrentTask();
            assertEquals(currentTask.getOutput().get(0).getUserId(),amqpDto.getUserId());

            return null;
        }).when(reactorSinkService).notifyAboutAMQPStep(any());


        MockEndpoint mock = getMockEndpoint("mock:finishAMQPRoute");

        CompareUsersModel model = UtilModelFromSpec.getModelFromSpecs(specs,UserRouteNames.AMQP.name);
        model.setNextTask();

        mock.setExpectedMessageCount(1);
        template.sendBody("direct:X_AMQP_step",model );
        mock.assertIsSatisfied();
        Message message = mock.getExchanges().get(0).getMessage();
        CompareUsersModel modelOut = message.getBody(CompareUsersModel.class);

        verify(reactorSinkService).notifyAboutAMQPStep(any());
        var  currentTask = (Task<String, String, List<ModelUser>>)modelOut.getCurrentTask();
        assertEquals(currentTask.getOutput(),List.of( GetUserModel.fromDto(amqpDto)));

    }
    @Test
    public void aMQPRouteTestWithError() throws Exception{
        String errorMsg = "Ooops!";
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();

        RouteDefinition route = context.getRouteDefinition(UserRouteNames.AMQP.name);

        AMQPModelGroupDto amqpGroupDto = AMQPModelGroupDto.builder().groupId("A").groupName("B").userId("ABCD").build();
        AMQPModelUserDto amqpDto = AMQPModelUserDto.builder().groups(List.of(amqpGroupDto)).name("A").age(15).userId("ABCD").build();
        adviceWith(route, context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById("AMQP_Async_Processor").replace().setBody(exchange->{

                    CompareUsersModel model = AMQPFutureProcessor.setBody(exchange,new  AMQPModelUserDto[] {amqpDto});
                    return model;
                });
                weaveAddLast().to("mock:finishAMQPRoute");
            }
        });
        context.start();

        doAnswer((Answer<Void>) invocation->{
            Exchange exchange = invocation.getArgument(0);
            CompareUsersModel body =  exchange.getMessage().getBody(CompareUsersModel.class);
            var  currentTask = (Task<String, String, List<ModelUser>>)body.getCurrentTask();
            assertEquals(currentTask.getOutput().get(0).getUserId(),amqpDto.getUserId());

            return null;
        }).when(reactorSinkService).notifyAboutAMQPStep(any());


        MockEndpoint mock = getMockEndpoint("mock:finishAMQPRoute");

        CompareUsersModel model = UtilModelFromSpec.getModelFromSpecs(specs,UserRouteNames.AMQP.name);
        model.setNextTask();
        model.getCurrentTask().setErrorMessage(errorMsg);
        mock.setExpectedMessageCount(1);
        template.sendBody("direct:"+UserRouteNames.AMQP.name,model );
        mock.assertIsSatisfied();
        Message message = mock.getExchanges().get(0).getMessage();
        CompareUsersModel modelOut = message.getBody(CompareUsersModel.class);

        verify(reactorSinkService).notifyAboutAMQPStep(any());
        var  currentTask = (Task<String, String, List<ModelUser>>)modelOut.getCurrentTask();
        assertEquals(currentTask.getErrorMessage(),errorMsg);

    }



}
