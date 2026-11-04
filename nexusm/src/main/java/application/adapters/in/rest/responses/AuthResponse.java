package application.adapters.in.rest.responses;

import java.time.LocalDateTime;

public record AuthResponse(String token, String tokenType, LocalDateTime expiresAt,
                           UserResponse user) {
}
