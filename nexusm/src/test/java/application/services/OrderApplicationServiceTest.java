package application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.enums.OrderStatus;
import application.domain.enums.StockMovementType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.OrderStateTransitionException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.OrderLine;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.Quantity;
import application.support.TestFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderApplicationServiceTest {

    private TestFixture f;
    private Seller seller;
    private Buyer buyer;
    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        seller = f.approvedSeller("vende@tienda.com");
        buyer = f.buyer("compra@correo.com");
        product = f.product(seller, "TEC-0001", "100000");
        warehouse = f.warehouse("Bodega Norte");
        f.stock(seller, product, warehouse, 50, 5);
    }

    private Order place(Product p, int qty) {
        return f.orderService.placeOrder(buyer.getId(), List.of(new OrderLine(p.getId(), Quantity.of(qty))));
    }

    @Test
    void priceComesFromCatalogNotFromClient() {
        Order order = place(product, 2);
        assertEquals(Money.of("200000", "COP"), order.getTotal());
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
    }

    @Test
    void inactiveProductCannotBeOrdered() {
        f.productService.changeAvailability(seller.getId(), product.getId(), false);
        assertThrows(BusinessRuleException.class, () -> place(product, 1));
    }

    @Test
    void insufficientStockIsRejectedAtCreation() {
        Product scarce = f.product(seller, "TEC-0002", "5000");
        f.stock(seller, scarce, warehouse, 1, 0);
        assertThrows(InsufficientStockException.class, () -> place(scarce, 3));
    }

    @Test
    void stockIsReservedOnlyWhenConfirmed() {
        Order order = place(product, 4);
        assertEquals(50, f.inventoryService.findInventory(product.getId(), warehouse.getId())
            .getOnHand().getValue());
        Order confirmed = f.orderService.confirmOrder(order.getId());
        assertEquals(OrderStatus.PAID, confirmed.getStatus());
        assertEquals(warehouse.getId(), confirmed.getItems().get(0).getWarehouseId());
        assertEquals(46, f.inventoryService.findInventory(product.getId(), warehouse.getId())
            .getOnHand().getValue());
        assertEquals(StockMovementType.RESERVATION, f.inventoryService
            .findMovements(product.getId(), warehouse.getId()).get(0).type());
    }

    @Test
    void cancellingConfirmedOrderReleasesStock() {
        Order order = place(product, 4);
        f.orderService.confirmOrder(order.getId());
        f.orderService.cancelOrder(buyer.getId(), order.getId());
        assertEquals(50, f.inventoryService.findInventory(product.getId(), warehouse.getId())
            .getOnHand().getValue());
        assertEquals(StockMovementType.RELEASE, f.inventoryService
            .findMovements(product.getId(), warehouse.getId()).get(0).type());
    }

    @Test
    void shippedOrderCannotBeCancelled() {
        Order order = place(product, 1);
        f.orderService.confirmOrder(order.getId());
        f.orderService.shipOrder(order.getId());
        assertThrows(OrderStateTransitionException.class,
            () -> f.orderService.cancelOrder(buyer.getId(), order.getId()));
    }

    @Test
    void anotherBuyerCannotCancelOrSee() {
        Order order = place(product, 1);
        Buyer intruder = f.buyer("otro@correo.com");
        assertThrows(ForbiddenActionException.class,
            () -> f.orderService.cancelOrder(intruder.getId(), order.getId()));
        assertThrows(ForbiddenActionException.class,
            () -> f.orderService.findOrderById(intruder.getId(), order.getId()));
        assertEquals(order.getId(), f.orderService.findOrderById(seller.getId(), order.getId()).getId());
    }

    @Test
    void inactiveWarehouseIsNotUsed() {
        f.warehouseService.changeStatus(warehouse.getId(), false);
        assertThrows(InsufficientStockException.class, () -> place(product, 1));
    }
}
