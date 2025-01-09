package org.composer.core.services;


import org.composer.core.model.Specs;
import org.composer.core.model.XTaskModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class SpecToModelTest {


    private final ISpecToModel specToModel = new SpecToModel();
    @Test
    public void specToModelTest(){
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        XTaskModel model = specToModel.getModelFromSpecs(specs);
        assertEquals(model.getTask_id(),specs.getTaskId());
        assertEquals(model.getNextTask().getExecutor(),model.getTaskList().get(0).getExecutor());

    }
}
