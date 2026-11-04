package application.domain.services;

import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Coordina el stock con el ciclo de vida de la orden.
 *
 * <p>Reglas:</p>
 * <ul>
 *   <li>Al confirmar se asigna bodega a cada ítem y se reserva (descuenta) el stock.</li>
 *   <li>Al cancelar una orden confirmada se devuelve el stock a la misma bodega.</li>
 *   <li>Cancelar una orden pendiente no toca el stock, porque nunca se reservó.</li>
 * </ul>
 * Este servicio no persiste la orden: eso lo hace el caso de uso.
 */
public final class OrderFulfillmentDomainService {

    private final InventoryDomainService inventoryDomainService;
    private final WarehouseAllocationService warehouseAllocationService;

    public OrderFulfillmentDomainService(InventoryDomainService inventoryDomainService,
                                         WarehouseAllocationService warehouseAllocationService) {
        this.inventoryDomainService = Objects.requireNonNull(inventoryDomainService,
            "inventoryDomainService es obligatorio");
        this.warehouseAllocationService = Objects.requireNonNull(warehouseAllocationService,
            "warehouseAllocationService es obligatorio");
    }

    /**
     * Confirma la orden reservando stock.
     *
     * @return inventarios modificados (para registrar movimientos y alertas de stock bajo)
     */
    public List<Inventory> confirm(Order order) {
        Map<ProductId, WarehouseId> allocation = warehouseAllocationService.allocate(order.getItems());
        order.assignWarehouses(allocation);
        order.confirm();
        List<Inventory> touched = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            touched.add(inventoryDomainService.reserve(
                item.getProductId(), item.getWarehouseId(), item.getQuantity()));
        }
        return touched;
    }

    /**
     * Cancela la orden y, si tenía stock reservado, lo devuelve.
     *
     * @return inventarios modificados (vacío si la orden estaba pendiente)
     */
    public List<Inventory> cancel(Order order) {
        boolean hadReservation = order.hasReservedStock();
        order.cancel();
        List<Inventory> touched = new ArrayList<>();
        if (hadReservation) {
            for (OrderItem item : order.getItems()) {
                touched.add(inventoryDomainService.restock(
                    item.getProductId(), item.getWarehouseId(), item.getQuantity()));
            }
        }
        return touched;
    }
}
