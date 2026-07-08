package com.commerceai.catalog.admin

import com.commerceai.catalog.dto.AdminProductResponse
import com.commerceai.catalog.dto.BulkUpdateProductsRequest
import com.commerceai.catalog.dto.BulkUpdateProductsResponse
import com.commerceai.catalog.dto.CategoryResponse
import com.commerceai.catalog.dto.CreateCategoryRequest
import com.commerceai.catalog.dto.CreateProductRequest
import com.commerceai.catalog.dto.PageResponse
import com.commerceai.catalog.dto.ProductDetailResponse
import com.commerceai.catalog.dto.UpdateCategoryRequest
import com.commerceai.catalog.dto.UpdateProductRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminCatalogController(
    private val adminCatalogManagementService: AdminCatalogManagementService,
) {
    @GetMapping("/categories")
    fun listCategories(): List<CategoryResponse> = adminCatalogManagementService.listCategories()

    @PostMapping("/categories")
    fun createCategory(@Valid @RequestBody request: CreateCategoryRequest): CategoryResponse =
        adminCatalogManagementService.createCategory(request)

    @PutMapping("/categories/{id}")
    fun updateCategory(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCategoryRequest,
    ): CategoryResponse = adminCatalogManagementService.updateCategory(id, request)

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCategory(@PathVariable id: Long) {
        adminCatalogManagementService.deleteCategory(id)
    }

    @GetMapping("/products")
    fun listProducts(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(defaultValue = "false") lowStockOnly: Boolean,
        @RequestParam(required = false) active: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?,
    ): PageResponse<AdminProductResponse> = adminCatalogManagementService.listProducts(
        search = search,
        categoryId = categoryId,
        lowStockOnly = lowStockOnly,
        active = active,
        page = page,
        size = size,
        sort = sort,
    )

    @GetMapping("/products/{id}")
    fun getProduct(@PathVariable id: Long): AdminProductResponse =
        adminCatalogManagementService.getProduct(id)

    @PostMapping("/products")
    fun createProduct(@Valid @RequestBody request: CreateProductRequest): ProductDetailResponse =
        adminCatalogManagementService.createProduct(request)

    @PutMapping("/products/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProductRequest,
    ): ProductDetailResponse = adminCatalogManagementService.updateProduct(id, request)

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(@PathVariable id: Long) {
        adminCatalogManagementService.deleteProduct(id)
    }

    @PostMapping("/products/bulk-update")
    fun bulkUpdateProducts(
        @Valid @RequestBody request: BulkUpdateProductsRequest,
    ): BulkUpdateProductsResponse = adminCatalogManagementService.bulkUpdateProducts(request)
}
