package com.restaurant.service;

import com.restaurant.dto.MenuItemDTO;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.MenuItem;
import com.restaurant.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    // ── Public queries ───────────────────────────────────────────────────────

    public List<MenuItemDTO> getAllAvailableItems() {
        return menuItemRepository.findByAvailableTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> getAllItems() {
        return menuItemRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> getItemsByCategory(String category) {
        return menuItemRepository.findByCategoryAndAvailableTrue(category).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public MenuItemDTO getItemById(Long id) {
        return toDTO(findOrThrow(id));
    }

    public List<String> getAllCategories() {
        return menuItemRepository.findAllDistinctCategories();
    }

    // ── Admin mutations ──────────────────────────────────────────────────────

    @Transactional
    public MenuItemDTO createItem(MenuItemDTO dto) {
        MenuItem item = MenuItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .imageUrl(dto.getImageUrl())
                .available(dto.getAvailable() != null ? dto.getAvailable() : true)
                .build();
        return toDTO(menuItemRepository.save(item));
    }

    @Transactional
    public MenuItemDTO updateItem(Long id, MenuItemDTO dto) {
        MenuItem item = findOrThrow(id);
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setCategory(dto.getCategory());
        item.setImageUrl(dto.getImageUrl());
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
        return toDTO(menuItemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long id) {
        MenuItem item = findOrThrow(id);
        menuItemRepository.delete(item);
    }

    @Transactional
    public MenuItemDTO toggleAvailability(Long id) {
        MenuItem item = findOrThrow(id);
        item.setAvailable(!item.isAvailable());
        return toDTO(menuItemRepository.save(item));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private MenuItem findOrThrow(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
    }

    public MenuItemDTO toDTO(MenuItem item) {
        return MenuItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .category(item.getCategory())
                .imageUrl(item.getImageUrl())
                .available(item.isAvailable())
                .build();
    }
}
