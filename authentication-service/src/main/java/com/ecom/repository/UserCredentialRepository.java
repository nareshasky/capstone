package com.ecom.repository;


import com.ecom.entity.CustomerCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialRepository  extends JpaRepository<CustomerCredential,Integer> {
    Optional<CustomerCredential> findByUsername(String username);
    Optional<CustomerCredential> findByUsernameOrEmail(String username,String email);
    CustomerCredential save(CustomerCredential credential);
}
