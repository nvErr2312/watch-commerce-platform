package com.fullstack.orderservice.query.projection;

import com.fullstack.orderservice.query.model.OrderReadModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderReadModelRepository extends JpaRepository<OrderReadModel, Long> {
}
