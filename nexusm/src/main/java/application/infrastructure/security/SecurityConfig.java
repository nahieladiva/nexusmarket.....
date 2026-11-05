package application.infrastructure.security;

import application.adapters.in.rest.security.JwtAuthenticationFilter;
import application.adapters.in.rest.security.SecurityErrorHandler;
import application.domain.ports.in.AccountPort;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AccountPort accountPort) throws Exception {
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
                .requestMatchers(HttpMethod.POST, "/api/auth/register/buyer", "/api/auth/login").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthenticationFilter(accountPort),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
