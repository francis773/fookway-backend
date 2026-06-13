package com.restaurant.service;

import com.restaurant.dto.OrderResponseDTO;
import com.restaurant.dto.UpdateOrderStatusDTO;
import com.restaurant.exception.InvalidOrderException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.Order;
import com.restaurant.model.OrderStatus;
import com.restaurant.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KitchenService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    /**
     * Returns all orders the kitchen needs to act on (PENDING or PREPARING),
     * sorted oldest-first so the kitchen works through them in order.
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getActiveOrders() {
        return orderRepository
                .findByStatusInOrderByCreatedAtDesc(
                        List.of(OrderStatus.PENDING, OrderStatus.PREPARING))
                .stream()
                .map(orderService::toResponseDTO)
                .toList();
    }

    /**
     * Kitchen can only move an order PENDING → PREPARING or PREPARING → READY.
     * Any other transition is rejected.
     */
    @Transactional
    public OrderResponseDTO updateCookingStatus(Long orderId, OrderStatus newStatus) {
        if (newStatus != OrderStatus.PREPARING && newStatus != OrderStatus.READY) {
            throw new InvalidOrderException(
                    "Kitchen can only set status to PREPARING or READY. Got: " + newStatus);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Validate the specific kitchen transitions
        boolean valid = (order.getStatus() == OrderStatus.PENDING   && newStatus == OrderStatus.PREPARING)
                     || (order.getStatus() == OrderStatus.PREPARING && newStatus == OrderStatus.READY);

        if (!valid) {
            throw new InvalidOrderException(
                    "Cannot move order from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);
        return orderService.toResponseDTO(orderRepository.save(order));
    }
}
