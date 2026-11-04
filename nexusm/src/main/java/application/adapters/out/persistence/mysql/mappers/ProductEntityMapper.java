package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.ProductJpaEntity;
import application.domain.models.Product;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

import org.springframework.stereotype.Component;

/**
 * Mapea entre la entidad JPA y el modelo de dominio de productos.
 */
@Component
public class ProductEntityMapper {

    public ProductJpaEntity toEntity(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(product.getId().toString());
        entity.setCode(product.getCode().getValue());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPriceAmount(product.getPrice().getAmount());
        entity.setPriceCurrency(product.getPrice().getCurrency().getCurrencyCode());
        entity.setSellerId(product.getSellerId().toString());
        entity.setActive(product.isActive());
        entity.setCreatedAt(product.getCreatedAt());
        return entity;
    }

    public Product toDomain(ProductJpaEntity entity) {
        return new Product(
            ProductId.of(entity.getId()),
            ProductCode.of(entity.getCode()),
            entity.getName(),
            entity.getDescription(),
            Money.of(entity.getPriceAmount().toPlainString(), entity.getPriceCurrency()),
            UserId.of(entity.getSellerId()),
            entity.isActive(),
            entity.getCreatedAt());
    }
}