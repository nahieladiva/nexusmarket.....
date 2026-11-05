package application.domain.services.shipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.enums.OrderStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.models.Buyer;
import application.domain.models.LogisticsOperator;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.Shipment;
import application.domain.models.Warehouse;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.Quantity;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the shipment use cases.
 */
class ShipmentServicesTest {

    private TestFixture f;
    private Buyer buyer;
    private LogisticsOperator operator;
    private Warehouse first;
    private Warehouse second;
    private Order order;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        buyer = f.buyer("buyer@mail.com");
        operator = f.logisticsOperator("operator@mail.com");
        Seller seller = f.approvedSeller("seller@mail.com");
        first = f.warehouse("First");
        second = f.warehouse("Second");
        Product firstProduct = f.product(seller, "TEC-0001", "1000");
        Product secondProduct = f.product(seller, "TEC-0002", "2000");
        f.stock(seller, firstProduct, first, 10, 1);
        f.stock(seller, secondProduct, second, 10, 1);
        f.addItemToCart.execute(buyer, firstProduct.getId(), Quantity.of(1));
        f.addItemToCart.execute(buyer, secondProduct.getId(), Quantity.of(1));
        order = f.payOrder.execute(buyer, f.placeOrder.execute(buyer).getId());
    }

    private Shipment dispatch(Warehouse warehouse) {
        Shipment shipment = f.createShipment.execute(operator, order.getId(), warehouse.getId());
        f.packShipment.execute(operator, shipment.getId());
        return f.dispatchShipment.execute(operator, shipment.getId(), "TRACK-1");
    }

    private OrderStatus orderStatus() {
        return f.orders.findById(order.getId()).orElseThrow().getStatus();
    }

    @Test
    void orderAdvancesOnlyWhenEveryWarehouseHasShipped() {
        Shipment firstShipment = dispatch(first);
        assertEquals(ShipmentStatus.IN_TRANSIT, firstShipment.getStatus());
        assertEquals(OrderStatus.PAID, orderStatus());
        Shipment secondShipment = dispatch(second);
        assertEquals(OrderStatus.SHIPPED, orderStatus());

        f.confirmDelivery.execute(operator, firstShipment.getId());
        assertEquals(OrderStatus.SHIPPED, orderStatus());
        f.confirmDelivery.execute(operator, secondShipment.getId());
        assertEquals(OrderStatus.DELIVERED, orderStatus());
    }

    @Test
    void earlyDeliveryDoesNotBlockTheOrder() {
        Shipment firstShipment = dispatch(first);
        f.confirmDelivery.execute(operator, firstShipment.getId());
        Shipment secondShipment = dispatch(second);
        assertEquals(OrderStatus.SHIPPED, orderStatus());
        f.confirmDelivery.execute(operator, secondShipment.getId());
        assertEquals(OrderStatus.DELIVERED, orderStatus());
    }

    @Test
    void aWarehouseHasOnlyOneShipmentPerOrder() {
        f.createShipment.execute(operator, order.getId(), first.getId());
        assertThrows(DuplicateResourceException.class,
            () -> f.createShipment.execute(operator, order.getId(), first.getId()));
    }

    @Test
    void shipmentGoesToTheBuyerAddress() {
        Shipment shipment = f.createShipment.execute(operator, order.getId(), first.getId());
        assertEquals(buyer.getDefaultShippingAddress(), shipment.getShippingAddress());
    }

    @Test
    void onlyLogisticsOperatorsHandleShipments() {
        assertThrows(ForbiddenActionException.class,
            () -> f.createShipment.execute(buyer, order.getId(), first.getId()));
    }

    @Test
    void orderWithShipmentsCannotBeCancelled() {
        f.createShipment.execute(operator, order.getId(), first.getId());
        assertThrows(BusinessRuleException.class, () -> f.cancelOrder.execute(buyer, order.getId()));
        assertEquals(OrderStatus.PAID, orderStatus());
    }

    @Test
    void shipmentsAreVisibleOnlyToWhoCanSeeTheOrder() {
        OrderId id = order.getId();
        f.createShipment.execute(operator, id, first.getId());
        assertEquals(1, f.consultShipment.findByOrder(buyer, id).size());
        Buyer other = f.buyer("other@mail.com");
        assertThrows(ForbiddenActionException.class, () -> f.consultShipment.findByOrder(other, id));
        assertThrows(ForbiddenActionException.class, () -> f.consultShipment.findAll(buyer));
    }
}
