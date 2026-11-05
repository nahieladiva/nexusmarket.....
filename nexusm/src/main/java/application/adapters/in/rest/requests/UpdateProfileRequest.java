package application.adapters.in.rest.requests;

/** Optional fields: those sent as null are not changed. */
public record UpdateProfileRequest(String fullName, String phone) {
}
