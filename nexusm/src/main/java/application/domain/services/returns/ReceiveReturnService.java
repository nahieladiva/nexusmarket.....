package application.domain.services.returns;

import application.domain.enums.StockMovementType;
import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ReturnRequestRepository;
import application.domain.services.DomainService;
import application.domain.services.InventoryDomainService;
import application.domain.services.inventory.RecordStockMovementService;
import application.domain.services.refund.CreateRefundService;
import application.domain.valueobjects.ReturnId;

import java.util.Objects;

/**
 * A logistics operator receives an approved return: the units go back to the warehouse that
 * shipped them, and a pending refund is created.
 */
@DomainService
public class ReceiveReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final InventoryDomainService inventoryDomainService;
    private final RecordStockMovementService recordStockMovementService;
    private final CreateRefundService createRefundService;

    public ReceiveReturnService(ReturnRequestRepository returnRequestRepository,
                                OrderRepository orderRepository,
                                InventoryDomainService inventoryDomainService,
                                RecordStockMovementService recordStockMovementService,
                                CreateRefundService createRefundService) {
        this.returnRequestRepository = Objects.requireNonNull(returnRequestRepository);
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.inventoryDomainService = Objects.requireNonNull(inventoryDomainService);
        this.recordStockMovementService = Objects.requireNonNull(recordStockMovementService);
        this.createRefundService = Objects.requireNonNull(createRefundService);
    }

    public ReturnRequest execute(User operator, ReturnId returnId) {
        operator.requireRole(UserRole.LOGISTICS_OPERATOR);
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
            .orElseThrow(() -> new ResourceNotFoundException("Return", returnId.toString()));
        returnRequest.markAsReceived();
        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        Order order = orderRepository.findById(saved.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order", saved.getOrderId().toString()));
        Inventory inventory = inventoryDomainService.restock(saved.getProductId(),
            order.warehouseOf(saved.getProductId()), saved.getQuantity());
        recordStockMovementService.execute(inventory, StockMovementType.RETURN,
            saved.getQuantity().getValue(), "Return received", operator.getId(),
            saved.getId().toString());
        createRefundService.forReturn(saved);
        return saved;
    }
}
