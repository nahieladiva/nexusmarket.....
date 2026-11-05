package application.domain.services.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.ProductStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.Quantity;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of product blocking and of the visibility of products of suspended sellers.
 */
class ProductModerationTest {

    private TestFixture f;
    private User admin;
    private Seller seller;
    private Buyer buyer;
    private Product product;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        admin = f.admin();
        seller = f.approvedSeller("seller@mail.com");
        buyer = f.buyer("buyer@mail.com");
        product = f.product(seller, "TEC-0001", "1000");
        f.stock(seller, product, f.warehouse("Main"), 10, 1);
    }

    @Test
    void blockedProductLeavesTheCatalogAndCannotBeBought() {
        f.blockProduct.execute(admin, product.getId());
        assertTrue(f.consultProduct.searchPublished(null).isEmpty());
        assertThrows(BusinessRuleException.class,
            () -> f.addItemToCart.execute(buyer, product.getId(), Quantity.of(1)));
    }

    @Test
    void sellerCannotLiftABlock() {
        f.blockProduct.execute(admin, product.getId());
        assertThrows(BusinessRuleException.class, () -> f.publishProduct.execute(seller, product.getId()));
        assertThrows(BusinessRuleException.class, () -> f.suspendProduct.execute(seller, product.getId()));
        assertThrows(BusinessRuleException.class, () -> f.discontinueProduct.execute(seller, product.getId()));
        assertEquals(ProductStatus.BLOCKED, f.products.findById(product.getId()).orElseThrow().getStatus());
    }

    @Test
    void onlyAnAdministratorBlocksAndUnblocks() {
        assertThrows(ForbiddenActionException.class, () -> f.blockProduct.execute(seller, product.getId()));
        f.blockProduct.execute(admin, product.getId());
        assertThrows(ForbiddenActionException.class, () -> f.unblockProduct.execute(seller, product.getId()));
    }

    @Test
    void unblockedProductStaysSuspendedUntilTheSellerPublishesIt() {
        f.blockProduct.execute(admin, product.getId());
        assertEquals(ProductStatus.SUSPENDED, f.unblockProduct.execute(admin, product.getId()).getStatus());
        assertTrue(f.consultProduct.searchPublished(null).isEmpty());
        assertEquals(ProductStatus.PUBLISHED, f.publishProduct.execute(seller, product.getId()).getStatus());
    }

    @Test
    void onlyABlockedProductCanBeUnblocked() {
        assertThrows(BusinessRuleException.class, () -> f.unblockProduct.execute(admin, product.getId()));
        assertEquals(ProductStatus.PUBLISHED, f.products.findById(product.getId()).orElseThrow().getStatus());
    }

    @Test
    void administratorDoesNotUseTheSuspensionOfTheSeller() {
        assertThrows(ForbiddenActionException.class, () -> f.suspendProduct.execute(admin, product.getId()));
    }

    @Test
    void suspendedSellerProductsLeaveTheCatalogAndComeBackWithTheSeller() {
        assertEquals(1, f.consultProduct.searchPublished(null).size());
        f.suspendSeller.execute(admin, seller.getId());
        assertTrue(f.consultProduct.searchPublished(null).isEmpty());
        assertThrows(ResourceNotFoundException.class, () -> f.consultProduct.findPublished(product.getId()));
        assertEquals(ProductStatus.PUBLISHED, f.products.findById(product.getId()).orElseThrow().getStatus());

        f.approveSeller.execute(admin, seller.getId());
        assertEquals(1, f.consultProduct.searchPublished(null).size());
    }

    @Test
    void suspendedSellerProductCannotBeAddedToTheCart() {
        f.suspendSeller.execute(admin, seller.getId());
        assertThrows(BusinessRuleException.class,
            () -> f.addItemToCart.execute(buyer, product.getId(), Quantity.of(1)));
    }

    @Test
    void orderCannotBePlacedIfTheSellerWasSuspendedAfterAddingToTheCart() {
        f.addItemToCart.execute(buyer, product.getId(), Quantity.of(1));
        f.suspendSeller.execute(admin, seller.getId());
        assertThrows(BusinessRuleException.class, () -> f.placeOrder.execute(buyer));
        assertEquals(1, f.consultCart.execute(buyer).getItems().size());

        f.approveSeller.execute(admin, seller.getId());
        Order order = f.placeOrder.execute(buyer);
        assertEquals(1, order.getItems().size());
    }
}
