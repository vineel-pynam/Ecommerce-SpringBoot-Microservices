package com.vineelpynam.ProductService.service;

import com.vineelpynam.ProductService.entity.Product;
import com.vineelpynam.ProductService.exception.ProductServiceCustomException;
import com.vineelpynam.ProductService.model.ProductRequest;
import com.vineelpynam.ProductService.model.ProductResponse;
import com.vineelpynam.ProductService.repository.ProductRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.springframework.beans.BeanUtils.*;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public long addProduct(ProductRequest productRequest) {
        log.info("Adding Product...");

        Product product
                = Product.builder()
                .productName(productRequest.getName())
                .price(productRequest.getPrice())
                .quantity(productRequest.getQuantity())
                .build();

        product = productRepository.save(product);
        log.info("Product Added");

        return product.getProductId();
    }

    @Override
    public ProductResponse getProductById(long productId) {
        log.info("Get the product for product_id: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow( () -> new ProductServiceCustomException("Product Not Found..!", "PRODUCT_NOT_FOUND"));

        ProductResponse productResponse = new ProductResponse();
        copyProperties(product, productResponse);

        return productResponse;
    }

    @Override
    public void reduceQuantity(long productId, long quantity) {
        log.info("Reduce Quantity {} for id: {}", quantity, productId);

        Product product = productRepository.findById(productId)
                .orElseThrow( ()-> new ProductServiceCustomException(
                        "Product Not Found",
                        "PRODUCT_NOT_FOUND"
                ));

        if( product.getQuantity() < quantity ){
            throw new ProductServiceCustomException(
                    "Product doesn't have sufficient quantity",
                    "INSUFFICIENT_QUANTITY"
            );
        }

        product.setQuantity( product.getQuantity() - quantity );
        productRepository.save(product);
        log.info("Product Quantity Updated Successfully...!");
    }
}
