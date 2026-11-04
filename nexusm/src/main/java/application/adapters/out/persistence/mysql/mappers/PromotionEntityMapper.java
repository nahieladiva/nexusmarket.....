package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.PromotionJpaEntity;
import application.domain.models.Promotion;
import application.domain.valueobjects.DateRange;
import application.domain.valueobjects.Percentage;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.PromotionId;
import application.domain.valueobjects.UserId;
import org.springframework.stereotype.Component;

@Component
public class PromotionEntityMapper {

    public PromotionJpaEntity toEntity(Promotion promotion) {
        PromotionJpaEntity entity = new PromotionJpaEntity();
        entity.setId(promotion.getId().toString());
        entity.setProductId(promotion.getProductId().toString());
        entity.setCreatedBy(promotion.getCreatedBy().toString());
        entity.setDiscountPercent(promotion.getDiscount().getValue());
        entity.setStartDate(promotion.getPeriod().getStartDate());
        entity.setEndDate(promotion.getPeriod().getEndDate());
        entity.setEnded(promotion.isEnded());
        entity.setCreatedAt(promotion.getCreatedAt());
        return entity;
    }

    public Promotion toDomain(PromotionJpaEntity entity) {
        return new Promotion(
            PromotionId.of(entity.getId()),
            ProductId.of(entity.getProductId()),
            UserId.of(entity.getCreatedBy()),
            Percentage.of(entity.getDiscountPercent()),
            DateRange.of(entity.getStartDate(), entity.getEndDate()),
            entity.isEnded(),
            entity.getCreatedAt());
    }
}
