package com.staynest.controller;

import com.staynest.dto.ApiResponse;
import com.staynest.entity.Amenity;
import com.staynest.entity.Category;
import com.staynest.service.CategoryService;
import com.staynest.service.PropertyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;
    private final PropertyService propertyService;

    public CategoryController(CategoryService categoryService, PropertyService propertyService) {
        this.categoryService = categoryService;
        this.propertyService = propertyService;
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/amenities")
    public ResponseEntity<ApiResponse<List<Amenity>>> getAllAmenities() {
        List<Amenity> amenities = propertyService.getAllAmenities();
        return ResponseEntity.ok(ApiResponse.success(amenities));
    }
}
