package org.composer.core.model;

import lombok.Builder;
import lombok.Data;
import org.composer.core.utils.Task;


import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class XTaskModel implements Serializable {
    private String task_id;
    private Task<String, String,List<ModelUser>> rest_step;
    private Task<String, String, List<ModelUser>> amqp_step;
    private Task<String, String, List<ModelUser>> grpc_step;
    private String final_result;
}
