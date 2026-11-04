package application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.events.ProductAddedEvent;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductCode;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductApplicationServiceTest {

    private TestFixture f;
    private Seller seller;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        seller = f.approvedSeller("vende@tienda.com");
    }

    @Test
    void approvedSellerPublishesAndEventIsPublished() {
        Product product = f.product(seller, "TEC-0001", "150000");
        assertTrue(product.isActive());
        assertEquals(1, f.events.count(ProductAddedEvent.class));
    }

    @Test
    void unapprovedSellerCannotPublish() {
        Seller pending = (Seller) f.auth.registerSeller("Pepe", Email.of("pepe@tienda.com"),
            PhoneNumber.of("+573001112233"), "Tienda2026", "Pepe Store");
        assertThrows(IllegalStateException.class, () -> f.product(pending, "TEC-0002", "1000"));
    }

    @Test
    void buyerCannotPublish() {
        var buyer = f.buyer("compra@correo.com");
        assertThrows(BusinessRuleException.class, () -> f.productService.createProduct(buyer.getId(),
            ProductCode.of("TEC-0003"), "X", "Y", Money.of("1000", "COP")));
    }

    @Test
    void productCodeMustBeUnique() {
        f.product(seller, "TEC-0001", "150000");
        assertThrows(DuplicateResourceException.class, () -> f.product(seller, "TEC-0001", "9000"));
    }

    @Test
    void priceMustBeGreaterThanZero() {
        assertThrows(IllegalArgumentException.class, () -> f.product(seller, "TEC-0004", "0"));
    }

    @Test
    void onlyOwnerChangesPrice() {
        Product product = f.product(seller, "TEC-0001", "150000");
        Seller other = f.approvedSeller("otro@tienda.com");
        assertThrows(ForbiddenActionException.class, () -> f.productService.changePrice(
            other.getId(), product.getId(), Money.of("1", "COP")));
        Product updated = f.productService.changePrice(seller.getId(), product.getId(),
            Money.of("120000", "COP"));
        assertEquals(Money.of("120000", "COP"), updated.getPrice());
    }

    @Test
    void inactiveProductsAreHiddenFromSearch() {
        Product product = f.product(seller, "TEC-0001", "150000");
        f.product(seller, "TEC-0002", "90000");
        f.productService.changeAvailability(seller.getId(), product.getId(), false);
        assertEquals(1, f.productService.searchProducts(null).size());
        assertEquals(2, f.productService.findBySeller(seller.getId()).size());
    }
}
