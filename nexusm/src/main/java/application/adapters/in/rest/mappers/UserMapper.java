package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.AddressRequest;
import application.adapters.in.rest.responses.AddressResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.domain.enums.SellerStatus;
import application.domain.models.Buyer;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.PhoneNumber;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Converts between REST DTOs and domain users. It never exposes the password hash.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        String businessName = null;
        SellerStatus sellerStatus = null;
        AddressResponse defaultShipping = null;
        if (user instanceof Seller seller) {
            businessName = seller.getBusinessName();
            sellerStatus = seller.getStatus();
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
            user.getRole(),
            businessName,
            sellerStatus,
            defaultShipping,
            addresses,
            user.getCreatedAt());
    }

    public AddressResponse toResponse(Address address) {
        return new AddressResponse(address.getStreet(), address.getCity(),
            address.getState(), address.getZipCode(), address.getCountry());
    }

    /** Null stays null, because updates treat it as "do not change". */
    public Address toDomain(AddressRequest address) {
        return address == null ? null : Address.of(address.street(), address.city(), address.state(),
            address.zipCode(), address.country());
    }

    /** Null stays null, because updates treat it as "do not change". */
    public PhoneNumber toPhone(String phone) {
        return phone == null ? null : PhoneNumber.of(phone);
    }
}
