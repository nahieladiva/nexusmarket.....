package application.adapters.in.rest.responses;

import application.domain.enums.SellerStatus;
import application.domain.enums.UserRole;
import java.time.LocalDateTime;
import java.util.List;

/**
 * User DTO for REST responses. It never carries the password hash.
 */
public record UserResponse(String id, String fullName, String email, String phone, UserRole role,
                           String businessName, SellerStatus sellerStatus,
                           AddressResponse defaultShippingAddress,
                           List<AddressResponse> addresses, LocalDateTime createdAt) {
}
