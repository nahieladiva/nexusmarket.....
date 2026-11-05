package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.ShipmentResponse;
import application.domain.models.Shipment;
import application.domain.valueobjects.UserId;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMapper {

    private final UserMapper userMapper;

    public ShipmentMapper(UserMapper userMapper) {
        this.userMapper = Objects.requireNonNull(userMapper, "User mapper is required");
    }

    public ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
            shipment.getId().toString(),
            shipment.getOrderId().toString(),
            shipment.getWarehouseId().toString(),
            userMapper.toResponse(shipment.getShippingAddress()),
            shipment.getLogisticsOperatorId().map(UserId::toString).orElse(null),
            shipment.getTrackingNumber().orElse(null),
            shipment.getStatus(),
            shipment.getCreatedAt(),
            shipment.getDispatchedAt().orElse(null),
            shipment.getDeliveredAt().orElse(null));
    }
}
