package application.domain.ports.in;

import application.domain.models.AuthSession;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductId;

import java.util.List;

/**
 * Input port for anonymous visitors: the public catalog, the registration of buyers and login.
 */
public interface PublicAccessPort {

    User registerBuyer(String fullName, Email email, PhoneNumber phone, String rawPassword,
                       Address defaultShippingAddress);

    AuthSession login(Email email, String rawPassword);

    Product findProduct(ProductId productId);

    List<Product> searchProducts(String keyword);
}
