package com.ecom.controllers;

import static org.junit.jupiter.api.Assertions.*;

import com.ecom.dto.ProductCatalogueRequest;
import com.ecom.dto.ProductResponse;
import com.ecom.dto.StockUpdateRequest;
import com.ecom.entities.Product;
import com.ecom.services.ProductService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;


       //CREATE PRODUCT

    @Test
    void createProduct_success() throws Exception {

        ProductCatalogueRequest request = new ProductCatalogueRequest();
        request.setName("Laptop");
        request.setDescription("Gaming Laptop");
        request.setPrice(BigDecimal.valueOf(50000));
        request.setStockQuantity(10);
        request.setActive(true);

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Laptop")
                .build();

        when(productService.saveProduct(any(ProductCatalogueRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/products/admin/saveProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop"));
    }


      // GET SINGLE PRODUCT


    @Test
    void getSingleProduct_success() throws Exception {

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Phone")
                .build();

        when(productService.getProduct(1L))
                .thenReturn(response);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Phone"));
    }


      // GET ALL PRODUCTS


    @Test
    void getAllProducts_success() throws Exception {

        when(productService.getAllProducts())
                .thenReturn(List.of(new Product(), new Product()));

        mockMvc.perform(get("/products/getAllProducts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }


      // UPDATE PRODUCT (STOCK)


    @Test
    void updateProduct_success() throws Exception {

        StockUpdateRequest request = new StockUpdateRequest();
        request.setProductId(1L);
        request.setQuantity(3);

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .stockQuantity(7)
                .build();

        when(productService.updateProduct(1L, 3))
                .thenReturn(response);

        mockMvc.perform(put("/products/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(7));
    }


      // DELETE PRODUCT

    @Test
    void deleteProduct_success() throws Exception {

        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/products/admin/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }
}
