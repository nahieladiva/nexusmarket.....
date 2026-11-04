package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.ProductMapper;
import application.adapters.in.rest.requests.AvailabilityRequest;
import application.adapters.in.rest.requests.CreateProductRequest;
import application.adapters.in.rest.requests.PriceChangeRequest;
import application.adapters.in.rest.requests.UpdateProductRequest;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.security.AuthenticatedUser;
import application.domain.ports.in.ProductManagementPort;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST del puerto de entrada {@link ProductManagementPort}.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductManagementPort productManagementPort;
    private final ProductMapper productMapper;

    public ProductController(ProductManagementPort productManagementPort, ProductMapper productMapper) {
        this.productManagementPort = productManagementPort;
        this.productMapper = productMapper;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> search(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(productManagementPort.searchProducts(keyword).stream()
            .map(productMapper::toResponse).toList());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ProductResponse>> mine(@AuthenticationPrincipal AuthenticatedUser me) {
        return ResponseEntity.ok(productManagementPort.findBySeller(me.userId()).stream()
            .map(productMapper::toResponse).toList());
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> all() {
        return ResponseEntity.ok(productManagementPort.findAllProducts().stream()
            .map(productMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(productMapper.toResponse(
            productManagementPort.findProductById(ProductId.of(id))));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@AuthenticationPrincipal AuthenticatedUser me,
                                                  @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toResponse(
            productManagementPort.createProduct(me.userId(), ProductCode.of(request.code()),
                request.name(), request.description(), productMapper.toDomain(request.price()))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@AuthenticationPrincipal AuthenticatedUser me,
                                                  @PathVariable String id,
                                                  @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productMapper.toResponse(productManagementPort.updateProduct(
            me.userId(), ProductId.of(id), request.name(), request.description())));
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<ProductResponse> changePrice(@AuthenticationPrincipal AuthenticatedUser me,
                                                       @PathVariable String id,
                                                       @RequestBody PriceChangeRequest request) {
        return ResponseEntity.ok(productMapper.toResponse(productManagementPort.changePrice(
            me.userId(), ProductId.of(id), productMapper.toDomain(request.price()))));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<ProductResponse> changeAvailability(@AuthenticationPrincipal AuthenticatedUser me,
                                                              @PathVariable String id,
                                                              @RequestBody AvailabilityRequest request) {
        return ResponseEntity.ok(productMapper.toResponse(productManagementPort.changeAvailability(
            me.userId(), ProductId.of(id), request.active())));
    }
}
