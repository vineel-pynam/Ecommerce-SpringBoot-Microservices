package com.vineelpynam.OrderService.exception;

import lombok.Data;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Data
public class CustomException extends RuntimeException{

    private String errorCode;
    private int status;

    public CustomException(String message, String errorCode, int status){
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

}
