package org.composer.adapter.services;


import org.composer.adapter.dto.*;
import org.composer.core.model.*;
import org.composer.core.utils.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
@Service
public class FluxProcessingService implements IFluxProcessingService  {
    Logger logger = LoggerFactory.getLogger(FluxProcessingService.class);

    @Override
    public Specs getSpecsFromInput(TaskInput input) {
        return Specs.builder().taskId(getTaskId()).specifications(input.getSpecifics()).build();
    }

    public String getTaskId(){
        return UUID.randomUUID().toString();
    }

//    public XTaskModel getXModelFromDto(TaskInput dto, String taskId){
//        return XTaskModel.builder()
//                .task_id(taskId)
//                .rest_step(Task.<String, String, List<ModelUser>>builder().input(dto.getSpecifics()).build())
//                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(dto.getSpecifics()).build())
//                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(dto.getSpecifics()).build())
//                .build();
//    }
    public Flux<TaskOutput> postProcessContainerFlux(Flux<FluxMessageContainer<?>> inFlux, String taskId, Runnable doOnCancel){
        Flux<TaskOutput> outFlux = inFlux
                .filter(msg-> msg.getTaskId().equals(taskId))
                .takeUntil(msg->msg.getStage().equals(ProcessStages.STOP))
                .map(this::getTaskOutput)
                .doOnTerminate(() -> {
                    logger.info("doOnTerminate called");
                    doOnCancel.run();})

                ;
        return outFlux;
    }


    public TaskOutput getTaskOutput(FluxMessageContainer<?> input ){
        ProcessStages stage = input.getStage();
        Object content = input.getContent();
        if(input.getError()!=null) {
            return TaskOutput.builder().taskId(input.getTaskId()).content("").error(input.getError()).stage(stage).build();
        }
        if((stage == ProcessStages.GRPC || stage == ProcessStages.AMQP || stage == ProcessStages.REST)&& fluxContentSatisfiesCondition(content)){

            List<ModelUser> userArr =  parseFluxContent(content);
            List<UserDto> userDtos = userArr.stream()
                    .map(this::modelToDto)
                    .toList();
            return TaskOutput.builder().taskId(input.getTaskId())
                    .content(ContainerUser.builder().userViewContent(userDtos).build())
                    .stage(input.getStage()).build();

        }
        else if (stage == ProcessStages.FINISH){

            return TaskOutput.builder().taskId(input.getTaskId())
                    .content(content).stage(input.getStage()).build();
        }
        else{
            return TaskOutput.builder().taskId(input.getTaskId())
                    .content(content).stage(input.getStage()).build();
        }
    }

    public boolean fluxContentSatisfiesCondition(Object content){
        if(content instanceof Collection<?>) return true;
        else return false;
    }

    public List<ModelUser> parseFluxContent(Object content){
        return ((Collection<?>) content).stream().map(el-> (ModelUser)el).toList();
    }
    public UserDto modelToDto(ModelUser u){
        return UserDto.builder()
                .age(u.getUserage())
                .name(u.getUsername())
                .userId(u.getUserId())
                .groups(u.getGroups().stream()
                        .map(g-> GroupDto.builder()
                                .groupId(g.getGroupId())
                                .groupName(g.getGroupName())
                                .groupStatus(g.getGroupStatus())
                                .userId(g.getUserId())
                                .userStatus(g.getUserStatus())
                                .build())
                        .toList())
                .build();
    }


}
