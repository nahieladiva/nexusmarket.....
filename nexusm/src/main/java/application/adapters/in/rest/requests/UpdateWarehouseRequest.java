package application.adapters.in.rest.requests;

/** Campos opcionales: los que lleguen nulos no se cambian. */
public record UpdateWarehouseRequest(String name, AddressRequest address,
                                     WarehouseLocationRequest location) {
}
