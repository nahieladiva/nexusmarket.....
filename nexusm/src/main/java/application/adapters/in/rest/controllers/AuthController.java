package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.requests.CreateBuyerRequest;
import application.adapters.in.rest.requests.CreateSellerRequest;
import application.adapters.in.rest.requests.LoginRequest;
import application.adapters.in.rest.responses.AuthResponse;
import application.adapters.in.rest.responses.MessageResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.adapters.in.rest.security.AuthenticatedUser;
import application.domain.models.AuthSession;
import application.domain.ports.in.AuthenticationPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST del puerto de entrada {@link AuthenticationPort}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationPort authenticationPort;
    private final UserMapper userMapper;

    public AuthController(AuthenticationPort authenticationPort, UserMapper userMapper) {
        this.authenticationPort = authenticationPort;
        this.userMapper = userMapper;
    }

    @PostMapping("/register/buyer")
    public ResponseEntity<UserResponse> registerBuyer(@RequestBody CreateBuyerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(
            authenticationPort.registerBuyer(request.fullName(), Email.of(request.email()),
                PhoneNumber.of(request.phone()), request.password(),
                userMapper.toDomain(request.defaultShippingAddress()))));
    }

    @PostMapping("/register/seller")
    public ResponseEntity<UserResponse> registerSeller(@RequestBody CreateSellerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(
            authenticationPort.registerSeller(request.fullName(), Email.of(request.email()),
                PhoneNumber.of(request.phone()), request.password(), request.businessName())));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthSession session = authenticationPort.login(Email.of(request.email()), request.password());
        return ResponseEntity.ok(new AuthResponse(session.token(), "Bearer", session.expiresAt(),
            userMapper.toResponse(session.user())));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal AuthenticatedUser me) {
        authenticationPort.logout(me.token());
        return ResponseEntity.ok(new MessageResponse("Sesión cerrada; el token quedó revocado"));
    }
}
