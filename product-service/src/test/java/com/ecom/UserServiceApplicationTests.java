package com.ecom;


import com.ecom.dto.ProductCatalogueRequest;
import com.ecom.dto.StockUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",

        "spring.cloud.discovery.enabled=false",
        "spring.cloud.config.enabled=false",

        "message=Test Message"
})
class UserServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 1️ Context Load Test (Smoke Test)
    @Test
    void contextLoads() {
        // verifies application context loads successfully
    }

    // 2️ Create Product API Test
    @Test
    void createProduct_success() throws Exception {

        ProductCatalogueRequest request = new ProductCatalogueRequest();
        request.setName("Laptop");
        request.setDescription("Gaming Laptop");
        request.setPrice(BigDecimal.valueOf(60000));
        request.setStockQuantity(10);
        request.setActive(true);

        mockMvc.perform(
                        post("/products/admin/saveProduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    // 3️ Get All Products API Test
    @Test
    void getAllProducts_success() throws Exception {

        mockMvc.perform(get("/products/getAllProducts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // 4️ Get Single Product - Not Found Case
    @Test
    void getSingleProduct_notFound() throws Exception {

        mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // 5️ Update Stock API Test
    @Test
    void updateStock_success() throws Exception {

        StockUpdateRequest request = new StockUpdateRequest();
        request.setProductId(1L);
        request.setQuantity(5);

        mockMvc.perform(
                        put("/products/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    // 6️ Delete Product API Test
    @Test
    void deleteProduct_success() throws Exception {

        mockMvc.perform(delete("/products/admin/1"))
                .andExpect(status().isNoContent());
    }
}
