package com.vineelpynam.ProductService.exception;

import lombok.*;

@Data
public class ProductServiceCustomException extends RuntimeException{
    private String errorCode;
    public ProductServiceCustomException(String message, String errorCode){
        super(message);
        this.errorCode = errorCode;
    }
}
