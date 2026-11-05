package application.adapters.useCases;

import application.domain.models.TokenClaims;
import application.domain.models.User;
import application.domain.ports.in.AccountPort;
import application.domain.services.authorization.LogoutService;
import application.domain.services.authorization.ResolveActorService;
import application.domain.services.authorization.ValidateTokenService;
import application.domain.services.user.AddAddressService;
import application.domain.services.user.ChangePasswordService;
import application.domain.services.user.RemoveAddressService;
import application.domain.services.user.UpdateProfileService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link AccountPort} by delegating to the domain services.
 */
@Service
@RequiredArgsConstructor
public class AccountUseCase implements AccountPort {

    private final ResolveActorService resolveActor;
    private final UpdateProfileService updateProfile;
    private final AddAddressService addAddress;
    private final RemoveAddressService removeAddress;
    private final ChangePasswordService changePassword;
    private final ValidateTokenService validateToken;
    private final LogoutService logout;

    @Override
    public User findProfile(UserId userId) {
        return resolveActor.execute(userId);
    }

    @Override
    public User updateProfile(UserId userId, String fullName, PhoneNumber phone) {
        return updateProfile.execute(resolveActor.execute(userId), fullName, phone);
    }

    @Override
    public User addAddress(UserId userId, Address address) {
        return addAddress.execute(resolveActor.execute(userId), address);
    }

    @Override
    public User removeAddress(UserId userId, Address address) {
        return removeAddress.execute(resolveActor.execute(userId), address);
    }

    @Override
    public User changePassword(UserId userId, String currentPassword, String newPassword) {
        return changePassword.execute(resolveActor.execute(userId), currentPassword, newPassword);
    }

    @Override
    public TokenClaims validateToken(String token) {
        return validateToken.execute(token);
    }

    @Override
    public void logout(String token) {
        logout.execute(token);
    }
}
