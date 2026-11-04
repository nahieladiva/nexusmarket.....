package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.PriceQuoteResponse;
import application.adapters.in.rest.responses.PromotionResponse;
import application.domain.models.PriceQuote;
import application.domain.models.Promotion;
import org.springframework.stereotype.Component;

@Component
public class PromotionMapper {

    public PromotionResponse toResponse(Promotion promotion) {
        return new PromotionResponse(
            promotion.getId().toString(),
            promotion.getProductId().toString(),
            promotion.getCreatedBy().toString(),
            promotion.getDiscount().getValue().toPlainString(),
            promotion.getPeriod().getStartDate(),
            promotion.getPeriod().getEndDate(),
            promotion.isEnded(),
            promotion.getCreatedAt());
    }

    public PriceQuoteResponse toResponse(PriceQuote quote) {
        return new PriceQuoteResponse(
            quote.productId().toString(),
            quote.basePrice().getAmount().toPlainString(),
            quote.discount().getValue().toPlainString(),
            quote.finalPrice().getAmount().toPlainString(),
            quote.finalPrice().getCurrency().getCurrencyCode());
    }
}
