package org.composer.core.processors;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.engine.SimpleCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.model.Specs;
import org.composer.core.services.ISpecToModel;
import org.composer.core.services.InitCompareUserModel;
import org.composer.core.services.RestFutureProcessor;
import org.composer.core.services.SpecToModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class InitCompareUserModelTest {
    ISpecToModel specToModel = new SpecToModel();
    InitCompareUserModel initCompareUserModel = new InitCompareUserModel(specToModel);
    @Test
    public void initCompareUserModelTest() throws Exception {
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        CamelContext camelContext = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getMessage().setBody(specs);
        initCompareUserModel.process(exchange);
        CompareUsersModel modelOut = exchange.getMessage().getBody(CompareUsersModel.class);
        assertEquals(modelOut.getCurrentTask().getInput(),specs.getSpecifications());
        assertEquals(modelOut.getTaskList().size(),4);
    }

}
