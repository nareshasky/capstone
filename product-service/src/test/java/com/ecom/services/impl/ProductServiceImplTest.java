package com.ecom.services.impl;

import com.ecom.dto.ProductCatalogueRequest;
import com.ecom.dto.ProductResponse;
import com.ecom.entities.Product;
import com.ecom.exceptions.InsufficientStockException;
import com.ecom.exceptions.ProductCreationException;
import com.ecom.exceptions.ResourceNotFoundException;
import com.ecom.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PasswordEncoder passwordEncoder;


     //  saveProduct()
     @Test
    void saveProduct_success() {

        ProductCatalogueRequest request = new ProductCatalogueRequest();
        request.setName("Laptop");
        request.setDescription("Gaming Laptop");
        request.setPrice(BigDecimal.valueOf(50000));
        request.setStockQuantity(10);
        request.setActive(true);

        Product savedProduct = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("Gaming Laptop")
                .price(BigDecimal.valueOf(50000))
                .stockQuantity(10)
                .active(true)
                .build();

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        ProductResponse response =
                productService.saveProduct(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Laptop", response.getName());

        verify(productRepository, times(1))
                .save(any(Product.class));
    }

    @Test
    void saveProduct_exception() {

        ProductCatalogueRequest request = new ProductCatalogueRequest();
        request.setName("Laptop");

        when(productRepository.save(any(Product.class)))
                .thenThrow(RuntimeException.class);

        assertThrows(ProductCreationException.class,
                () -> productService.saveProduct(request));
    }

    /* -------------------------
       getAllProducts()
       ------------------------- */

    @Test
    void getAllProducts_success() {

        when(productRepository.findAll())
                .thenReturn(List.of(new Product(), new Product()));

        List<Product> products =
                productService.getAllProducts();

        assertEquals(2, products.size());
        verify(productRepository, times(1)).findAll();
    }

    /* -------------------------
       getProduct()
       ------------------------- */

    @Test
    void getProduct_success() {

        Product product = Product.builder()
                .id(1L)
                .name("Phone")
                .stockQuantity(5)
                .build();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductResponse response =
                productService.getProduct(1L);

        assertEquals(1L, response.getId());
        assertEquals("Phone", response.getName());
    }

    @Test
    void getProduct_notFound() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProduct(99L));
    }

    /* -------------------------
       updateProduct()
       ------------------------- */

    @Test
    void updateProduct_success() {

        Product product = Product.builder()
                .id(1L)
                .name("TV")
                .stockQuantity(10)
                .build();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response =
                productService.updateProduct(1L, 3);

        assertEquals(7, response.getStockQuantity());
    }

    @Test
    void updateProduct_insufficientStock() {

        Product product = Product.builder()
                .id(1L)
                .name("TV")
                .stockQuantity(2)
                .build();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class,
                () -> productService.updateProduct(1L, 5));
    }

    @Test
    void updateProduct_notFound() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(1L, 2));
    }

    /* -------------------------
       deleteProduct()
       ------------------------- */

    @Test
    void deleteProduct_success() {

        Product product = Product.builder()
                .id(1L)
                .name("Monitor")
                .build();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.deleteProductById(1L))
                .thenReturn(product);

        ProductResponse response =
                productService.deleteProduct(1L);

        assertEquals(1L, response.getId());
        verify(productRepository, times(1))
                .deleteProductById(1L);
    }

    @Test
    void deleteProduct_notFound() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(1L));
    }

    @Test
    void mapping_shouldMapAllFieldsCorrectly() {

        ProductCatalogueRequest request = new ProductCatalogueRequest();
        request.setName("Tablet");
        request.setDescription("Android Tablet");
        request.setPrice(BigDecimal.valueOf(15000));
        request.setStockQuantity(8);
        request.setActive(true);

        Product product = productService.mapToProductEntity(request);

        assertEquals("Tablet", product.getName());
        assertEquals(8, product.getStockQuantity());
        assertTrue(product.getActive());
    }

}
