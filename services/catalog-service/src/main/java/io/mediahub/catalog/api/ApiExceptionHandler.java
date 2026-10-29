package io.mediahub.catalog.api;

import io.mediahub.catalog.media.MediaNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps domain failures to RFC 9457 problem responses. Validation errors (400) are already
 * handled by the base class once {@code spring.mvc.problemdetails.enabled} is on.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(MediaNotFoundException.class)
    ProblemDetail notFound(MediaNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    ProblemDetail duplicate() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "A media item with this identifier already exists");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail concurrentUpdate() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "The media item was changed by someone else; reload and retry");
    }
}
