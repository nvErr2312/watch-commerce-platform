package com.fullstack.commonservice.advice;

import com.fullstack.commonservice.response.ResponseError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.computeIfAbsent(error.getField(), key -> new ArrayList<>())
                        .add(error.getDefaultMessage()));

        ResponseError responseError = new ResponseError(
                "VALIDATION_ERROR",
                "Dữ liệu không hợp lệ",
                errors);

        return ResponseEntity.badRequest().body(responseError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseError> handleBusinessException(
            IllegalArgumentException ex) {
        ResponseError responseError = new ResponseError(
                "BUSINESS_ERROR",
                ex.getMessage(),
                Collections.emptyMap());

        return ResponseEntity.badRequest().body(responseError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseError> handleException(Exception ex) {
        log.error("Unhandled exception", ex);

        ResponseError responseError = new ResponseError(
                "INTERNAL_SERVER_ERROR",
                "Đã xảy ra lỗi hệ thống",
                Collections.emptyMap());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(responseError);
    }
}
