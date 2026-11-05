package application.adapters.in.rest.requests;

/**
 * DTO of a location inside a warehouse.
 */
public record WarehouseLocationRequest(String aisle, String shelf, String bin) {
}