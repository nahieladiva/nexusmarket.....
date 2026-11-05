package application.adapters.in.rest.requests;

import application.domain.enums.ProductType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** The seller comes from the token, not from the body. */
public record CreateProductRequest(@NotBlank String code, @NotNull ProductType type,
                                   @NotBlank String name, String description,
                                   @NotNull @Valid PriceRequest price) {
}
