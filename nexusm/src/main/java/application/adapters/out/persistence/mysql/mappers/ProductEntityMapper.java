package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.ProductJpaEntity;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Product;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

import org.springframework.stereotype.Component;

/**
 * Maps between the JPA entity and the product domain model.
 */
@Component
public class ProductEntityMapper {

    public ProductJpaEntity toEntity(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(product.getId().toString());
        entity.setCode(product.getCode().getValue());
        entity.setType(product.getType().name());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPriceAmount(product.getPrice().getAmount());
        entity.setPriceCurrency(product.getPrice().getCurrency().getCurrencyCode());
        entity.setSellerId(product.getSellerId().toString());
        entity.setStatus(product.getStatus().name());
        entity.setCreatedAt(product.getCreatedAt());
        return entity;
    }

    public Product toDomain(ProductJpaEntity entity) {
        return new Product(
            ProductId.of(entity.getId()),
            ProductCode.of(entity.getCode()),
            ProductType.valueOf(entity.getType()),
            entity.getName(),
            entity.getDescription(),
            Money.of(entity.getPriceAmount().toPlainString(), entity.getPriceCurrency()),
            UserId.of(entity.getSellerId()),
            ProductStatus.valueOf(entity.getStatus()),
            entity.getCreatedAt());
    }
}