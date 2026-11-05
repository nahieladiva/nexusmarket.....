package application.domain.services.product;

import application.domain.exceptions.BusinessRuleException;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;

import java.util.Objects;

/**
 * A product can be bought only when it is published and its seller is approved. Suspending a seller
 * hides their products without changing them, so lifting the suspension brings them back.
 */
@DomainService
public class ValidateProductAvailabilityService {

    private final UserRepository userRepository;

    public ValidateProductAvailabilityService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public boolean isAvailable(Product product) {
        return product.isActive()
            && userRepository.findById(product.getSellerId())
                .filter(Seller.class::isInstance)
                .map(Seller.class::cast)
                .filter(Seller::isApproved)
                .isPresent();
    }

    /**
     * @throws BusinessRuleException when the product cannot be bought
     */
    public void execute(Product product) {
        if (!isAvailable(product)) {
            throw new BusinessRuleException("Product " + product.getName() + " is not available");
        }
    }
}
