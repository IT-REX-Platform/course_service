package de.unistuttgart.iste.gits.courseservice.exception;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves exceptions thrown during data fetching.
 * Converts exceptions to {@link GraphQLError}s and logs them.
 */
@Component
@Slf4j
public class ExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(@NonNull Throwable ex, @NonNull DataFetchingEnvironment env) {
        log.error("Exception occurred during data fetching. Class: {}, Message: {}", ex.getClass().getSimpleName(), ex.getMessage());
        log.debug("Exception trace: ", ex);

        return GraphqlErrorBuilder.newError()
                .extensions(buildExtensions(ex))
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .errorType(getErrorType(ex))
                .message(ex.getMessage())
                .build();
    }

    private ErrorType getErrorType(Throwable ex) {
        if (ex instanceof EntityNotFoundException) {
            return ErrorType.DataFetchingException;
        } else if (ex instanceof ValidationException) {
            return ErrorType.ValidationError;
        }
        // HINT add more error types here
        return ErrorType.ExecutionAborted;
    }

    private Map<String, Object> buildExtensions(Throwable ex) {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("exception", ex.getClass().getSimpleName());
        extensions.put("message", ex.getMessage());
        if (ex.getStackTrace().length > 0) {
            extensions.put("thrownBy", ex.getStackTrace()[0].toString());
        }

        return extensions;
    }

}