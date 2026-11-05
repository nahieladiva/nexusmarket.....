package application.adapters.useCases;

import application.domain.models.AuthSession;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.in.PublicAccessPort;
import application.domain.services.authorization.LoginService;
import application.domain.services.product.ConsultProductService;
import application.domain.services.user.RegisterBuyerService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements {@link PublicAccessPort} by delegating to the domain services.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PublicAccessUseCase implements PublicAccessPort {

    private final RegisterBuyerService registerBuyer;
    private final LoginService login;
    private final ConsultProductService consultProduct;

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
    @Transactional(readOnly = true)
    public Product findProduct(ProductId productId) {
        return consultProduct.findPublished(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        return consultProduct.searchPublished(keyword);
    }
}
