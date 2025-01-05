package org.composer.adapter.filters;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.security.auth.message.AuthException;
import org.composer.adapter.services.FluxProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;


@Component
public class CustomErrorMessageExceptionResolver extends DataFetcherExceptionResolverAdapter {
    Logger logger = LoggerFactory.getLogger(CustomErrorMessageExceptionResolver.class);
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        logger.error("ERROR OCCURRED in COMPOSER! Cause: "+ex.getMessage());

        GraphQLError error = GraphqlErrorBuilder.newError(env)
                .errorType(ErrorType.INTERNAL_ERROR)
                .message("There are some internal error! "+ex.getMessage())
                .build();

        return error;
    }
}
