package application.domain.services.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.SellerStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.UserId;
import application.support.TestFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the product use cases.
 */
class ProductServicesTest {

    private TestFixture f;
    private Seller seller;
    private Product draft;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        seller = f.approvedSeller("seller@mail.com");
        draft = register(seller, "TEC-0001");
    }

    private Product register(Seller owner, String code) {
        return f.registerProduct.execute(owner, ProductCode.of(code), ProductType.PHYSICAL, "Laptop",
            "A laptop", Money.of("1000", "COP"));
    }

    @Test
    void registeredProductIsADraftOutOfTheCatalog() {
        assertEquals(ProductStatus.DRAFT, draft.getStatus());
        assertTrue(f.consultProduct.searchPublished(null).isEmpty());
        assertThrows(ResourceNotFoundException.class, () -> f.consultProduct.findPublished(draft.getId()));
    }

    @Test
    void publishedProductAppearsInTheCatalog() {
        f.publishProduct.execute(seller, draft.getId());
        assertEquals(draft.getId(), f.consultProduct.findPublished(draft.getId()).getId());
        assertEquals(1, f.consultProduct.searchPublished("lap").size());
    }

    @Test
    void codeMustBeUnique() {
        assertThrows(DuplicateResourceException.class, () -> register(seller, "TEC-0001"));
    }

    @Test
    void unapprovedSellerCannotPublish() {
        Seller pending = new Seller(UserId.random(), "Pending", Email.of("pending@mail.com"),
            PhoneNumber.of("+573001112233"), "hashed:Clave123", "Pending Store", List.of(),
            SellerStatus.PENDING_APPROVAL, TestFixture.NOW);
        f.users.save(pending);
        Product product = register(pending, "TEC-0002");
        assertThrows(BusinessRuleException.class, () -> f.publishProduct.execute(pending, product.getId()));
    }

    @Test
    void onlyTheOwnerChangesAProduct() {
        Seller other = f.approvedSeller("other@mail.com");
        assertThrows(ForbiddenActionException.class,
            () -> f.updateProduct.execute(other, draft.getId(), "Other", null));
        assertThrows(ForbiddenActionException.class,
            () -> f.changeProductPrice.execute(other, draft.getId(), Money.of("5", "COP")));
        assertThrows(ForbiddenActionException.class, () -> f.publishProduct.execute(other, draft.getId()));
        assertEquals("Desk", f.updateProduct.execute(seller, draft.getId(), "Desk", null).getName());
        assertEquals(Money.of("5", "COP"),
            f.changeProductPrice.execute(seller, draft.getId(), Money.of("5", "COP")).getPrice());
    }

    @Test
    void sellerSuspendsAndAdministratorDiscontinues() {
        f.publishProduct.execute(seller, draft.getId());
        assertEquals(ProductStatus.SUSPENDED,
            f.suspendProduct.execute(seller, draft.getId()).getStatus());
        assertEquals(ProductStatus.DISCONTINUED,
            f.discontinueProduct.execute(f.admin(), draft.getId()).getStatus());
    }

    @Test
    void queriesAreLimitedByRole() {
        assertEquals(1, f.consultProduct.findBySeller(seller).size());
        assertEquals(1, f.consultProduct.findAll(f.admin()).size());
        assertThrows(ForbiddenActionException.class, () -> f.consultProduct.findAll(seller));
    }
}
