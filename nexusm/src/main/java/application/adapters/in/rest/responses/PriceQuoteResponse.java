package application.adapters.in.rest.responses;

public record PriceQuoteResponse(String productId, String basePrice, String discountPercent,
                                 String finalPrice, String currency) {
}
