package application.adapters.in.rest.responses;

import java.time.LocalDateTime;
import java.util.List;

/** {@code totalAmount} and {@code totalCurrency} are null while the cart is empty. */
public record CartResponse(String id, String buyerId, List<CartItemResponse> items,
                           String totalAmount, String totalCurrency, LocalDateTime updatedAt) {
}
