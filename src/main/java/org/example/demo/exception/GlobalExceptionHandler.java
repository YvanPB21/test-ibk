package org.example.demo.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  public Mono<ResponseEntity<ErrorResponse>> handleRuntimeException(RuntimeException ex) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    String message = ex.getMessage();

    if (message == null) {
      message = "An unexpected error occurred";
    }

    if (message.toLowerCase().contains("not found")) {
      status = HttpStatus.NOT_FOUND;
    } else if (message.toLowerCase().contains("stock")
        || message.toLowerCase().contains("no items")) {
      status = HttpStatus.BAD_REQUEST;
    } else if (message.toLowerCase().contains("concurrency conflict")
        || message.toLowerCase().contains("conflicto de concurrencia")) {
      status = HttpStatus.CONFLICT;
    } else if (message.toLowerCase().contains("not in pending state")) {
      status = HttpStatus.BAD_REQUEST;
    }

    ErrorResponse errorResponse = new ErrorResponse(status.value(), message);
    return Mono.just(ResponseEntity.status(status).body(errorResponse));
  }

  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<ResponseEntity<ErrorResponse>> handleValidationExceptions(
      WebExchangeBindException ex) {
    String message = ex.getBindingResult().getAllErrors().stream()
        .map(error -> error.getDefaultMessage()).collect(Collectors.joining(", "));

    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
  }

  public record ErrorResponse(int status, String message) {
  }
}