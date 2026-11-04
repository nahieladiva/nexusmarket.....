package application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.StockMovementType;
import application.domain.events.LowStockEvent;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.InsufficientStockException;
import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.StockMovement;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Quantity;
import application.support.TestFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InventoryApplicationServiceTest {

    private TestFixture f;
    private Seller seller;
    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        seller = f.approvedSeller("vende@tienda.com");
        product = f.product(seller, "TEC-0001", "150000");
        warehouse = f.warehouse("Bodega Norte");
    }

    @Test
    void initialStockIsRecordedInHistory() {
        f.stock(seller, product, warehouse, 20, 5);
        List<StockMovement> history = f.inventoryService.findMovements(product.getId(), warehouse.getId());
        assertEquals(1, history.size());
        assertEquals(StockMovementType.INITIAL, history.get(0).type());
        assertEquals(20, history.get(0).delta());
    }

    @Test
    void oneInventoryPerProductAndWarehouse() {
        f.stock(seller, product, warehouse, 20, 5);
        assertThrows(DuplicateResourceException.class, () -> f.stock(seller, product, warehouse, 3, 1));
    }

    @Test
    void cannotStockInInactiveWarehouse() {
        f.warehouseService.changeStatus(warehouse.getId(), false);
        assertThrows(BusinessRuleException.class, () -> f.stock(seller, product, warehouse, 20, 5));
    }

    @Test
    void negativeAdjustmentWorksAndRequiresReason() {
        f.stock(seller, product, warehouse, 20, 5);
        assertThrows(BusinessRuleException.class, () -> f.inventoryService.adjustStock(
            seller.getId(), product.getId(), warehouse.getId(), -3, " "));
        Inventory adjusted = f.inventoryService.adjustStock(seller.getId(), product.getId(),
            warehouse.getId(), -3, "Unidades dañadas");
        assertEquals(17, adjusted.getOnHand().getValue());
    }

    @Test
    void stockNeverGoesNegative() {
        f.stock(seller, product, warehouse, 2, 1);
        assertThrows(InsufficientStockException.class, () -> f.inventoryService.adjustStock(
            seller.getId(), product.getId(), warehouse.getId(), -5, "Conteo físico"));
    }

    @Test
    void lowStockNotifiesSeller() {
        f.stock(seller, product, warehouse, 6, 5);
        f.inventoryService.adjustStock(seller.getId(), product.getId(), warehouse.getId(), -2, "Merma");
        assertEquals(1, f.events.count(LowStockEvent.class));
        assertTrue(f.notifier.sent.stream().anyMatch(n -> n.recipient().equals(seller.getId())
            && n.subject().equals("Stock bajo")));
        assertEquals(1, f.inventoryService.findBelowReorderPoint().size());
    }

    @Test
    void otherSellerCannotTouchStockButAdminCan() {
        f.stock(seller, product, warehouse, 6, 1);
        Seller other = f.approvedSeller("otro@tienda.com");
        assertThrows(ForbiddenActionException.class, () -> f.inventoryService.restock(
            other.getId(), product.getId(), warehouse.getId(), Quantity.of(5), "x"));
        var admin = f.admin();
        Inventory restocked = f.inventoryService.restock(admin.getId(), product.getId(),
            warehouse.getId(), Quantity.of(5), null);
        assertEquals(11, restocked.getOnHand().getValue());
    }
}
