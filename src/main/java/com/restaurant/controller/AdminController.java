package com.restaurant.controller;

import com.restaurant.dto.DashboardSummaryDTO;
import com.restaurant.dto.OrderResponseDTO;
import com.restaurant.dto.UpdateOrderStatusDTO;
import com.restaurant.model.OrderStatus;
import com.restaurant.service.OrderService;
import com.restaurant.service.QRService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
    private final QRService qrService;

    // ── Dashboard ────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/dashboard
     * Returns aggregated stats for today: order counts, revenue, popular items.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryDTO> getDashboard() {
        return ResponseEntity.ok(orderService.getDashboardSummary());
    }

    // ── Orders management ────────────────────────────────────────────────────

    /**
     * GET /api/admin/orders
     * Returns all orders. Optional ?status= filter (e.g. PENDING, PREPARING).
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> getOrders(
            @RequestParam(required = false) OrderStatus status) {
        if (status != null) {
            return ResponseEntity.ok(orderService.getOrdersByStatus(status));
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * GET /api/admin/orders/{id}
     * Returns a single order with full item details.
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * PUT /api/admin/orders/{id}/status
     * Allows admin to transition an order to any valid next status,
     * including SERVED and COMPLETED.
     */
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDTO dto) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, dto.getStatus()));
    }

    // ── QR Code generation ───────────────────────────────────────────────────

    /**
     * GET /api/admin/tables/{tableNumber}/qr
     * Returns a PNG image of the QR code for the specified table.
     * The QR encodes the customer menu URL with the table's unique token.
     */
    @GetMapping("/tables/{tableNumber}/qr")
    public ResponseEntity<byte[]> getTableQR(@PathVariable Integer tableNumber) {
        byte[] qrImage = qrService.generateQRCode(tableNumber);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"table-" + tableNumber + "-qr.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }
}
