package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.RefundResponse;
import application.domain.models.Refund;
import application.domain.valueobjects.ReturnId;
import org.springframework.stereotype.Component;

@Component
public class RefundMapper {

    public RefundResponse toResponse(Refund refund) {
        return new RefundResponse(
            refund.getId().toString(),
            refund.getOrderId().toString(),
            refund.getBuyerId().toString(),
            refund.getReturnId().map(ReturnId::toString).orElse(null),
            refund.getAmount().getAmount().toPlainString(),
            refund.getAmount().getCurrency().getCurrencyCode(),
            refund.getStatus(),
            refund.getCreatedAt(),
            refund.getResolvedAt().orElse(null));
    }
}
