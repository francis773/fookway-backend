package com.restaurant.controller;

import com.restaurant.dto.OrderResponseDTO;
import com.restaurant.dto.UpdateOrderStatusDTO;
import com.restaurant.service.KitchenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kitchen")
@RequiredArgsConstructor
public class KitchenController {

    private final KitchenService kitchenService;

    /**
     * GET /api/kitchen/orders
     * Returns all PENDING and PREPARING orders, sorted oldest-first.
     * Requires KITCHEN or ADMIN role.
     */
    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('KITCHEN', 'ADMIN')")
    public ResponseEntity<List<OrderResponseDTO>> getActiveOrders() {
        return ResponseEntity.ok(kitchenService.getActiveOrders());
    }

    /**
     * PUT /api/kitchen/orders/{id}/status
     * Advances an order: PENDING → PREPARING, or PREPARING → READY.
     * Requires KITCHEN or ADMIN role.
     */
    @PutMapping("/orders/{id}/status")
    @PreAuthorize("hasAnyRole('KITCHEN', 'ADMIN')")
    public ResponseEntity<OrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDTO dto) {
        return ResponseEntity.ok(kitchenService.updateCookingStatus(id, dto.getStatus()));
    }
}
