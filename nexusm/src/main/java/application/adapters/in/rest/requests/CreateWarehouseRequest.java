package application.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO to create a warehouse.
 */
public record CreateWarehouseRequest(@NotBlank String name, @NotNull @Valid AddressRequest address,
                                     @NotNull @Valid WarehouseLocationRequest location) {
}
