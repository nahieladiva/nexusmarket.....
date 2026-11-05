package application.domain.ports.in;

import application.domain.models.AuthSession;
import application.domain.models.Product;
import application.domain.models.TokenClaims;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

import java.util.List;

/**
 * Input port for what does not depend on a role: the public catalog, the registration of buyers,
 * the session, and the profile of the authenticated user.
 */
public interface PublicAccessPort {

    User registerBuyer(String fullName, Email email, PhoneNumber phone, String rawPassword,
                       Address defaultShippingAddress);

    AuthSession login(Email email, String rawPassword);

    void logout(String token);

    TokenClaims validateToken(String token);

    Product findProduct(ProductId productId);

    List<Product> searchProducts(String keyword);

    User findProfile(UserId userId);

    User updateProfile(UserId userId, String fullName, PhoneNumber phone);

    User addAddress(UserId userId, Address address);

    User removeAddress(UserId userId, Address address);
}
