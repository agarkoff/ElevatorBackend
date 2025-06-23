package com.example.elevatorbackend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ElevatorExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Long> handleException(Exception e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(1L, HttpStatus.OK);
    }
}
