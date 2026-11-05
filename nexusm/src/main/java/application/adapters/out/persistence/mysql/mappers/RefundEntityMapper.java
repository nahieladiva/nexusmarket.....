package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.RefundJpaEntity;
import application.domain.models.Refund;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.RefundId;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import org.springframework.stereotype.Component;

/**
 * Maps between the JPA entity and the refund domain model.
 */
@Component
public class RefundEntityMapper {

    public RefundJpaEntity toEntity(Refund refund) {
        RefundJpaEntity entity = new RefundJpaEntity();
        entity.setId(refund.getId().toString());
        entity.setOrderId(refund.getOrderId().toString());
        entity.setBuyerId(refund.getBuyerId().toString());
        entity.setReturnId(refund.getReturnId().map(ReturnId::toString).orElse(null));
        entity.setAmount(refund.getAmount().getAmount());
        entity.setCurrency(refund.getAmount().getCurrency().getCurrencyCode());
        entity.setStatus(refund.getStatus());
        entity.setCreatedAt(refund.getCreatedAt());
        entity.setResolvedAt(refund.getResolvedAt().orElse(null));
        return entity;
    }

    public Refund toDomain(RefundJpaEntity entity) {
        return new Refund(
            RefundId.of(entity.getId()),
            OrderId.of(entity.getOrderId()),
            UserId.of(entity.getBuyerId()),
            entity.getReturnId() == null ? null : ReturnId.of(entity.getReturnId()),
            Money.of(entity.getAmount().toPlainString(), entity.getCurrency()),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getResolvedAt());
    }
}
