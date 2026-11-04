package application.domain.services.cart;

import application.domain.enums.UserRole;
import application.domain.models.Cart;
import application.domain.models.User;
import application.domain.ports.out.CartRepository;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.services.DomainService;
import application.domain.valueobjects.CartId;

import java.util.Objects;

/**
 * Returns the cart of a buyer. The cart is created empty the first time it is needed.
 */
@DomainService
public class ConsultCartService {

    private final CartRepository cartRepository;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public ConsultCartService(CartRepository cartRepository, ClockPort clock,
                              IdGeneratorPort idGenerator) {
        this.cartRepository = Objects.requireNonNull(cartRepository);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public Cart execute(User buyer) {
        buyer.requireRole(UserRole.BUYER);
        return cartRepository.findByBuyerId(buyer.getId())
            .orElseGet(() -> cartRepository.save(
                Cart.createFor(CartId.of(idGenerator.newId()), buyer.getId(), clock.now())));
    }
}
