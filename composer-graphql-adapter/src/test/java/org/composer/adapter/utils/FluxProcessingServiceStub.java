package org.composer.adapter.utils;

import org.composer.adapter.services.FluxProcessingService;

public class FluxProcessingServiceStub extends FluxProcessingService {
    private String taskId;

    public void setTaskId(String taskId){
        this.taskId = taskId;
    }
    public String getTaskId(){
        return this.taskId;
    }


}
