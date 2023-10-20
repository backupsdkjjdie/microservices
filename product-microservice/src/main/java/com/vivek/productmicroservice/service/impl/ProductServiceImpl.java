package com.vivek.productmicroservice.service.impl;

import com.vivek.productmicroservice.entity.Product;
import com.vivek.productmicroservice.exception.ProductServiceCustomException;
import com.vivek.productmicroservice.model.ProductRequest;
import com.vivek.productmicroservice.model.ProductResponse;
import com.vivek.productmicroservice.repository.ProductRepository;
import com.vivek.productmicroservice.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.springframework.beans.BeanUtils.*;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Override
    public long addProduct(ProductRequest productRequest) {
        log.info("adding product");
        Product product = Product.builder()
                .productName(productRequest.getName())
                .price(productRequest.getPrice())
                .quantity(productRequest.getQuantity())
                .build();
        productRepository.save(product);
        log.info("product created");
        return product.getProductId();
    }

    @Override
    public ProductResponse getProductById(long productId) {
        log.info("Getting the product for productId : {}",productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomException("Product with given id not found","PRODUCT_NOT_FOUND"));
        ProductResponse productResponse = new ProductResponse();
        copyProperties(product,productResponse);
        return productResponse;
    }

    @Override
    public void reduceQuantity(long productId, long quantity) {
        log.info("Reduce quantity {} for ID : {}",quantity,productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new ProductServiceCustomException("Product with given id not found","PRODUCT_NOT_FOUND"));
        if(product.getQuantity() < quantity){
            log.error("OUT_OF_STOCK");
            throw new ProductServiceCustomException(
                    "Product does not have sufficient quantity",
                    "OUT_OF_STOCK"
            );
        }
        product.setQuantity(product.getQuantity()-quantity);
        productRepository.save(product);
        log.info("Product quantity updated successfully!!!");
    }
}
