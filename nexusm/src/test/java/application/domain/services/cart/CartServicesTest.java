package application.domain.services.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the cart use cases.
 */
class CartServicesTest {

    private TestFixture f;
    private Buyer buyer;
    private Seller seller;
    private Product product;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        buyer = f.buyer("buyer@mail.com");
        seller = f.approvedSeller("seller@mail.com");
        product = f.product(seller, "TEC-0001", "1000");
    }

    @Test
    void cartIsCreatedOnceAndKeepsCatalogPrice() {
        f.addItemToCart.execute(buyer, product.getId(), Quantity.of(2));
        Cart cart = f.addItemToCart.execute(buyer, product.getId(), Quantity.of(1));
        assertEquals(1, cart.getItems().size());
        assertEquals(Money.of("3000", "COP"), cart.total().orElseThrow());
        assertEquals(cart.getId(), f.consultCart.execute(buyer).getId());
    }

    @Test
    void onlyBuyersHaveCarts() {
        assertThrows(ForbiddenActionException.class, () -> f.consultCart.execute(seller));
    }

    @Test
    void suspendedProductCannotBeAdded() {
        f.suspendProduct.execute(seller, product.getId());
        assertThrows(BusinessRuleException.class,
            () -> f.addItemToCart.execute(buyer, product.getId(), Quantity.of(1)));
    }

    @Test
    void unknownProductIsNotFound() {
        assertThrows(ResourceNotFoundException.class,
            () -> f.addItemToCart.execute(buyer, ProductId.random(), Quantity.of(1)));
    }

    @Test
    void zeroQuantityRemovesTheItem() {
        f.addItemToCart.execute(buyer, product.getId(), Quantity.of(2));
        assertTrue(f.changeCartItemQuantity.execute(buyer, product.getId(), Quantity.zero()).isEmpty());
    }

    @Test
    void itemCanBeRemovedAndCartCleared() {
        f.addItemToCart.execute(buyer, product.getId(), Quantity.of(2));
        assertTrue(f.removeItemFromCart.execute(buyer, product.getId()).isEmpty());
        f.addItemToCart.execute(buyer, product.getId(), Quantity.of(1));
        assertTrue(f.clearCart.execute(buyer).isEmpty());
    }
}
