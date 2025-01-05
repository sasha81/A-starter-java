package org.composer.adapter.filters;


import graphql.GraphQLError;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.language.Field;
import graphql.schema.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CustomErrorMessageResolverTest {

    private CustomErrorMessageExceptionResolver customErrorMessageExceptionResolver = new CustomErrorMessageExceptionResolver();


    @Test
    public void getGQLError(){
        String errorMsg = "badRequest received";
        Throwable exception = new IllegalArgumentException(errorMsg);
        DataFetchingEnvironment env = new DataFetchingEnvironmentImpl.Builder()
                .executionStepInfo(ExecutionStepInfo.newExecutionStepInfo()
                        .type(GraphQLEnumType.newEnum().name("B").build())
                        .build())
                .mergedField(MergedField.newMergedField()

                        .addField(Field.newField().name("A").build())

                        .build())

                .build();
        GraphQLError error = customErrorMessageExceptionResolver.resolveToSingleError(exception, env);
        assertTrue(error.getMessage().contains(errorMsg));
    }
}
