package application.adapters.in.rest.requests;

/**
 * Address DTO for REST input and output.
 */
public record AddressRequest(String street, String city, String state,
                             String zipCode, String country) {
}