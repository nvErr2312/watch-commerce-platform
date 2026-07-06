package com.fullstack.inventoryservice.query.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fullstack.inventoryservice.query.entity.InventoryView;

public interface InventoryViewRepository extends JpaRepository<InventoryView, String> {

    Optional<InventoryView> findByProductId(String productId);
}
