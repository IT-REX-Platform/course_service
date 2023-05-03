package de.unistuttgart.iste.gits.template.exception;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TemplateExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(@NonNull Throwable ex, @NonNull DataFetchingEnvironment env) {
        if (ex instanceof ChangeSetPersister.NotFoundException) {
            return handleNotFoundException((ChangeSetPersister.NotFoundException) ex);
        } else {
            return handleGenericException(ex);
        }
    }

    private GraphQLError handleNotFoundException(ChangeSetPersister.NotFoundException ex) {
        log.error("Entity not found", ex);
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.DataFetchingException)
                .message(ex.getMessage())
                .build();
    }

    private GraphQLError handleGenericException(Throwable ex) {
        log.error("Unexpected error", ex);
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.ExecutionAborted)
                .message(ex.getMessage())
                .build();
    }
}
