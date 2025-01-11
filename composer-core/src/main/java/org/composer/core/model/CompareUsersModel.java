package org.composer.core.model;

import lombok.Builder;
import lombok.Data;
import org.composer.core.utils.Task;


import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class CompareUsersModel implements Serializable {
    private String task_id;
    //It should be a taskGraph in a real life app
    List<Task<String, String,?>> taskList;
    Task<String, String,?> currentTask;


    public void setNextTask(){
        this.currentTask = getNextTask();
    }
    public Task<String, String,?> getNextTask(){
        for(int i=taskList.size()-1; i>=0; i--){

            var tempTask= this.taskList.get(i);
            if(tempTask.getErrorMessage()!=null || tempTask.getOutput()!=null ){
                return this.taskList.get(i+1);
            }
        }
        return this.taskList.get(0);
    }
}
