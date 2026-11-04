package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.AddressEmbeddable;
import application.adapters.out.persistence.mysql.entities.UserJpaEntity;
import application.domain.enums.UserRole;
import application.domain.models.Administrator;
import application.domain.models.Buyer;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Convierte usuarios del dominio (Buyer, Seller, Administrator) a la tabla {@code users}
 * y viceversa. Las direcciones adicionales van en {@code user_addresses}.
 */
@Component
public class UserEntityMapper {

    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity(
            user.getId().toString(),
            user.getFullName(),
            user.getEmail().getValue(),
            user.getPhone().getValue(),
            user.getRole().name(),
            user.getPasswordHash(),
            user.getCreatedAt());
        if (user instanceof Seller seller) {
            entity.setBusinessName(seller.getBusinessName());
            entity.setApproved(seller.isApproved());
        } else if (user instanceof Buyer buyer) {
            Address address = buyer.getDefaultShippingAddress();
            entity.setShippingStreet(address.getStreet());
            entity.setShippingCity(address.getCity());
            entity.setShippingState(address.getState());
            entity.setShippingZip(address.getZipCode());
            entity.setShippingCountry(address.getCountry());
        }
        entity.setAddresses(user.getAddresses().stream()
            .map(a -> new AddressEmbeddable(a.getStreet(), a.getCity(), a.getState(),
                a.getZipCode(), a.getCountry()))
            .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
        return entity;
    }

    public User toDomain(UserJpaEntity entity) {
        UserRole role = UserRole.valueOf(entity.getRole());
        UserId id = UserId.of(entity.getId());
        Email email = Email.of(entity.getEmail());
        PhoneNumber phone = PhoneNumber.of(entity.getPhone());
        List<Address> addresses = entity.getAddresses() == null ? List.of()
            : entity.getAddresses().stream()
                .map(a -> Address.of(a.getStreet(), a.getCity(), a.getState(), a.getZipCode(),
                    a.getCountry()))
                .toList();
        return switch (role) {
            case BUYER -> new Buyer(id, entity.getFullName(), email, phone, entity.getPasswordHash(),
                Address.of(entity.getShippingStreet(), entity.getShippingCity(),
                    entity.getShippingState(), entity.getShippingZip(), entity.getShippingCountry()),
                addresses, entity.getCreatedAt());
            case SELLER -> new Seller(id, entity.getFullName(), email, phone, entity.getPasswordHash(),
                entity.getBusinessName(), addresses, entity.isApproved(), entity.getCreatedAt());
            case ADMIN -> new Administrator(id, entity.getFullName(), email, phone,
                entity.getPasswordHash(), addresses, entity.getCreatedAt());
        };
    }
}
