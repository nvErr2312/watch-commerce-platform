package com.fullstack.orderservice.query.projection;

import com.fullstack.orderservice.query.model.OrderReadModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderReadModelRepository extends JpaRepository<OrderReadModel, Long> {
    List<OrderReadModel> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
