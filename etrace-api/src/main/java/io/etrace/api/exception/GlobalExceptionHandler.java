package io.etrace.api.exception;

import io.etrace.api.model.ExceptionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(Exception ex) {
        logger.error("handleException:", ex);
        ExceptionEntity entity = new ExceptionEntity();
        entity.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        entity.setReasonPhrase(HttpStatus.INTERNAL_SERVER_ERROR.name());
        entity.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(entity);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity handleNotFoundException(NotFoundException ex) {
        logger.error("handleNotFoundException:", ex);
        ExceptionEntity entity = new ExceptionEntity();
        entity.setStatus(HttpStatus.NOT_FOUND.value());
        entity.setReasonPhrase(HttpStatus.NOT_FOUND.name());
        entity.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(entity);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity handleBadRequestException(BadRequestException ex) {
        logger.error("handleBadRequestException:", ex);
        ExceptionEntity entity = new ExceptionEntity();
        entity.setStatus(HttpStatus.BAD_REQUEST.value());
        entity.setReasonPhrase(HttpStatus.BAD_REQUEST.name());
        entity.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(entity);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity handleUnauthorizedException(UnauthorizedException ex) {
        logger.error("handleUnauthorizedException:", ex);
        ExceptionEntity entity = new ExceptionEntity();
        entity.setStatus(HttpStatus.UNAUTHORIZED.value());
        entity.setReasonPhrase(HttpStatus.UNAUTHORIZED.name());
        entity.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(entity);
    }

    @ExceptionHandler(UserForbiddenException.class)
    public ResponseEntity handleUserForbiddenException(UserForbiddenException ex) {
        logger.error("handleUserForbiddenException:", ex);
        ExceptionEntity entity = new ExceptionEntity();
        entity.setStatus(HttpStatus.FORBIDDEN.value());
        entity.setReasonPhrase(HttpStatus.FORBIDDEN.name());
        entity.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(entity);
    }

}
