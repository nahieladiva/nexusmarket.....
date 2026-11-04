package application.adapters.in.rest.requests;

public record CreateBuyerRequest(String fullName, String email, String phone,
                                 String password, AddressRequest defaultShippingAddress) {
}
