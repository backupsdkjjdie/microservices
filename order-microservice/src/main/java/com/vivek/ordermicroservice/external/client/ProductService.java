package com.vivek.ordermicroservice.external.client;

import com.vivek.ordermicroservice.exception.CustomException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "PRODUCT-SERVICE/product")
@CircuitBreaker(name = "external", fallbackMethod = "fallback")
public interface ProductService {
    @PutMapping("/reduceQuantity/{id}")
    ResponseEntity<?> reduceProductQuantity(@PathVariable("id") long productId,
                                                   @RequestParam long quantity);
    default ResponseEntity<?> fallback(Exception exception){
        throw new CustomException("Product Service is not available!", "UNAVAILABLE",500);
    }
}
