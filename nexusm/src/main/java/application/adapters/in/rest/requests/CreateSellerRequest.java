package application.adapters.in.rest.requests;

public record CreateSellerRequest(String fullName, String email, String phone,
                                  String password, String businessName) {
}
