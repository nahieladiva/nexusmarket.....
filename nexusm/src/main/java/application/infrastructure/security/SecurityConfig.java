package application.infrastructure.security;

import application.adapters.in.rest.security.JwtAuthenticationFilter;
import application.adapters.in.rest.security.SecurityErrorHandler;
import application.domain.enums.UserRole;
import application.domain.ports.in.AccountPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security. Each route prefix belongs to one role, and anything else is denied.
 * Ownership rules (for example, "only the product owner") are validated in the domain services.
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
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**")
                .permitAll()
                .requestMatchers("/api/v1/account/**").authenticated()
                .requestMatchers("/api/v1/buyer/**").hasRole(UserRole.BUYER.name())
                .requestMatchers("/api/v1/seller/**").hasRole(UserRole.SELLER.name())
                .requestMatchers("/api/v1/logistics/**").hasRole(UserRole.LOGISTICS_OPERATOR.name())
                .requestMatchers("/api/v1/admin/**").hasRole(UserRole.ADMINISTRATOR.name())
                .requestMatchers("/api/v1/supervisor/**").hasRole(UserRole.SUPERVISOR.name())
                .anyRequest().denyAll())
            .addFilterBefore(new JwtAuthenticationFilter(accountPort),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
