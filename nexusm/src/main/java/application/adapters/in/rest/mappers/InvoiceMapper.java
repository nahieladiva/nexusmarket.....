package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.InvoiceResponse;
import application.domain.models.Invoice;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
            invoice.getId().toString(),
            invoice.getInvoiceNumber(),
            invoice.getOrderId().toString(),
            invoice.getBuyerId().toString(),
            invoice.getTotal().getAmount().toPlainString(),
            invoice.getTotal().getCurrency().getCurrencyCode(),
            invoice.getStatus(),
            invoice.getIssuedAt(),
            invoice.getVoidedAt());
    }
}
