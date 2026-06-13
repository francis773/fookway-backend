package com.restaurant.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderItemRequestDTO {

    @NotNull(message = "Menu item ID is required")
    private Long menuItemId;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 50, message = "Quantity cannot exceed 50")
    private int quantity;

    private String specialInstructions;
}
