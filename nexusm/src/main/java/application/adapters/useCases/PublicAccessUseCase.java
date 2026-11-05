package application.adapters.useCases;

import application.domain.models.AuthSession;
import application.domain.models.Product;
import application.domain.models.TokenClaims;
import application.domain.models.User;
import application.domain.ports.in.PublicAccessPort;
import application.domain.services.authorization.LoginService;
import application.domain.services.authorization.LogoutService;
import application.domain.services.authorization.ResolveActorService;
import application.domain.services.authorization.ValidateTokenService;
import application.domain.services.product.ConsultProductService;
import application.domain.services.user.AddAddressService;
import application.domain.services.user.RegisterBuyerService;
import application.domain.services.user.RemoveAddressService;
import application.domain.services.user.UpdateProfileService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link PublicAccessPort} by delegating to the domain services.
 */
@Service
@RequiredArgsConstructor
public class PublicAccessUseCase implements PublicAccessPort {

    private final ResolveActorService resolveActor;
    private final RegisterBuyerService registerBuyer;
    private final LoginService login;
    private final LogoutService logout;
    private final ValidateTokenService validateToken;
    private final ConsultProductService consultProduct;
    private final UpdateProfileService updateProfile;
    private final AddAddressService addAddress;
    private final RemoveAddressService removeAddress;

    @Override
    public User registerBuyer(String fullName, Email email, PhoneNumber phone, String rawPassword,
                              Address defaultShippingAddress) {
        return registerBuyer.execute(fullName, email, phone, rawPassword, defaultShippingAddress);
    }

    @Override
    public AuthSession login(Email email, String rawPassword) {
        return login.execute(email, rawPassword);
    }

    @Override
    public void logout(String token) {
        logout.execute(token);
    }

    @Override
    public TokenClaims validateToken(String token) {
        return validateToken.execute(token);
    }

    @Override
    public Product findProduct(ProductId productId) {
        return consultProduct.findPublished(productId);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return consultProduct.searchPublished(keyword);
    }

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
}
