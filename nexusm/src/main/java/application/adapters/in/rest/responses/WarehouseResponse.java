package application.adapters.in.rest.responses;

import application.domain.enums.WarehouseStatus;
import application.domain.enums.WarehouseType;

/**
 * Warehouse DTO for REST responses.
 */
public record WarehouseResponse(String id, WarehouseType type, String ownerSellerId, String name,
                                AddressResponse address, String aisle, String shelf, String bin,
                                WarehouseStatus status) {
}
