package com.fullstack.userservice.query.projection;

import com.fullstack.userservice.query.model.UserReadModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReadModelRepository extends JpaRepository<UserReadModel, Long> {
    Optional<UserReadModel> findByEmail(String email);
    Optional<UserReadModel> findByUsername(String username);
}
