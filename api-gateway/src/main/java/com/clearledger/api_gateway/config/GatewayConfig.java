package com.clearledger.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter corsFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {

            var response = exchange.getResponse();
            var request  = exchange.getRequest();
            var headers  = response.getHeaders();

            // Add CORS headers to every single response —
            // before anything else in the chain runs
            headers.set("Access-Control-Allow-Origin",      "http://localhost:5173");
            headers.set("Access-Control-Allow-Methods",     "GET,POST,PUT,DELETE,OPTIONS");
            headers.set("Access-Control-Allow-Headers",     "Authorization,Content-Type,X-User-Id");
            headers.set("Access-Control-Expose-Headers",    "Authorization");
            headers.set("Access-Control-Allow-Credentials", "true");
            headers.set("Access-Control-Max-Age",           "3600");

            // For OPTIONS preflight: return 200 immediately,
            // never touch JwtAuthFilter or gateway routing
            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                return response.setComplete();
            }

            return chain.filter(exchange);
        };
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }
}