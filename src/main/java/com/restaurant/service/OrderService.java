package com.restaurant.service;

import com.restaurant.dto.*;
import com.restaurant.exception.InvalidOrderException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.*;
import com.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;

    // ── Customer: place order ────────────────────────────────────────────────

    @Transactional
    public OrderResponseDTO placeOrder(OrderRequestDTO request) {
        // Validate table token
        RestaurantTable table = tableRepository.findByQrToken(request.getTableToken())
                .orElseThrow(() -> new InvalidOrderException("Invalid table QR code"));

        if (!table.isActive()) {
            throw new InvalidOrderException("This table is not currently active");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }

        // Build order
        Order order = Order.builder()
                .tableNumber(table.getTableNumber())
                .tableToken(request.getTableToken())
                .status(OrderStatus.PENDING)
                .customerNote(request.getCustomerNote())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemReq.getMenuItemId()));

            if (!menuItem.isAvailable()) {
                throw new InvalidOrderException("Item '" + menuItem.getName() + "' is currently unavailable");
            }

            BigDecimal unitPrice = menuItem.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .specialInstructions(itemReq.getSpecialInstructions())
                    .build();

            order.getItems().add(orderItem);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        return toResponseDTO(saved);
    }

    // ── Customer: check order status ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return toResponseDTO(order);
    }

    // ── Admin/Kitchen: list orders ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getActiveOrders() {
        return orderRepository
                .findByStatusInOrderByCreatedAtDesc(List.of(OrderStatus.PENDING, OrderStatus.PREPARING))
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByTable(Integer tableNumber) {
        return orderRepository.findByTableNumberOrderByCreatedAtDesc(tableNumber).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── Admin/Kitchen: update order status ───────────────────────────────────

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        return toResponseDTO(orderRepository.save(order));
    }

    // ── Admin dashboard summary ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        long totalToday = orderRepository.countOrdersToday(startOfDay, endOfDay);
        long pending = orderRepository.countByStatus(OrderStatus.PENDING);
        long preparing = orderRepository.countByStatus(OrderStatus.PREPARING);
        long completed = orderRepository.countByStatus(OrderStatus.COMPLETED);

        BigDecimal revenue = orderRepository.sumRevenueToday(startOfDay, endOfDay);
        if (revenue == null) revenue = BigDecimal.ZERO;

        List<Object[]> rawPopular = orderRepository.findPopularItemsToday(startOfDay, endOfDay);
        List<DashboardSummaryDTO.ItemStat> popular = rawPopular.stream()
                .limit(5)
                .map(row -> new DashboardSummaryDTO.ItemStat(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());

        return DashboardSummaryDTO.builder()
                .totalOrdersToday(totalToday)
                .pendingOrders(pending)
                .preparingOrders(preparing)
                .completedOrders(completed)
                .revenueToday(revenue)
                .popularItems(popular)
                .build();
    }

    // ── Status transition validation ─────────────────────────────────────────

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING    -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING  -> next == OrderStatus.READY     || next == OrderStatus.CANCELLED;
            case READY      -> next == OrderStatus.SERVED    || next == OrderStatus.CANCELLED;
            case SERVED     -> next == OrderStatus.COMPLETED || next == OrderStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!valid) {
            throw new InvalidOrderException(
                    "Cannot transition order from " + current + " to " + next
            );
        }
    }

    // ── DTO mapping ──────────────────────────────────────────────────────────

    public OrderResponseDTO toResponseDTO(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getItems().stream()
                .map(oi -> OrderItemResponseDTO.builder()
                        .id(oi.getId())
                        .itemName(oi.getMenuItem().getName())
                        .quantity(oi.getQuantity())
                        .unitPrice(oi.getUnitPrice())
                        .subtotal(oi.getSubtotal())
                        .specialInstructions(oi.getSpecialInstructions())
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .tableNumber(order.getTableNumber())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .customerNote(order.getCustomerNote())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
