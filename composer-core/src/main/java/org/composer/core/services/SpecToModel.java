package org.composer.core.services;

import org.composer.core.model.ContainerResults;
import org.composer.core.model.ModelUser;
import org.composer.core.model.Specs;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.routes.UserRouteNames;
import org.composer.core.utils.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class SpecToModel implements ISpecToModel{


    @Override
    public CompareUsersModel getModelFromSpecs(Specs specs) throws Exception{
   //     throw new Exception("Oops! Failed to init a Compare Task");
        List<Task<String, String, ?>> taskList = new ArrayList<>();
        taskList.add(Task.<String, String, List<ModelUser>>builder().executor(UserRouteNames.REST.name).input(specs.getSpecifications()).build());
        taskList.add(Task.<String, String, List<ModelUser>>builder().executor(UserRouteNames.AMQP.name).input(specs.getSpecifications()).build());
        taskList.add(Task.<String, String, List<ModelUser>>builder().executor(UserRouteNames.GRPC.name).input(specs.getSpecifications()).build());
        taskList.add(Task.<String, String, ContainerResults>builder().executor(UserRouteNames.RESULT.name).input(specs.getSpecifications()).build());
        var taskModel = CompareUsersModel.builder()
                .task_id(specs.getTaskId())
                .taskList(taskList)
                .build();
        taskModel.setNextTask();
            return taskModel;

    }


}
