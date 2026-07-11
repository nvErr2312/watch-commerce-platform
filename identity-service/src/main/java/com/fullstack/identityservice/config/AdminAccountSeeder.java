package com.fullstack.identityservice.config;

import com.fullstack.identityservice.model.Account;
import com.fullstack.identityservice.model.AccountStatus;
import com.fullstack.identityservice.model.Role;
import com.fullstack.identityservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAccountSeeder implements ApplicationRunner {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.password}")
    private String password;

    @Value("${app.admin.user-id}")
    private Long userId;

    @Override
    public void run(ApplicationArguments args) {
        String normalizedEmail = email.trim().toLowerCase();
        Account account = accountRepository.findByEmail(normalizedEmail).orElseGet(Account::new);
        account.setEmail(normalizedEmail);
        account.setPassword(passwordEncoder.encode(password));
        account.setUserId(userId);
        account.setRole(Role.ADMIN);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }
}
