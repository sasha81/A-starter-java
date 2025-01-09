package org.composer.core.services;


import org.apache.camel.*;
import org.apache.camel.clock.Clock;
import org.apache.camel.impl.engine.SimpleCamelContext;
import org.apache.camel.spi.UnitOfWork;
import org.apache.camel.support.DefaultExchange;
import org.composer.core.model.*;
import org.composer.core.stubs.SinkObjectServiceStub;
import org.composer.core.utils.ISinkMapObjectService;
import org.composer.core.utils.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReactorSinkServiceTest {

    private final SinkObjectServiceStub sinkMapService = new SinkObjectServiceStub();
    private final IReactorSinkService reactorSinkService = new ReactorSinkService(sinkMapService);
    private  final ISpecToModel specToModel= new SpecToModel();

    private final ModelToFlux modelToFluxService= new ModelToFlux();

    @Test
    public void notifyAboutRestStepNoErrorTest(){
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        String userId = "123456";

        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        XTaskModel model = specToModel.getModelFromSpecs(specs);
        model.setNextTask();
        var  currentTask = (Task<String, String, List<ModelUser>>)model.getCurrentTask();
        currentTask.setOutput(userList);

        sinkMapService.setPublishConsumer((tskId, container)->{
            assertEquals(tskId,specs.getTaskId());
            assertEquals(container.getContent(),userList);

        });
        CamelContext context = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getMessage().setBody(model);

        reactorSinkService.notifyAboutRestStep(exchange);
    }

    @Test
    public void notifyAboutRestStepWithErrorTest(){
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();


        String error = "Ooops!";
        XTaskModel model = specToModel.getModelFromSpecs(specs);
        model.setNextTask();
        var  currentTask = (Task<String, String, List<ModelUser>>)model.getCurrentTask();
        currentTask.setErrorMessage(error);

        sinkMapService.setPublishConsumer((tskId, container)->{
            assertEquals(tskId,specs.getTaskId());
            assertEquals(container.getError(),error);

        });
        CamelContext context = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getMessage().setBody(model);

        reactorSinkService.notifyAboutRestStep(exchange);
    }
    @Test
    public void notifyAboutGrpcStepNoErrorTest(){
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        String userId = "123456";

        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        XTaskModel model = specToModel.getModelFromSpecs(specs);
        model.setNextTask();
        var  currentTask = (Task<String, String, List<ModelUser>>)model.getCurrentTask();
        currentTask.setOutput(userList);

        sinkMapService.setPublishConsumer((tskId, container)->{
            assertEquals(tskId,specs.getTaskId());
            assertEquals(container.getContent(),userList);

        });
        CamelContext context = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getMessage().setBody(model);

        reactorSinkService.notifyAboutGRPCStep(exchange);
    }

    @Test
    public void notifyAboutGrpcStepWithErrorTest(){
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        String taskId = "abcdef";

        String error = "Ooops!";
        XTaskModel model = specToModel.getModelFromSpecs(specs);
        model.setNextTask();
        var  currentTask = (Task<String, String, List<ModelUser>>)model.getCurrentTask();
        currentTask.setErrorMessage(error);

        sinkMapService.setPublishConsumer((tskId, container)->{
            assertEquals(tskId,specs.getTaskId());
            assertEquals(container.getError(),error);

        });
        CamelContext context = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getMessage().setBody(model);

        reactorSinkService.notifyAboutGRPCStep(exchange);
    }
    @Test
    public void notifyAboutAmqpStepNoErrorTest(){
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        String taskId = "abcdef"; String userId = "123456";

        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        XTaskModel model = specToModel.getModelFromSpecs(specs);
        model.setNextTask();
        var  currentTask = (Task<String, String, List<ModelUser>>)model.getCurrentTask();
        currentTask.setOutput(userList);

        sinkMapService.setPublishConsumer((tskId, container)->{
            assertEquals(tskId,specs.getTaskId());
            assertEquals(container.getContent(),userList);

        });
        CamelContext context = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getMessage().setBody(model);

        reactorSinkService.notifyAboutAMQPStep(exchange);
    }

    @Test
    public void notifyAboutAmqpStepWithErrorTest(){
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        String taskId = "abcdef";

        String error = "Ooops!";
        XTaskModel model = specToModel.getModelFromSpecs(specs);
        model.setNextTask();
        var  currentTask = (Task<String, String, List<ModelUser>>)model.getCurrentTask();
        currentTask.setErrorMessage(error);

        sinkMapService.setPublishConsumer((tskId, container)->{
            assertEquals(tskId,specs.getTaskId());
            assertEquals(container.getError(),error);

        });
        CamelContext context = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getMessage().setBody(model);

        reactorSinkService.notifyAboutAMQPStep(exchange);
    }
}
