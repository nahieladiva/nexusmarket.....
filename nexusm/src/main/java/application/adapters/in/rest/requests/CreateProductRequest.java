package application.adapters.in.rest.requests;

/** The seller comes from the token, not from the body. */
public record CreateProductRequest(String code, String name, String description,
                                   PriceRequest price) {
}
