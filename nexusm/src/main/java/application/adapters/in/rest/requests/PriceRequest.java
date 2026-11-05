package application.adapters.in.rest.requests;

/**
 * Price DTO (amount and currency) for REST.
 */
public record PriceRequest(String amount, String currency) {
}