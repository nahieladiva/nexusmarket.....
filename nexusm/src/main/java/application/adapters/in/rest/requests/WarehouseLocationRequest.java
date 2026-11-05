package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO of a location inside a warehouse.
 */
public record WarehouseLocationRequest(@NotBlank String aisle, @NotBlank String shelf,
                                       @NotBlank String bin) {
}
