package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.PriceRequest;
import application.adapters.in.rest.responses.ProductResponse;
import application.domain.models.Product;
import application.domain.valueobjects.Money;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Money toDomain(PriceRequest price) {
        if (price == null || price.amount() == null || price.currency() == null) {
            throw new IllegalArgumentException("The price (amount and currency) is required");
        }
        return Money.of(price.amount(), price.currency());
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId().toString(),
            product.getCode().getValue(),
            product.getName(),
            product.getDescription(),
            product.getPrice().getAmount().toPlainString(),
            product.getPrice().getCurrency().getCurrencyCode(),
            product.getSellerId().toString(),
            product.isActive(),
            product.getCreatedAt());
    }
}
