package com.restaurant.repository;

import com.restaurant.model.Order;
import com.restaurant.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findByStatusInOrderByCreatedAtDesc(List<OrderStatus> statuses);

    List<Order> findByTableNumberOrderByCreatedAtDesc(Integer tableNumber);

    List<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    long countByStatus(OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :start AND o.createdAt < :end")
    long countOrdersToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :start AND o.createdAt < :end ORDER BY o.createdAt DESC")
    List<Order> findOrdersToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT oi.menuItem.name, SUM(oi.quantity)
        FROM OrderItem oi
        WHERE oi.order.createdAt >= :start AND oi.order.createdAt < :end
        GROUP BY oi.menuItem.name
        ORDER BY SUM(oi.quantity) DESC
        """)
    List<Object[]> findPopularItemsToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.createdAt >= :start AND o.createdAt < :end
          AND o.status NOT IN ('CANCELLED')
        """)
    java.math.BigDecimal sumRevenueToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
