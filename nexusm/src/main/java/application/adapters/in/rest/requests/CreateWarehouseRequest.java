package application.adapters.in.rest.requests;

/**
 * DTO to create a warehouse.
 */
public record CreateWarehouseRequest(String name, AddressRequest address,
                                     WarehouseLocationRequest location) {
}