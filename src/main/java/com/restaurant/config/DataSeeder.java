package com.restaurant.config;

import com.restaurant.model.*;
import com.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedTables();
        seedMenuItems();
    }

    private void seedUsers() {
        userRepository.findByUsername("admin").ifPresent(userRepository::delete);
        userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .active(true)
                .build());
        System.out.println(">>> Admin user ready (admin / admin123)");

        userRepository.findByUsername("kitchen").ifPresent(userRepository::delete);
        userRepository.save(User.builder()
                .username("kitchen")
                .password(passwordEncoder.encode("kitchen123"))
                .role(UserRole.KITCHEN)
                .active(true)
                .build());
        System.out.println(">>> Kitchen user ready (kitchen / kitchen123)");
    }

    private void seedTables() {
        // Fixed tokens for all 10 tables
        String[] tokens = {
            "table-01-token-0000-000000000001",
            "table-02-token-0000-000000000002",
            "table-03-token-0000-000000000003",
            "table-04-token-0000-000000000004",
            "table-05-token-0000-000000000005",
            "table-06-token-0000-000000000006",
            "table-07-token-0000-000000000007",
            "table-08-token-0000-000000000008",
            "table-09-token-0000-000000000009",
            "table-10-token-0000-000000000010"
        };

        for (int i = 1; i <= 10; i++) {
            final int tableNum = i;
            final String token = tokens[i - 1];
            tableRepository.findByTableNumber(tableNum).ifPresentOrElse(
                t -> { t.setQrToken(token); tableRepository.save(t); },
                () -> tableRepository.save(RestaurantTable.builder()
                        .tableNumber(tableNum)
                        .capacity(tableNum <= 3 ? 2 : tableNum <= 7 ? 4 : 6)
                        .qrToken(token)
                        .active(true)
                        .build())
            );
        }
        System.out.println(">>> 10 restaurant tables ready with fixed tokens");
    }

    private void seedMenuItems() {
        if (menuItemRepository.count() > 0) return;

        List<MenuItem> items = List.of(
            // Starters
            item("Bruschetta", "Grilled bread topped with fresh tomatoes, garlic, and basil", 7.50, "Starters"),
            item("Calamari Fritti", "Crispy fried squid rings with lemon aioli", 11.00, "Starters"),
            item("Soup of the Day", "Chef's daily selection served with crusty bread", 8.00, "Starters"),
            item("Chicken Wings", "Oven-baked wings in BBQ or buffalo sauce (6 pcs)", 12.50, "Starters"),
            item("Garden Salad", "Mixed greens, cherry tomatoes, cucumber, balsamic vinaigrette", 9.00, "Starters"),

            // Mains
            item("Grilled Salmon", "Atlantic salmon fillet, seasonal vegetables, lemon butter", 24.00, "Mains"),
            item("Beef Burger", "Wagyu beef patty, lettuce, tomato, cheese, brioche bun", 18.50, "Mains"),
            item("Margherita Pizza", "San marzano tomato, buffalo mozzarella, fresh basil", 16.00, "Mains"),
            item("Pasta Carbonara", "Spaghetti, pancetta, egg yolk, pecorino, black pepper", 17.50, "Mains"),
            item("Chicken Parmiggiana", "Crumbed chicken, napoli sauce, mozzarella, chips", 21.00, "Mains"),
            item("Vegetable Stir Fry", "Seasonal vegetables, tofu, oyster sauce, jasmine rice", 15.00, "Mains"),
            item("Lamb Rack", "3-bone rack, rosemary jus, roasted potatoes", 35.00, "Mains"),

            // Sides
            item("Chips & Aioli", "Crispy shoestring fries with garlic aioli", 7.00, "Sides"),
            item("Steamed Rice", "Jasmine steamed rice", 4.00, "Sides"),
            item("Garlic Bread", "Toasted sourdough with garlic butter (4 slices)", 6.00, "Sides"),

            // Desserts
            item("Chocolate Lava Cake", "Warm dark chocolate fondant, vanilla ice cream", 12.00, "Desserts"),
            item("Creme Brulee", "Classic French custard with caramelised sugar", 10.00, "Desserts"),
            item("Tiramisu", "Espresso-soaked ladyfingers, mascarpone, cocoa", 11.00, "Desserts"),

            // Drinks
            item("Soft Drink", "Coke, Diet Coke, Sprite, Fanta (330ml)", 4.00, "Drinks"),
            item("Still Water", "Purified still water (500ml)", 3.50, "Drinks"),
            item("Fresh Orange Juice", "Freshly squeezed, served chilled", 6.00, "Drinks"),
            item("House Coffee", "Espresso, Latte, Flat White, or Cappuccino", 5.00, "Drinks")
        );

        menuItemRepository.saveAll(items);
        System.out.println(">>> 22 menu items seeded");
    }

    private MenuItem item(String name, String desc, double price, String category) {
        return MenuItem.builder()
                .name(name)
                .description(desc)
                .price(BigDecimal.valueOf(price))
                .category(category)
                .available(true)
                .build();
    }
}
