package com.fullstack.apigateway.Filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtHeaderFilter
        extends AbstractGatewayFilterFactory<JwtHeaderFilter.Config> {

    private static final String HEADER_SESSION_ID = "X-Session-Id";
    private static final String HEADER_ACCOUNT_ID = "X-Account-Id";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    public JwtHeaderFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) ->
                exchange.getPrincipal()
                        .flatMap(principal -> {
                            if (!(principal instanceof JwtAuthenticationToken jwtAuth)) {
                                return chain.filter(exchange);
                            }

                            var jwt = jwtAuth.getToken();

                            String sessionId = getClaimAsString(jwt.getClaim("sid"));
                            String accountId = getClaimAsString(jwt.getClaim("accountId"));
                            String userId = getClaimAsString(jwt.getClaim("userId"));
                            String email = getClaimAsString(jwt.getClaim("email"));
                            String role = getClaimAsString(jwt.getClaim("role"));

                            var mutatedRequest = exchange.getRequest()
                                    .mutate()
                                    .headers(headers -> {
                                        // Xóa header do client tự gửi để tránh giả mạo thông tin.
                                        headers.remove(HEADER_SESSION_ID);
                                        headers.remove(HEADER_ACCOUNT_ID);
                                        headers.remove(HEADER_USER_ID);
                                        headers.remove(HEADER_USER_EMAIL);
                                        headers.remove(HEADER_USER_ROLE);

                                        setHeaderIfPresent(
                                                headers,
                                                HEADER_SESSION_ID,
                                                sessionId
                                        );

                                        setHeaderIfPresent(
                                                headers,
                                                HEADER_ACCOUNT_ID,
                                                accountId
                                        );

                                        setHeaderIfPresent(
                                                headers,
                                                HEADER_USER_ID,
                                                userId
                                        );

                                        setHeaderIfPresent(
                                                headers,
                                                HEADER_USER_EMAIL,
                                                email
                                        );

                                        setHeaderIfPresent(
                                                headers,
                                                HEADER_USER_ROLE,
                                                role
                                        );
                                    })
                                    .build();

                            var mutatedExchange = exchange.mutate()
                                    .request(mutatedRequest)
                                    .build();

                            return chain.filter(mutatedExchange);
                        })
                        // Khi request không có Principal thì vẫn phải tiếp tục filter chain.
                        .switchIfEmpty(chain.filter(exchange));
    }

    private static String getClaimAsString(Object claim) {
        return claim != null ? claim.toString() : null;
    }

    private static void setHeaderIfPresent(
            org.springframework.http.HttpHeaders headers,
            String headerName,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            headers.set(headerName, value);
        }
    }

    public static class Config {
    }
}