package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;

public record DispatchShipmentRequest(@NotBlank String trackingNumber) {
}
