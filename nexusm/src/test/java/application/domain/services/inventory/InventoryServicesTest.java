package application.domain.services.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.enums.StockMovementType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.InsufficientStockException;
import application.domain.models.Inventory;
import application.domain.models.LogisticsOperator;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the inventory use cases.
 */
class InventoryServicesTest {

    private static final WarehouseLocation LOCATION = WarehouseLocation.of("A", "1", "2");

    private TestFixture f;
    private Seller seller;
    private LogisticsOperator operator;
    private Warehouse marketplace;
    private Warehouse own;
    private Product product;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        seller = f.approvedSeller("seller@mail.com");
        operator = f.logisticsOperator("operator@mail.com");
        marketplace = f.warehouse("Main");
        own = f.warehouses.save(Warehouse.createSellerWarehouse(WarehouseId.random(), seller.getId(),
            "Own", TestFixture.address(), LOCATION));
        product = f.product(seller, "TEC-0001", "1000");
    }

    private Inventory create(Warehouse warehouse, int units) {
        return f.createInventory.execute(operator, product.getId(), warehouse.getId(), Quantity.of(units),
            Quantity.of(2), LOCATION);
    }

    @Test
    void initialStockIsRecorded() {
        create(marketplace, 10);
        assertEquals(StockMovementType.INITIAL, f.consultInventory
            .findMovements(operator, product.getId(), marketplace.getId()).get(0).type());
    }

    @Test
    void oneInventoryPerProductAndWarehouse() {
        create(marketplace, 10);
        assertThrows(DuplicateResourceException.class, () -> create(marketplace, 1));
    }

    @Test
    void digitalProductHasNoInventory() {
        Product digital = f.digitalProduct(seller, "DIG-0001", "500");
        assertThrows(BusinessRuleException.class, () -> f.createInventory.execute(operator, digital.getId(),
            marketplace.getId(), Quantity.of(1), Quantity.of(0), LOCATION));
    }

    @Test
    void sellerManagesStockOnlyInTheirWarehouse() {
        assertThrows(ForbiddenActionException.class, () -> f.createInventory.execute(seller, product.getId(),
            marketplace.getId(), Quantity.of(1), Quantity.of(0), LOCATION));
        Inventory inventory = f.createInventory.execute(seller, product.getId(), own.getId(),
            Quantity.of(5), Quantity.of(0), LOCATION);
        assertEquals(5, inventory.getOnHand().getValue());
        assertEquals(1, f.consultInventory.findBySeller(seller).size());
        Seller other = f.approvedSeller("other@mail.com");
        assertThrows(ForbiddenActionException.class,
            () -> f.consultInventory.execute(other, product.getId(), own.getId()));
    }

    @Test
    void restockAndAdjustChangeTheStock() {
        create(marketplace, 10);
        assertEquals(15, f.restockInventory.execute(operator, product.getId(), marketplace.getId(),
            Quantity.of(5), null).getOnHand().getValue());
        assertEquals(12, f.adjustStock.execute(operator, product.getId(), marketplace.getId(), -3,
            "Damaged").getOnHand().getValue());
    }

    @Test
    void adjustmentNeedsAReasonAndEnoughStock() {
        create(marketplace, 2);
        assertThrows(BusinessRuleException.class,
            () -> f.adjustStock.execute(operator, product.getId(), marketplace.getId(), -1, " "));
        assertThrows(InsufficientStockException.class,
            () -> f.adjustStock.execute(operator, product.getId(), marketplace.getId(), -3, "Lost"));
    }

    @Test
    void lowStockIsReportedAndThresholdCanChange() {
        create(marketplace, 3);
        f.adjustStock.execute(operator, product.getId(), marketplace.getId(), -2, "Lost");
        assertEquals(1, f.consultInventory.findBelowReorderPoint(operator).size());
        f.changeReorderThreshold.execute(operator, product.getId(), marketplace.getId(), Quantity.of(0));
        assertEquals(0, f.consultInventory.findBelowReorderPoint(operator).size());
    }

    @Test
    void administratorReadsButDoesNotManage() {
        create(marketplace, 3);
        assertThrows(ForbiddenActionException.class, () -> f.createInventory.execute(f.admin(), product.getId(),
            own.getId(), Quantity.of(1), Quantity.of(0), LOCATION));
        assertThrows(ForbiddenActionException.class, () -> f.restockInventory.execute(f.admin(), product.getId(),
            marketplace.getId(), Quantity.of(1), null));
        assertThrows(ForbiddenActionException.class, () -> f.adjustStock.execute(f.admin(), product.getId(),
            marketplace.getId(), -1, "Lost"));
        assertThrows(ForbiddenActionException.class, () -> f.changeReorderThreshold.execute(f.admin(),
            product.getId(), marketplace.getId(), Quantity.of(0)));
        assertEquals(1, f.consultInventory.findAll(f.admin()).size());
    }

    @Test
    void supervisorReadsButDoesNotManage() {
        create(marketplace, 3);
        assertThrows(ForbiddenActionException.class, () -> f.adjustStock.execute(f.supervisor("s@mail.com"),
            product.getId(), marketplace.getId(), -1, "Lost"));
        assertEquals(1, f.consultInventory.findAll(f.supervisor("s2@mail.com")).size());
    }
}
