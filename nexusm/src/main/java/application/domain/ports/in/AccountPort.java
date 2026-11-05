package application.domain.ports.in;

import application.domain.models.TokenClaims;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;

/**
 * Input port for what any authenticated user does with their own account, whatever their role:
 * profile, addresses, password and session.
 */
public interface AccountPort {

    User findProfile(UserId userId);

    User updateProfile(UserId userId, String fullName, PhoneNumber phone);

    User addAddress(UserId userId, Address address);

    User removeAddress(UserId userId, Address address);

    User changePassword(UserId userId, String currentPassword, String newPassword);

    TokenClaims validateToken(String token);

    void logout(String token);
}
