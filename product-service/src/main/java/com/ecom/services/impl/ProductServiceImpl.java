package com.ecom.services.impl;

import com.ecom.dto.ProductCatalogueRequest;
import com.ecom.entities.Product;
import com.ecom.exceptions.InsufficientStockException;
import com.ecom.exceptions.ProductCreationException;
import com.ecom.exceptions.ResourceNotFoundException;
import com.ecom.dto.ProductResponse;
import com.ecom.repositories.ProductRepository;
import com.ecom.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ProductResponse saveProduct(ProductCatalogueRequest productCatalogueRequest) {

        Product product = mapToProductEntity(productCatalogueRequest);
        Product savedProduct;
        try {
            savedProduct = productRepository.save(product);
        } catch (Exception ex) {
            throw new ProductCreationException("Unable to create product", ex);
        }
        return mapToProductResponseDto(savedProduct);
    }

    //DTO → Entity Mapping
    public Product mapToProductEntity(ProductCatalogueRequest request) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .active(request.getActive())
                .build();
    }
    //Entity → DTO Mapping
    public ProductResponse mapToProductResponseDto(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }


    @Override
    public List<Product> getAllProducts() {
        //implement RATING SERVICE CALL: USING REST TEMPLATE
        return productRepository.findAll();
    }

    //get single user
    @Override
    public ProductResponse getProduct(Long productId) {
        //get product from database with the help  of product repository
        return productRepository.findById(productId)
                .map(this::mapToProductResponseDto)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product with given id is not found on server !! : " + productId));
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, int quantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + productId));

        if (product.getStockQuantity() <=quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName());
        }

        product.setStockQuantity(
                product.getStockQuantity() - quantity);

        Product updatedProduct = productRepository.save(product);

        return mapToProductResponseDto(updatedProduct);
    }

    @Override
    public ProductResponse deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));

        productRepository.delete(product);

        return mapToProductResponseDto(product);
    }
    @Override
    @Transactional
    public ProductResponse increaseStock(Long productId, int quantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + productId));

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        product.setStockQuantity(
                product.getStockQuantity() + quantity
        );

        Product updatedProduct = productRepository.save(product);
        return mapToProductResponseDto(updatedProduct);
    }


}
