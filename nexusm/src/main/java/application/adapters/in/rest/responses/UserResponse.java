package application.adapters.in.rest.responses;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de usuario para respuestas REST.
 */
public record UserResponse(String id, String fullName, String email, String phone,
                           String role, String businessName,
                           AddressResponse defaultShippingAddress,
                           List<AddressResponse> addresses, LocalDateTime createdAt) {
}