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
import org.composer.core.model.Specs;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.services.ISpecToModel;
import org.composer.core.services.ReactorSinkService;
import org.composer.core.services.RestFutureProcessor;
import org.composer.core.services.SpecToModel;
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

    private  final ISpecToModel specToModel= new SpecToModel();
    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new BusinessProcessXRoute(businessProcessXService, reactorSinkService, specToModel);
    }


    @Test
    public void RstRouteTest() throws Exception {
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
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
            CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
            assertEquals(body.getCurrentTask().getOutput(), restOutput);
            var  currentTask = (Task<String, String, List<ModelUser>>)body.getCurrentTask();
            assertEquals(currentTask.getOutput().get(0).getUserId(),restUserDto.getUserId());


            return null;
        }).when(reactorSinkService).notifyAboutRestStep(any());

        MockEndpoint mock = getMockEndpoint("mock:finishRestRoute");


        CompareUsersModel model = UtilModelFromSpec.getModelFromSpecs(specs,"X_Rest_step");
        model.setNextTask();

        mock.setExpectedMessageCount(1);
        template.sendBody("direct:X_Rest_step", model);
        mock.assertIsSatisfied();
        Message message = mock.getExchanges().get(0).getMessage();
        CompareUsersModel modelOut = message.getBody(CompareUsersModel.class);
        assertEquals(modelOut.getCurrentTask().getOutput(), List.of( GetUserModel.fromDto(restUserDto)));
    }

    @Test
    public void RstRouteWithErrorTest() throws Exception {
        String errorMsg = "Ooops!";
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
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
            CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
            assertEquals(body.getCurrentTask().getOutput(), restOutput);
            var  currentTask = (Task<String, String, List<ModelUser>>)body.getCurrentTask();
            assertEquals(currentTask.getOutput().get(0).getUserId(),restUserDto.getUserId());


            return null;
        }).when(reactorSinkService).notifyAboutRestStep(any());

        MockEndpoint mock = getMockEndpoint("mock:finishRestRoute");


        CompareUsersModel model = UtilModelFromSpec.getModelFromSpecs(specs,"X_Rest_step");
        model.setNextTask();
        model.getCurrentTask().setErrorMessage(errorMsg);
        mock.setExpectedMessageCount(1);
        template.sendBody("direct:X_Rest_step", model);
        mock.assertIsSatisfied();
        Message message = mock.getExchanges().get(0).getMessage();
        CompareUsersModel modelOut = message.getBody(CompareUsersModel.class);
        var  currentTask = (Task<String, String, List<ModelUser>>)modelOut.getCurrentTask();

        assertEquals(currentTask.getErrorMessage(),errorMsg);

    }

}
