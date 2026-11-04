package application.adapters.in.rest.requests;

/** Campos opcionales: los que lleguen nulos no se cambian. */
public record UpdateProfileRequest(String fullName, String phone) {
}
