package com.vineelpynam.OrderService.exception;

import com.vineelpynam.OrderService.external.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException customException){
        return new ResponseEntity<>(
                 ErrorResponse.builder()
                        .errorMessage(customException.getMessage())
                        .errorCode(customException.getErrorCode())
                        .build(), HttpStatus.valueOf(customException.getStatus()));
    }
}
