package application.adapters.in.rest.responses;

import application.domain.enums.ProductType;

public record CartItemResponse(String productId, ProductType productType, int quantity,
                               String unitPriceAmount, String unitPriceCurrency,
                               String subtotalAmount) {
}
