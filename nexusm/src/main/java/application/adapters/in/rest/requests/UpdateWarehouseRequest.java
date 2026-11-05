package application.adapters.in.rest.requests;

import jakarta.validation.Valid;

/** Optional fields: those sent as null are not changed. */
public record UpdateWarehouseRequest(String name, @Valid AddressRequest address,
                                     @Valid WarehouseLocationRequest location) {
}
