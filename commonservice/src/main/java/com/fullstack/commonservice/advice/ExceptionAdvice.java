package com.fullstack.commonservice.advice;

import com.fullstack.commonservice.response.ResponseError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.computeIfAbsent(error.getField(), key -> new ArrayList<>())
                        .add(error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(new ResponseError(
                "VALIDATION_ERROR",
                "Du lieu khong hop le",
                errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseError> handleBusinessException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ResponseError(
                "BUSINESS_ERROR",
                ex.getMessage(),
                Collections.emptyMap()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseError> handleNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseError(
                "NOT_FOUND",
                ex.getMessage(),
                Collections.emptyMap()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResponseError> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseError(
                "UNAUTHORIZED",
                ex.getMessage(),
                Collections.emptyMap()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseError> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseError(
                "INTERNAL_SERVER_ERROR",
                "Da xay ra loi he thong",
                Collections.emptyMap()));
    }
}
