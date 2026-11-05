package application.adapters.in.rest.responses;

/**
 * Address DTO for REST responses.
 */
public record AddressResponse(String street, String city, String state,
                              String zipCode, String country) {
}