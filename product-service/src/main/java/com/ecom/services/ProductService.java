package com.ecom.services;

import com.ecom.dto.ProductCatalogueRequest;
import com.ecom.entities.Product;
import com.ecom.dto.ProductResponse;

import java.util.List;


public interface ProductService {

    //create
    ProductResponse saveProduct(ProductCatalogueRequest user);

    //get all user
    List<Product> getAllProducts();

    //get single user of given userId
    ProductResponse getProduct(Long userId);

    ProductResponse updateProduct(Long id, int quantity);

    ProductResponse deleteProduct(Long id);

    ProductResponse increaseStock(Long productId, int quantity);

}
