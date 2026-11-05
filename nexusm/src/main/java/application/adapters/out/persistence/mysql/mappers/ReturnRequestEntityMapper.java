package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.ReturnRequestJpaEntity;
import application.domain.models.ReturnRequest;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import org.springframework.stereotype.Component;

/**
 * Maps between the JPA entity and the return request domain model.
 */
@Component
public class ReturnRequestEntityMapper {

    public ReturnRequestJpaEntity toEntity(ReturnRequest returnRequest) {
        ReturnRequestJpaEntity entity = new ReturnRequestJpaEntity();
        entity.setId(returnRequest.getId().toString());
        entity.setOrderId(returnRequest.getOrderId().toString());
        entity.setBuyerId(returnRequest.getBuyerId().toString());
        entity.setProductId(returnRequest.getProductId().toString());
        entity.setQuantity(returnRequest.getQuantity().getValue());
        entity.setRefundAmount(returnRequest.getRefundAmount().getAmount());
        entity.setRefundCurrency(returnRequest.getRefundAmount().getCurrency().getCurrencyCode());
        entity.setReason(returnRequest.getReason());
        entity.setStatus(returnRequest.getStatus());
        entity.setRequestedAt(returnRequest.getRequestedAt());
        entity.setResolvedAt(returnRequest.getResolvedAt().orElse(null));
        return entity;
    }

    public ReturnRequest toDomain(ReturnRequestJpaEntity entity) {
        return new ReturnRequest(
            ReturnId.of(entity.getId()),
            OrderId.of(entity.getOrderId()),
            UserId.of(entity.getBuyerId()),
            ProductId.of(entity.getProductId()),
            Quantity.of(entity.getQuantity()),
            Money.of(entity.getRefundAmount().toPlainString(), entity.getRefundCurrency()),
            entity.getReason(),
            entity.getStatus(),
            entity.getRequestedAt(),
            entity.getResolvedAt());
    }
}
