package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.OrderItemJpaEntity;
import application.adapters.out.persistence.mysql.entities.OrderJpaEntity;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Mapea entre la entidad JPA y el modelo de dominio de órdenes.
 */
@Component
public class OrderEntityMapper {

    public OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(order.getId().toString());
        entity.setBuyerId(order.getBuyerId().toString());
        entity.setStatus(order.getStatus().name());
        entity.setTotalAmount(order.getTotal().getAmount());
        entity.setTotalCurrency(order.getTotal().getCurrency().getCurrencyCode());
        entity.setPaidAt(order.getPaidAt().orElse(null));
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());
        order.getItems().forEach(item -> {
            OrderItemJpaEntity itemEntity = new OrderItemJpaEntity();
            itemEntity.setProductId(item.getProductId().toString());
            itemEntity.setProductType(item.getProductType().name());
            itemEntity.setQuantity(item.getQuantity().getValue());
            itemEntity.setUnitPriceAmount(item.getUnitPrice().getAmount());
            itemEntity.setUnitPriceCurrency(item.getUnitPrice().getCurrency().getCurrencyCode());
            itemEntity.setWarehouseId(item.getWarehouseId() == null
                ? null : item.getWarehouseId().toString());
            itemEntity.setOrder(entity);
            entity.getItems().add(itemEntity);
        });
        return entity;
    }

    public Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
            .map(this::toOrderItem)
            .toList();
        return new Order(
            OrderId.of(entity.getId()),
            UserId.of(entity.getBuyerId()),
            items,
            OrderStatus.valueOf(entity.getStatus()),
            Money.of(entity.getTotalAmount().toPlainString(), entity.getTotalCurrency()),
            entity.getPaidAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt());
    }

    private OrderItem toOrderItem(OrderItemJpaEntity entity) {
        return new OrderItem(
            ProductId.of(entity.getProductId()),
            ProductType.valueOf(entity.getProductType()),
            Quantity.of(entity.getQuantity()),
            Money.of(entity.getUnitPriceAmount().toPlainString(), entity.getUnitPriceCurrency()),
            entity.getWarehouseId() == null ? null : WarehouseId.of(entity.getWarehouseId()));
    }
}