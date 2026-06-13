package com.restaurant.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurant_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_number", nullable = false, unique = true)
    private Integer tableNumber;

    @Column
    private Integer capacity;

    @Column(name = "qr_token", nullable = false, unique = true, length = 36)
    private String qrToken;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
