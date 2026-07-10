package com.fullstack.productservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Temporary stopgap: commonservice pulls in spring-boot-starter-security for
 * the team's JWT work, which auto-locks every endpoint by default once it's
 * on the classpath. Proper fix is wiring commonservice's JwtAuthenticationFilter
 * here; until that's coordinated, keep endpoints open like before the merge.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
