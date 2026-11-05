package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.requests.CreateBuyerRequest;
import application.adapters.in.rest.requests.LoginRequest;
import application.adapters.in.rest.responses.AuthResponse;
import application.adapters.in.rest.responses.MessageResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.adapters.in.rest.security.AuthenticatedUser;
import application.domain.models.AuthSession;
import application.domain.ports.in.AccountPort;
import application.domain.ports.in.PublicAccessPort;
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
 * REST adapter for the registration of buyers and for the session.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PublicAccessPort publicAccessPort;
    private final AccountPort accountPort;
    private final UserMapper userMapper;

    public AuthController(PublicAccessPort publicAccessPort, AccountPort accountPort, UserMapper userMapper) {
        this.publicAccessPort = publicAccessPort;
        this.accountPort = accountPort;
        this.userMapper = userMapper;
    }

    @PostMapping("/register/buyer")
    public ResponseEntity<UserResponse> registerBuyer(@RequestBody CreateBuyerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(
            publicAccessPort.registerBuyer(request.fullName(), Email.of(request.email()),
                PhoneNumber.of(request.phone()), request.password(),
                userMapper.toDomain(request.defaultShippingAddress()))));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthSession session = publicAccessPort.login(Email.of(request.email()), request.password());
        return ResponseEntity.ok(new AuthResponse(session.token(), "Bearer", session.expiresAt(),
            userMapper.toResponse(session.user())));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal AuthenticatedUser me) {
        accountPort.logout(me.token());
        return ResponseEntity.ok(new MessageResponse("Session closed; the token was revoked"));
    }
}
