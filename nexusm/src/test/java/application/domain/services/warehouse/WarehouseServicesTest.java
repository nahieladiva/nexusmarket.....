package application.domain.services.warehouse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.enums.WarehouseStatus;
import application.domain.enums.WarehouseType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.WarehouseLocation;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the warehouse use cases.
 */
class WarehouseServicesTest {

    private static final WarehouseLocation LOCATION = WarehouseLocation.of("A", "1", "2");

    private TestFixture f;
    private User admin;
    private Seller seller;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        admin = f.admin();
        seller = f.approvedSeller("seller@mail.com");
    }

    private Warehouse marketplace(String name) {
        return f.createMarketplaceWarehouse.execute(admin, name, TestFixture.address(), LOCATION);
    }

    private Warehouse own(String name) {
        return f.createSellerWarehouse.execute(seller, name, TestFixture.address(), LOCATION);
    }

    @Test
    void administratorCreatesMarketplaceWarehouses() {
        assertEquals(WarehouseType.MARKETPLACE, marketplace("Main").getType());
        assertThrows(ForbiddenActionException.class,
            () -> f.createMarketplaceWarehouse.execute(seller, "Other", TestFixture.address(), LOCATION));
    }

    @Test
    void sellerCreatesTheirOwnWarehouse() {
        Warehouse warehouse = own("Own");
        assertEquals(WarehouseType.SELLER, warehouse.getType());
        assertEquals(seller.getId(), warehouse.getOwnerSellerId().orElseThrow());
        assertEquals(1, f.consultWarehouse.findBySeller(seller).size());
    }

    @Test
    void namesAreUniqueIgnoringCase() {
        marketplace("Main");
        assertThrows(DuplicateResourceException.class, () -> own("MAIN"));
    }

    @Test
    void ownerManagesSellerWarehouseAndAdministratorManagesMarketplaceWarehouse() {
        Warehouse own = own("Own");
        Warehouse main = marketplace("Main");
        Seller other = f.approvedSeller("other@mail.com");
        assertThrows(ForbiddenActionException.class, () -> f.deactivateWarehouse.execute(other, own.getId()));
        assertThrows(ForbiddenActionException.class, () -> f.deactivateWarehouse.execute(admin, own.getId()));
        assertThrows(ForbiddenActionException.class, () -> f.deactivateWarehouse.execute(seller, main.getId()));
        assertEquals(WarehouseStatus.INACTIVE, f.deactivateWarehouse.execute(seller, own.getId()).getStatus());
        assertEquals(WarehouseStatus.ACTIVE, f.activateWarehouse.execute(seller, own.getId()).getStatus());
    }

    @Test
    void warehouseCannotBeActivatedTwice() {
        Warehouse main = marketplace("Main");
        assertThrows(BusinessRuleException.class, () -> f.activateWarehouse.execute(admin, main.getId()));
    }

    @Test
    void updateKeepsWhatIsNull() {
        Warehouse main = marketplace("Main");
        Warehouse updated = f.updateWarehouse.execute(admin, main.getId(), "Central", null, null);
        assertEquals("Central", updated.getName());
        assertEquals(LOCATION, updated.getLocation());
    }

    @Test
    void queriesAreLimitedByRole() {
        Warehouse own = own("Own");
        Seller other = f.approvedSeller("other@mail.com");
        assertThrows(ForbiddenActionException.class, () -> f.consultWarehouse.execute(other, own.getId()));
        assertEquals(own.getId(), f.consultWarehouse.execute(seller, own.getId()).getId());
        assertEquals(1, f.consultWarehouse.findAll(admin).size());
        assertThrows(ForbiddenActionException.class, () -> f.consultWarehouse.findAll(seller));
    }
}
