package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.CartItemJpaEntity;
import application.adapters.out.persistence.mysql.entities.CartJpaEntity;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.valueobjects.CartId;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;

import org.springframework.stereotype.Component;

/**
 * Maps between the JPA entity and the cart domain model.
 */
@Component
public class CartEntityMapper {

    public CartJpaEntity toEntity(Cart cart) {
        CartJpaEntity entity = new CartJpaEntity();
        entity.setId(cart.getId().toString());
        entity.setBuyerId(cart.getBuyerId().toString());
        entity.setUpdatedAt(cart.getUpdatedAt());
        cart.getItems().forEach(item -> entity.getItems().add(toItemEntity(item, entity)));
        return entity;
    }

    public Cart toDomain(CartJpaEntity entity) {
        return new Cart(
            CartId.of(entity.getId()),
            UserId.of(entity.getBuyerId()),
            entity.getItems().stream().map(this::toCartItem).toList(),
            entity.getUpdatedAt());
    }

    private CartItemJpaEntity toItemEntity(CartItem item, CartJpaEntity cart) {
        CartItemJpaEntity entity = new CartItemJpaEntity();
        entity.setCart(cart);
        entity.setProductId(item.getProductId().toString());
        entity.setProductType(item.getProductType());
        entity.setQuantity(item.getQuantity().getValue());
        entity.setUnitPriceAmount(item.getUnitPrice().getAmount());
        entity.setUnitPriceCurrency(item.getUnitPrice().getCurrency().getCurrencyCode());
        return entity;
    }

    private CartItem toCartItem(CartItemJpaEntity entity) {
        return new CartItem(
            ProductId.of(entity.getProductId()),
            entity.getProductType(),
            Quantity.of(entity.getQuantity()),
            Money.of(entity.getUnitPriceAmount().toPlainString(), entity.getUnitPriceCurrency()));
    }
}
