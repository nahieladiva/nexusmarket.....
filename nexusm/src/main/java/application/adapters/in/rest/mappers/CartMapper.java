package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.CartItemResponse;
import application.adapters.in.rest.responses.CartResponse;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.valueobjects.Money;
import org.springframework.stereotype.Component;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        Money total = cart.total().orElse(null);
        return new CartResponse(
            cart.getId().toString(),
            cart.getBuyerId().toString(),
            cart.getItems().stream().map(this::toItemResponse).toList(),
            total == null ? null : total.getAmount().toPlainString(),
            total == null ? null : total.getCurrency().getCurrencyCode(),
            cart.getUpdatedAt());
    }

    private CartItemResponse toItemResponse(CartItem item) {
        return new CartItemResponse(
            item.getProductId().toString(),
            item.getProductType(),
            item.getQuantity().getValue(),
            item.getUnitPrice().getAmount().toPlainString(),
            item.getUnitPrice().getCurrency().getCurrencyCode(),
            item.subtotal().getAmount().toPlainString());
    }
}
