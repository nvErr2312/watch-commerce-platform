package com.fullstack.identityservice.repository;

import com.fullstack.identityservice.model.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
}
