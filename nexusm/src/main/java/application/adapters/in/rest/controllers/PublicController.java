package application.adapters.in.rest.controllers;

import application.adapters.in.rest.documentation.ApiDocumentation;
import application.adapters.in.rest.mappers.ProductMapper;
import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.requests.CreateBuyerRequest;
import application.adapters.in.rest.requests.LoginRequest;
import application.adapters.in.rest.responses.AuthResponse;
import application.adapters.in.rest.responses.ErrorResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.domain.models.AuthSession;
import application.domain.ports.in.PublicAccessPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Public", description = "Catalog, buyer registration and login, without a token")
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private static final String TOKEN_TYPE = "Bearer";

    private final PublicAccessPort port;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    @Operation(summary = "Register a buyer")
    @ApiResponse(responseCode = "201", description = "The registered buyer")
    @ApiResponse(responseCode = "400", description = ApiDocumentation.BAD_REQUEST,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "The email is already registered",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "The password does not follow the policy",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/buyers")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerBuyer(@Valid @RequestBody CreateBuyerRequest request) {
        return userMapper.toResponse(port.registerBuyer(request.fullName(), Email.of(request.email()),
            PhoneNumber.of(request.phone()), request.password(),
            userMapper.toDomain(request.defaultShippingAddress())));
    }

    @Operation(summary = "Log in and receive a JWT")
    @ApiResponse(responseCode = "200", description = "The token and the user")
    @ApiResponse(responseCode = "400", description = ApiDocumentation.BAD_REQUEST,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Incorrect email or password",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        AuthSession session = port.login(Email.of(request.email()), request.password());
        return new AuthResponse(session.token(), TOKEN_TYPE, session.expiresAt(),
            userMapper.toResponse(session.user()));
    }

    @Operation(summary = "Search the published products")
    @ApiResponse(responseCode = "200", description = "The available products, which can be none")
    @GetMapping("/products")
    public List<ProductResponse> searchProducts(@RequestParam(required = false) String keyword) {
        return port.searchProducts(keyword).stream().map(productMapper::toResponse).toList();
    }

    @Operation(summary = "Find one published product")
    @ApiResponse(responseCode = "200", description = "The product")
    @ApiResponse(responseCode = "400", description = ApiDocumentation.BAD_REQUEST,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "The product does not exist or is not available",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/products/{productId}")
    public ProductResponse findProduct(@PathVariable ProductId productId) {
        return productMapper.toResponse(port.findProduct(productId));
    }
}
