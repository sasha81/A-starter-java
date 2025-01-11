package org.composer.core.processors;


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.engine.SimpleCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.model.Specs;
import org.composer.core.services.SetNextTaskProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.composer.core.stubs.UtilModelFromSpec.getModelFromSpecs;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class SetNextTaskProcessorTest {

    SetNextTaskProcessor setNextTaskProcessor = new SetNextTaskProcessor();

    @Test
    public void setNextTaskProcessorTest() throws Exception {
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        String executor= "exec_1";
        CompareUsersModel model = getModelFromSpecs(specs,  executor);
        CamelContext camelContext = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getMessage().setBody(model);
        setNextTaskProcessor.process(exchange);
        CompareUsersModel modelOut = exchange.getMessage().getBody(CompareUsersModel.class);
        assertEquals(modelOut.getCurrentTask().getExecutor(),executor);
        assertEquals(modelOut.getCurrentTask().getInput(),specs.getSpecifications());

    }
}
