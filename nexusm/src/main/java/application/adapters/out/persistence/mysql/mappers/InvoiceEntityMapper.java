package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.InvoiceJpaEntity;
import application.domain.models.Invoice;
import application.domain.valueobjects.InvoiceId;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.UserId;

import org.springframework.stereotype.Component;

/**
 * Maps between the JPA entity and the invoice domain model.
 */
@Component
public class InvoiceEntityMapper {

    public InvoiceJpaEntity toEntity(Invoice invoice) {
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.setId(invoice.getId().toString());
        entity.setInvoiceNumber(invoice.getInvoiceNumber());
        entity.setOrderId(invoice.getOrderId().toString());
        entity.setBuyerId(invoice.getBuyerId().toString());
        entity.setTotalAmount(invoice.getTotal().getAmount());
        entity.setTotalCurrency(invoice.getTotal().getCurrency().getCurrencyCode());
        entity.setStatus(invoice.getStatus());
        entity.setIssuedAt(invoice.getIssuedAt());
        entity.setVoidedAt(invoice.getVoidedAt());
        return entity;
    }

    public Invoice toDomain(InvoiceJpaEntity entity) {
        return new Invoice(
            InvoiceId.of(entity.getId()),
            entity.getInvoiceNumber(),
            OrderId.of(entity.getOrderId()),
            UserId.of(entity.getBuyerId()),
            Money.of(entity.getTotalAmount().toPlainString(), entity.getTotalCurrency()),
            entity.getStatus(),
            entity.getIssuedAt(),
            entity.getVoidedAt());
    }
}
