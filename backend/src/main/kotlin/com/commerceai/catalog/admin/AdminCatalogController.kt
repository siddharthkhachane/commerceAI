package com.commerceai.catalog.admin

import com.commerceai.catalog.category.CategoryService
import com.commerceai.catalog.dto.CategoryResponse
import com.commerceai.catalog.dto.CreateCategoryRequest
import com.commerceai.catalog.dto.CreateProductRequest
import com.commerceai.catalog.dto.ProductDetailResponse
import com.commerceai.catalog.dto.UpdateCategoryRequest
import com.commerceai.catalog.dto.UpdateProductRequest
import com.commerceai.catalog.product.ProductService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminCatalogController(
    private val categoryService: CategoryService,
    private val productService: ProductService,
) {

    @PostMapping("/categories")
    fun createCategory(@Valid @RequestBody request: CreateCategoryRequest): CategoryResponse =
        categoryService.createCategory(request)

    @PutMapping("/categories/{id}")
    fun updateCategory(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCategoryRequest,
    ): CategoryResponse = categoryService.updateCategory(id, request)

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCategory(@PathVariable id: Long) {
        categoryService.deleteCategory(id)
    }

    @PostMapping("/products")
    fun createProduct(@Valid @RequestBody request: CreateProductRequest): ProductDetailResponse =
        productService.createProduct(request)

    @PutMapping("/products/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProductRequest,
    ): ProductDetailResponse = productService.updateProduct(id, request)

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(@PathVariable id: Long) {
        productService.deleteProduct(id)
    }
}
