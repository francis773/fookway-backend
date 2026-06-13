package com.restaurant.dto;

import com.restaurant.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusDTO {

    @NotNull(message = "Status is required")
    private OrderStatus status;
}
