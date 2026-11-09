package com.company.deal.adapter.in.web;

import com.company.deal.domain.exception.DealBusinessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.company.deal")
public class DealExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DealBusinessException.class)
    public ResponseEntity<ProblemDetail> handleDealBusinessException(DealBusinessException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setType(URI.create("https://api.company.com/errors/deal/" + ex.getErrorCode().getCode()));
        problem.setTitle(ex.getErrorCode().name());
        problem.setProperty("errorCode", ex.getErrorCode().getCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(ex.getStatus()).body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> String.format("%s: %s", err.getField(), err.getDefaultMessage()))
            .collect(Collectors.joining(", "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, validationErrors);
        problem.setTitle("Deal Validation Failed");
        problem.setProperty("errorCode", "DEAL_VALIDATION_ERROR");
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problem);
    }
}
