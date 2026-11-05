package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.AddressRequest;
import application.adapters.in.rest.responses.AddressResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.domain.models.Buyer;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Converts between REST DTOs and domain users. It never exposes the password hash.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        String businessName = null;
        AddressResponse defaultShipping = null;
        if (user instanceof Seller seller) {
            businessName = seller.getBusinessName();
        } else if (user instanceof Buyer buyer) {
            defaultShipping = toResponse(buyer.getDefaultShippingAddress());
        }
        List<AddressResponse> addresses = user.getAddresses().stream()
            .map(this::toResponse)
            .toList();
        return new UserResponse(
            user.getId().toString(),
            user.getFullName(),
            user.getEmail().getValue(),
            user.getPhone().getValue(),
            user.getRole().name(),
            businessName,
            defaultShipping,
            addresses,
            user.getCreatedAt());
    }

    public AddressResponse toResponse(Address address) {
        return new AddressResponse(address.getStreet(), address.getCity(),
            address.getState(), address.getZipCode(), address.getCountry());
    }

    public Address toDomain(AddressRequest address) {
        if (address == null) {
            throw new IllegalArgumentException("The address is required");
        }
        return Address.of(address.street(), address.city(), address.state(),
            address.zipCode(), address.country());
    }
}
