package com.fullstack.productservice.query.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fullstack.productservice.query.entity.ProductView;

public interface ProductViewRepository extends JpaRepository<ProductView, String> {

    Page<ProductView> findByDeletedFalse(Pageable pageable);

    Page<ProductView> findByDeletedFalseAndNameContainingIgnoreCaseOrDeletedFalseAndBrandContainingIgnoreCase(
            String name, String brand, Pageable pageable);
}
