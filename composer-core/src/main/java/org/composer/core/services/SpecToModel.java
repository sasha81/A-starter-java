package org.composer.core.services;

import org.composer.core.model.ContainerResults;
import org.composer.core.model.ModelUser;
import org.composer.core.model.Specs;
import org.composer.core.model.XTaskModel;
import org.composer.core.utils.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class SpecToModel implements ISpecToModel{


    @Override
    public XTaskModel getModelFromSpecs(Specs specs) {
        List<Task<String, String, ?>> taskList = new ArrayList<>();
        taskList.add(Task.<String, String, List<ModelUser>>builder().executor("X_Rest_step").input(specs.getSpecifications()).build());
        taskList.add(Task.<String, String, List<ModelUser>>builder().executor("X_AMQP_step").input(specs.getSpecifications()).build());
        taskList.add(Task.<String, String, List<ModelUser>>builder().executor("X_GRPC_step").input(specs.getSpecifications()).build());
        taskList.add(Task.<String, String, ContainerResults>builder().executor("X_finish").input(specs.getSpecifications()).build());
        var taskModel = XTaskModel.builder()
                .task_id(specs.getTaskId())
                .taskList(taskList)
                .build();
        taskModel.setNextTask();
            return taskModel;

    }

    @Override
    public List<String> getExecutionSequence(Specs specs) {
        var model = getModelFromSpecs(specs);
        List<String> list = model.getTaskList().stream().map(Task::getExecutor).toList();
        return list;
    }
}
