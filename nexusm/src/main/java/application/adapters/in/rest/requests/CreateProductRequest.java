package application.adapters.in.rest.requests;

/** El vendedor se toma del token, no del cuerpo. */
public record CreateProductRequest(String code, String name, String description,
                                   PriceRequest price) {
}
