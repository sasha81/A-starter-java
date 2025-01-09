package org.composer.core.stubs;

import org.composer.core.model.ContainerResults;
import org.composer.core.model.ModelUser;
import org.composer.core.model.Specs;
import org.composer.core.model.XTaskModel;
import org.composer.core.utils.Task;

import java.util.ArrayList;
import java.util.List;

public class UtilModelFromSpec {
    public static XTaskModel getModelFromSpecs(Specs specs, String executor) {
        List<Task<String, String, ?>> taskList = new ArrayList<>();
        taskList.add(Task.<String, String, List<ModelUser>>builder().executor(executor).input(specs.getSpecifications()).build());
        var taskModel = XTaskModel.builder()
                .task_id(specs.getTaskId())
                .taskList(taskList)
                .build();
        taskModel.setNextTask();
        return taskModel;

    }
}
