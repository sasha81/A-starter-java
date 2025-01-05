package org.composer.adapter.resolvers;

import org.composer.adapter.dto.TaskOutput;
import org.composer.adapter.dto.TaskInput;
import org.composer.adapter.services.IFluxProcessingService;
import org.composer.adapter.services.ISendToCamelService;
import org.composer.core.model.FluxMessageContainer;
import org.composer.core.model.XTaskModel;
import org.composer.core.utils.ISinkMapObjectService;
import org.reactivestreams.Publisher;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import java.io.IOException;
import java.util.List;

@Controller
public class XTaskSubscriber {

    private IFluxProcessingService fluxProcessingService;
    private ISendToCamelService sendToCamelService;
    private ISinkMapObjectService sinkMapService;

    public XTaskSubscriber(ISinkMapObjectService sinkMapService, ISendToCamelService sendToCamelService, IFluxProcessingService fluxProcessingService) {
       this.sinkMapService = sinkMapService;
        this.sendToCamelService = sendToCamelService;
        this.fluxProcessingService = fluxProcessingService;
    }

    @QueryMapping
    public String greet(String name){
        //Uncomment the exception to see how the system handles exceptions
        //throw new IllegalArgumentException("badRequest received");
        return "Hello "+name+" from a Spring Boot Gql server!";
    }


   @SubscriptionMapping("test")
    public Publisher<String> test(@Argument String num){
        List<String> nums = List.of(num,num,num);
        return Flux.fromStream(nums.stream());
    }

   @SubscriptionMapping("newXtask")
    public Flux<TaskOutput> newXtask(@Argument TaskInput xTaskDto ) throws IOException {
        String taskId = fluxProcessingService.getTaskId();
        XTaskModel model = fluxProcessingService.getXModelFromDto(xTaskDto, taskId);

        Flux<FluxMessageContainer<?>> rawFlux = sinkMapService.getNewFluxWithId(taskId);
        Flux<TaskOutput> outFlux = fluxProcessingService.postProcessContainerFlux(rawFlux,taskId,()->sinkMapService.deleteMap(taskId) );

                this.sendToCamelService.sendBodyToCamel("direct:new_X_task",model);

            return outFlux;

    }

}
