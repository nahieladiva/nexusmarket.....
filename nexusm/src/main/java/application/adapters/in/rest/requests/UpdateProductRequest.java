package application.adapters.in.rest.requests;

/** Campos opcionales: los que lleguen nulos no se cambian. */
public record UpdateProductRequest(String name, String description) {
}
