package application.adapters.in.rest.responses;

/**
 * DTO de almacén para respuestas REST.
 */
public record WarehouseResponse(String id, String name, AddressResponse address,
                                String aisle, String shelf, String bin, boolean active) {
}