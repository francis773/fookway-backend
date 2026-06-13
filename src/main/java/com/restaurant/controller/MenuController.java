package com.restaurant.controller;

import com.restaurant.dto.MenuItemDTO;
import com.restaurant.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // ── Public endpoints (no auth required) ─────────────────────────────────

    /**
     * GET /api/menu
     * Returns all available menu items, optionally filtered by category.
     */
    @GetMapping("/api/menu")
    public ResponseEntity<List<MenuItemDTO>> getMenu(
            @RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(menuService.getItemsByCategory(category));
        }
        return ResponseEntity.ok(menuService.getAllAvailableItems());
    }

    /**
     * GET /api/menu/categories
     * Returns all distinct category names that have at least one item.
     */
    @GetMapping("/api/menu/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(menuService.getAllCategories());
    }

    /**
     * GET /api/menu/{id}
     * Returns a single menu item by ID (available or not — for customer detail view).
     */
    @GetMapping("/api/menu/{id}")
    public ResponseEntity<MenuItemDTO> getMenuItem(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getItemById(id));
    }

    // ── Admin-only endpoints ─────────────────────────────────────────────────

    /**
     * GET /api/admin/menu
     * Returns ALL menu items (including unavailable) for admin management.
     */
    @GetMapping("/api/admin/menu")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MenuItemDTO>> getAllMenuItems() {
        return ResponseEntity.ok(menuService.getAllItems());
    }

    /**
     * POST /api/admin/menu
     * Creates a new menu item.
     */
    @PostMapping("/api/admin/menu")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemDTO> createMenuItem(@Valid @RequestBody MenuItemDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createItem(dto));
    }

    /**
     * PUT /api/admin/menu/{id}
     * Updates an existing menu item.
     */
    @PutMapping("/api/admin/menu/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemDTO> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemDTO dto) {
        return ResponseEntity.ok(menuService.updateItem(id, dto));
    }

    /**
     * DELETE /api/admin/menu/{id}
     * Deletes a menu item permanently.
     */
    @DeleteMapping("/api/admin/menu/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/admin/menu/{id}/toggle
     * Flips a menu item's availability flag (available ↔ unavailable).
     */
    @PatchMapping("/api/admin/menu/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemDTO> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.toggleAvailability(id));
    }
}
