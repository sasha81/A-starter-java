package org.composer.core.routes;


import org.composer.core.model.ModelUser;
import org.composer.core.model.Specs;
import org.composer.core.services.ISpecToModel;
import org.composer.core.services.SpecToModel;
import org.composer.core.stubs.UtilModelFromSpec;
import users.Users;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.services.GRPCRunnableAsyncProcessor;
import org.composer.core.services.ReactorSinkService;
import org.composer.core.utils.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;


import java.util.List;

import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
public class GRPCRouteTest extends CamelTestSupport{

    @Mock
    BusinessProcessXService businessProcessXService;

    @Mock
    ReactorSinkService reactorSinkService;

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }
    private  final ISpecToModel specToModel= new SpecToModel();

@Override
protected RouteBuilder createRouteBuilder() throws Exception{
        return new BusinessProcessXRoute(businessProcessXService, reactorSinkService, specToModel);
}



    @Test
    public void gRPCRouteTest() throws Exception {
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        RouteDefinition route = context.getRouteDefinition("X_GRPC_step");
        Users.Group group = Users.Group.newBuilder().setGroupId("12345")
                .setGroupname("Physics").setUserId("ABC").build();
        Users.UserViewDto userViewDto = Users.UserViewDto.newBuilder()
                .setUserId("ABC").setUsername("Sasha")
                .setUserage(30).addGroups(group).build();
        Users.UsersWithGroupsDto grpcResponse = Users.UsersWithGroupsDto.newBuilder()
                .addAllUsersWithGroups(List.of(userViewDto)).build();

        adviceWith(route, context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById("GRPC_Async_Processor").replace().setBody(exchange->{
                    return GRPCRunnableAsyncProcessor.setBody(exchange,grpcResponse);
                });
                weaveAddLast().to("mock:finishGRPCRoute");
            }
        });
        context.start();

        doAnswer((Answer<Void>) invocation->{
            Exchange exchange = invocation.getArgument(0);
            CompareUsersModel body =  exchange.getMessage().getBody(CompareUsersModel.class);
            assertEquals(body.getCurrentTask().getOutput(),grpcResponse);
            var  currentTask = (Task<String, String, List<ModelUser>>)body.getCurrentTask();
            assertEquals(currentTask.getOutput().get(0).getUserId(),grpcResponse.getUsersWithGroups(0).getUserId());

            return null;
        }).when(reactorSinkService).notifyAboutGRPCStep(any());

        MockEndpoint mock = getMockEndpoint("mock:finishGRPCRoute");

        CompareUsersModel model = UtilModelFromSpec.getModelFromSpecs(specs,"X_GRPC_step");
        model.setNextTask();

        mock.setExpectedMessageCount(1);
        template.sendBody("direct:X_GRPC_step",model );
        mock.assertIsSatisfied();
        Message message = mock.getExchanges().get(0).getMessage();
        CompareUsersModel modelOut = message.getBody(CompareUsersModel.class);
        var  currentTask = (Task<String, String, List<ModelUser>>)modelOut.getCurrentTask();
        assertEquals(currentTask.getOutput().get(0).getUserId(),userViewDto.getUserId());
        assertEquals(currentTask.getOutput().get(0).getUsername(),userViewDto.getUsername());
        assertEquals(currentTask.getOutput().get(0).getGroups().get(0).getGroupId(),group.getGroupId());
        assertEquals(currentTask.getOutput().get(0).getGroups().get(0).getUserId(),group.getUserId());


    }

    @Test
    public void gRPCRouteWithErrorTest() throws Exception {
        String errorMsg = "Ooops!";
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        RouteDefinition route = context.getRouteDefinition("X_GRPC_step");
        Users.Group group = Users.Group.newBuilder().setGroupId("12345")
                .setGroupname("Physics").setUserId("ABC").build();
        Users.UserViewDto userViewDto = Users.UserViewDto.newBuilder()
                .setUserId("ABC").setUsername("Sasha")
                .setUserage(30).addGroups(group).build();
        Users.UsersWithGroupsDto grpcResponse = Users.UsersWithGroupsDto.newBuilder()
                .addAllUsersWithGroups(List.of(userViewDto)).build();

        adviceWith(route, context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById("GRPC_Async_Processor").replace().setBody(exchange->{
                    return GRPCRunnableAsyncProcessor.setBody(exchange,grpcResponse);
                });
                weaveAddLast().to("mock:finishGRPCRoute");
            }
        });
        context.start();

        doAnswer((Answer<Void>) invocation->{
            Exchange exchange = invocation.getArgument(0);
            CompareUsersModel body =  exchange.getMessage().getBody(CompareUsersModel.class);
            assertEquals(body.getCurrentTask().getOutput(),grpcResponse);
            var  currentTask = (Task<String, String, List<ModelUser>>)body.getCurrentTask();
            assertEquals(currentTask.getOutput().get(0).getUserId(),grpcResponse.getUsersWithGroups(0).getUserId());

            return null;
        }).when(reactorSinkService).notifyAboutGRPCStep(any());

        MockEndpoint mock = getMockEndpoint("mock:finishGRPCRoute");

        CompareUsersModel model = UtilModelFromSpec.getModelFromSpecs(specs,"X_GRPC_step");
        model.setNextTask();
        model.getCurrentTask().setErrorMessage(errorMsg);
        mock.setExpectedMessageCount(1);
        template.sendBody("direct:X_GRPC_step",model );
        mock.assertIsSatisfied();
        Message message = mock.getExchanges().get(0).getMessage();
        CompareUsersModel modelOut = message.getBody(CompareUsersModel.class);
        var  currentTask = (Task<String, String, List<ModelUser>>)modelOut.getCurrentTask();

        assertEquals(currentTask.getErrorMessage(),errorMsg);



    }


}
