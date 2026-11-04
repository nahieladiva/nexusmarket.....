package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.PromotionMapper;
import application.adapters.in.rest.requests.CreatePromotionRequest;
import application.adapters.in.rest.responses.PriceQuoteResponse;
import application.adapters.in.rest.responses.PromotionResponse;
import application.adapters.in.rest.security.AuthenticatedUser;
import application.domain.ports.in.PromotionManagementPort;
import application.domain.valueobjects.DateRange;
import application.domain.valueobjects.Percentage;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.PromotionId;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST del puerto de entrada {@link PromotionManagementPort}.
 */
@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionManagementPort promotionManagementPort;
    private final PromotionMapper promotionMapper;

    public PromotionController(PromotionManagementPort promotionManagementPort,
                               PromotionMapper promotionMapper) {
        this.promotionManagementPort = promotionManagementPort;
        this.promotionMapper = promotionMapper;
    }

    @PostMapping
    public ResponseEntity<PromotionResponse> create(@AuthenticationPrincipal AuthenticatedUser me,
                                                    @RequestBody CreatePromotionRequest request) {
        if (request.discountPercent() == null) {
            throw new IllegalArgumentException("discountPercent es obligatorio");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionMapper.toResponse(
            promotionManagementPort.createPromotion(me.userId(), ProductId.of(request.productId()),
                Percentage.of(request.discountPercent()),
                DateRange.of(request.startDate(), request.endDate()))));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<PromotionResponse> end(@AuthenticationPrincipal AuthenticatedUser me,
                                                 @PathVariable String id) {
        return ResponseEntity.ok(promotionMapper.toResponse(
            promotionManagementPort.endPromotion(me.userId(), PromotionId.of(id))));
    }

    @GetMapping("/active")
    public ResponseEntity<List<PromotionResponse>> active() {
        return ResponseEntity.ok(promotionManagementPort.findActivePromotions().stream()
            .map(promotionMapper::toResponse).toList());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PromotionResponse>> byProduct(@PathVariable String productId) {
        return ResponseEntity.ok(promotionManagementPort.findByProduct(ProductId.of(productId)).stream()
            .map(promotionMapper::toResponse).toList());
    }

    @GetMapping("/price/{productId}")
    public ResponseEntity<PriceQuoteResponse> finalPrice(@PathVariable String productId) {
        return ResponseEntity.ok(promotionMapper.toResponse(
            promotionManagementPort.calculateFinalPrice(ProductId.of(productId))));
    }
}
