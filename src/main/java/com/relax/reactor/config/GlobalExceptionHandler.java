package com.relax.reactor.config;

import com.relax.reactor.exception.GambleNotAvailableException;
import com.relax.reactor.exception.PendingGambleException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex) {

        Map<String, Object> errorResponse = new LinkedHashMap<>();

        errorResponse.put("timestamp", ZonedDateTime.now().format(formatter));
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", String.format("Path '%s' not found", ex.getResourcePath()));

        errorResponse.put("path", ex.getResourcePath());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PendingGambleException.class)
    public ResponseEntity<Map<String, Object>> handlePendingGamble(PendingGambleException ex) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();

        errorResponse.put("timestamp", ZonedDateTime.now().format(formatter));
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Pending Gamble Decision");
        errorResponse.put("message", ex.getMessage());

        if (ex.getStashedCumulativeWinAmount() != null) {
            errorResponse.put("stashedCumulativeWinAmount", ex.getStashedCumulativeWinAmount());
        }

        Map<String, String> links = new HashMap<>();
        links.put("COLLECT", "/slot/gamble?choice=1");
        links.put("GAMBLE", "/slot/gamble?choice=2");
        errorResponse.put("_links", links);

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(GambleNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleGambleNotAvailable(GambleNotAvailableException ex) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();

        errorResponse.put("timestamp", ZonedDateTime.now().format(formatter));
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Gamble Not Available");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("action", "Please make a spin first to trigger a gamble opportunity");

        Map<String, String> links = new HashMap<>();
        links.put("spin", "/slot/spin?stake=0.10");
        errorResponse.put("_links", links);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", ZonedDateTime.now().format(formatter));
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");

        String paramName = ex.getName();

        String message = createHelpfulMessage(paramName, ex.getValue());
        errorResponse.put("message", message);

        Map<String, Object> examples = new HashMap<>();

        switch (paramName) {
            case "spins":
                examples.put("validExamples", Arrays.asList("1000", "50000", "1000000"));
                examples.put("constraints", "Integer between 1 and 3,000,000");
                break;
            case "stake":
                examples.put("validExamples", Arrays.asList("0.10", "1.50", "50.00"));
                examples.put("constraints", "Decimal number, multiple of 0.10, up to 100.00");
                break;
            case "choice":
                examples.put("validExamples", Arrays.asList("1", "2"));
                examples.put("constraints", "Must be 1 (COLLECT) or 2 (GAMBLE)");
                break;
        }

        if (!examples.isEmpty()) {
            errorResponse.put("examples", examples);
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private String createHelpfulMessage(String paramName, Object invalidValue) {
        String baseMessage = String.format(
                "The value '%s' is not valid for parameter '%s'.",
                invalidValue != null ? invalidValue.toString() : "null",
                paramName
        );

        return switch (paramName) {
            case "spins" -> baseMessage + " Please provide a whole number between 1 and 3,000,000.";
            case "stake" ->
                    baseMessage + " Please provide a decimal amount in multiples of $0.10 (e.g., 0.10, 1.50, 50.00).";
            case "choice" -> baseMessage + " Please provide either 1 (to collect) or 2 (to gamble).";
            default -> baseMessage + " Please check the parameter format.";
        };
    }
}
