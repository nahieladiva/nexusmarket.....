package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.ProductMapper;
import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.requests.CreateBuyerRequest;
import application.adapters.in.rest.requests.LoginRequest;
import application.adapters.in.rest.responses.AuthResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.domain.models.AuthSession;
import application.domain.ports.in.PublicAccessPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductId;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter of {@link PublicAccessPort}: what anyone can do without a token.
 */
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private static final String TOKEN_TYPE = "Bearer";

    private final PublicAccessPort port;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    @PostMapping("/buyers")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerBuyer(@Valid @RequestBody CreateBuyerRequest request) {
        return userMapper.toResponse(port.registerBuyer(request.fullName(), Email.of(request.email()),
            PhoneNumber.of(request.phone()), request.password(),
            userMapper.toDomain(request.defaultShippingAddress())));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        AuthSession session = port.login(Email.of(request.email()), request.password());
        return new AuthResponse(session.token(), TOKEN_TYPE, session.expiresAt(),
            userMapper.toResponse(session.user()));
    }

    @GetMapping("/products")
    public List<ProductResponse> searchProducts(@RequestParam(required = false) String keyword) {
        return port.searchProducts(keyword).stream().map(productMapper::toResponse).toList();
    }

    @GetMapping("/products/{productId}")
    public ProductResponse findProduct(@PathVariable ProductId productId) {
        return productMapper.toResponse(port.findProduct(productId));
    }
}
