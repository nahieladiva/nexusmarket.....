package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.ReturnResponse;
import application.domain.models.ReturnRequest;
import org.springframework.stereotype.Component;

@Component
public class ReturnMapper {

    public ReturnResponse toResponse(ReturnRequest returnRequest) {
        return new ReturnResponse(
            returnRequest.getId().toString(),
            returnRequest.getOrderId().toString(),
            returnRequest.getBuyerId().toString(),
            returnRequest.getProductId().toString(),
            returnRequest.getQuantity().getValue(),
            returnRequest.getRefundAmount().getAmount().toPlainString(),
            returnRequest.getRefundAmount().getCurrency().getCurrencyCode(),
            returnRequest.getReason(),
            returnRequest.getStatus(),
            returnRequest.getRequestedAt(),
            returnRequest.getResolvedAt().orElse(null));
    }
}
