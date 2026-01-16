package com.ecom.repositories;

import com.ecom.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>
{
    public Product deleteProductById(Long id);
}
