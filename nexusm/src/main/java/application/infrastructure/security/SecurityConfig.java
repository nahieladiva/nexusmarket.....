package application.infrastructure.security;

import application.adapters.in.rest.security.JwtAuthenticationFilter;
import application.adapters.in.rest.security.SecurityErrorHandler;
import application.domain.ports.in.AuthenticationPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security with role-based access rules.
 * Ownership rules (for example, "only the product owner") are validated in the use cases.
 * The routes are reorganized by role in phase 4.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMINISTRATOR";
    private static final String SELLER = "SELLER";
    private static final String BUYER = "BUYER";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationPort authenticationPort) throws Exception {
        SecurityErrorHandler errorHandler = new SecurityErrorHandler();
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(errorHandler)
                .accessDeniedHandler(errorHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                // Authentication
                .requestMatchers(HttpMethod.POST, "/api/auth/register/**", "/api/auth/login").permitAll()
                .requestMatchers("/api/auth/**").authenticated()
                // Users
                .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()
                .requestMatchers("/api/users", "/api/users/**").hasRole(ADMIN)
                // Products
                .requestMatchers(HttpMethod.GET, "/api/products/mine").hasRole(SELLER)
                .requestMatchers(HttpMethod.GET, "/api/products/all").hasRole(ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
                .requestMatchers("/api/products", "/api/products/**").hasRole(SELLER)
                // Warehouses
                .requestMatchers(HttpMethod.GET, "/api/warehouses", "/api/warehouses/**").authenticated()
                .requestMatchers("/api/warehouses", "/api/warehouses/**").hasRole(ADMIN)
                // Inventory
                .requestMatchers("/api/inventory", "/api/inventory/**").hasAnyRole(SELLER, ADMIN)
                // Orders
                .requestMatchers(HttpMethod.POST, "/api/orders").hasRole(BUYER)
                .requestMatchers(HttpMethod.GET, "/api/orders/mine").hasRole(BUYER)
                .requestMatchers(HttpMethod.GET, "/api/orders/seller").hasRole(SELLER)
                .requestMatchers(HttpMethod.GET, "/api/orders").hasRole(ADMIN)
                .requestMatchers(HttpMethod.PATCH, "/api/orders/*/confirm", "/api/orders/*/ship",
                    "/api/orders/*/deliver").hasRole(ADMIN)
                .requestMatchers(HttpMethod.PATCH, "/api/orders/*/cancel").hasAnyRole(BUYER, ADMIN)
                .requestMatchers("/api/orders/**").authenticated()
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthenticationFilter(authenticationPort),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
