package org.composer.core.processors;



import org.composer.core.model.ModelUser;
import org.composer.core.model.Specs;
import org.composer.core.services.ISpecToModel;
import org.composer.core.services.SpecToModel;
import users.Users;
import users.UsersServiceGrpc;
import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.engine.SimpleCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.EmptyAsyncCallback;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.services.GRPCRunnableAsyncProcessor;
import org.composer.core.utils.CustomAsyncCamelCallback;
import org.composer.core.utils.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;


import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;

//@CamelSpringBootTest
@ExtendWith(MockitoExtension.class)
public class GRPCProcessorTest {

    private  final ISpecToModel specToModel= new SpecToModel();
    @Mock
    UsersServiceGrpc.UsersServiceStub nestStub;



    @Test
    public void gRPCProcessorTest(){
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();

        CountDownLatch latch = new CountDownLatch(1);
        CamelContext camelContext = new SimpleCamelContext();
        Users.Group group = Users.Group.newBuilder().setGroupId("12345")
                .setGroupname("Physics").setUserId("ABC").build();
        Users.UserViewDto userViewDto = Users.UserViewDto.newBuilder()
                .setUserId("ABC").setUsername("Sasha")
                .setUserage(30).addGroups(group).build();
        Users.UsersWithGroupsDto grpcResponse = Users.UsersWithGroupsDto.newBuilder()
                .addAllUsersWithGroups(List.of(userViewDto)).build();
        GRPCRunnableAsyncProcessor grpcProcessor = new GRPCRunnableAsyncProcessor(nestStub);
        AsyncCallback asyncCamelCallback = new EmptyAsyncCallback();

        doAnswer((Answer<Void>) invocation->{
            CustomAsyncCamelCallback<Users.UsersWithGroupsDto> callback = invocation.getArgument(1);
            callback.onNext(grpcResponse);
            latch.countDown();
            return null;
        }).when(nestStub).findAllUsers(any(),any(CustomAsyncCamelCallback.class));

        Exchange exchange = new DefaultExchange(camelContext);

        String taskId="abcdef";
        String grpcInput= "Ann";

        String amqpInput = "John";
        String restInput = "Mark";
        CompareUsersModel model = specToModel.getModelFromSpecs(specs);
        model.setNextTask();

        exchange.getMessage().setBody(model);
        grpcProcessor.process(exchange,asyncCamelCallback);
        CompareUsersModel modelOut = exchange.getMessage().getBody(CompareUsersModel.class);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        var  currentTask = (Task<String, String, List<ModelUser>>)modelOut.getCurrentTask();
        assertNotNull(currentTask.getOutput());
        assertEquals(currentTask.getOutput().get(0).getUserId(),userViewDto.getUserId());
        assertEquals(currentTask.getOutput().get(0).getUsername(),userViewDto.getUsername());
        assertEquals(currentTask.getOutput().get(0).getGroups().get(0).getGroupId(),group.getGroupId());
        assertEquals(currentTask.getOutput().get(0).getGroups().get(0).getUserId(),group.getUserId());
    }

    @Test
    public void gRPCErrorProcessorTest(){
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        CountDownLatch latch = new CountDownLatch(1);
        CamelContext camelContext = new SimpleCamelContext();
//        Users.Group group = Users.Group.newBuilder().setGroupId("12345")
//                .setGroupname("Physics").setUserId("ABC").build();
//        Users.UserViewDto userViewDto = Users.UserViewDto.newBuilder()
//                .setUserId("ABC").setUsername("Sasha")
//                .setUserage(30).addGroups(group).build();
//        Users.UsersWithGroupsDto grpcResponse = Users.UsersWithGroupsDto.newBuilder()
//                .addAllUsersWithGroups(List.of(userViewDto)).build();
        GRPCRunnableAsyncProcessor grpcProcessor = new GRPCRunnableAsyncProcessor(nestStub);
        AsyncCallback asyncCamelCallback = new EmptyAsyncCallback();
        String errorMsg = "GRPC Error Occurred!";
        Throwable exception = new RuntimeException(errorMsg);
        doAnswer((Answer<Void>) invocation->{
            CustomAsyncCamelCallback<Users.UsersWithGroupsDto> callback = invocation.getArgument(1);
            callback.onError(exception);
            latch.countDown();
            return null;
        }).when(nestStub).findAllUsers(any(),any(CustomAsyncCamelCallback.class));

        Exchange exchange = new DefaultExchange(camelContext);

        String taskId="abcdef";
        String grpcInput= "Ann";

        String amqpInput = "John";
        String restInput = "Mark";
        CompareUsersModel model = specToModel.getModelFromSpecs(specs);
        model.setNextTask();

        exchange.getMessage().setBody(model);
        grpcProcessor.process(exchange,asyncCamelCallback);
        CompareUsersModel modelOut = exchange.getMessage().getBody(CompareUsersModel.class);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertNotNull(exchange.getException());
        assertNull(modelOut.getCurrentTask().getOutput());
        assertEquals(exchange.getException().getMessage(),errorMsg);
    }

}
