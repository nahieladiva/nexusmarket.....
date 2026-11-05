package application.adapters.in.rest.responses;

/**
 * Warehouse DTO for REST responses.
 */
public record WarehouseResponse(String id, String name, AddressResponse address,
                                String aisle, String shelf, String bin, boolean active) {
}