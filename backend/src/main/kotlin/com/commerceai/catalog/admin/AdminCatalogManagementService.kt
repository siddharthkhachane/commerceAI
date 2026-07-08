package com.commerceai.catalog.admin

import com.commerceai.catalog.category.CategoryService
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
import com.commerceai.catalog.dto.toAdminResponse
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.catalog.product.ProductService
import com.commerceai.common.BadRequestException
import com.commerceai.common.NotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class AdminCatalogManagementService(
    private val productRepository: ProductRepository,
    private val productService: ProductService,
    private val categoryService: CategoryService,
) {
    private val lowStockThreshold = 15

    fun listCategories(): List<CategoryResponse> = categoryService.listCategories()

    @Transactional(readOnly = true)
    fun listProducts(
        search: String?,
        categoryId: Long?,
        lowStockOnly: Boolean,
        active: String?,
        page: Int,
        size: Int,
        sort: String?,
    ): PageResponse<AdminProductResponse> {
        val activeStatus = when (active) {
            "active" -> true
            "inactive" -> false
            else -> null
        }

        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 100),
            buildSort(sort),
        )

        val result = productRepository.findAdminProducts(
            search = search?.trim()?.takeIf { it.isNotBlank() },
            categoryId = categoryId,
            lowStockOnly = lowStockOnly,
            lowStockThreshold = lowStockThreshold,
            activeStatus = activeStatus,
            pageable = pageable,
        )

        return PageResponse(
            content = result.content.map { it.toAdminResponse() },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    fun getProduct(id: Long): AdminProductResponse =
        productRepository.findById(id)
            .map { it.toAdminResponse() }
            .orElseThrow { NotFoundException("Product not found") }

    fun createProduct(request: CreateProductRequest): ProductDetailResponse =
        productService.createProduct(request)

    fun updateProduct(id: Long, request: UpdateProductRequest): ProductDetailResponse =
        productService.updateProduct(id, request)

    fun deleteProduct(id: Long) = productService.deleteProduct(id)

    fun createCategory(request: CreateCategoryRequest): CategoryResponse =
        categoryService.createCategory(request)

    fun updateCategory(id: Long, request: UpdateCategoryRequest): CategoryResponse =
        categoryService.updateCategory(id, request)

    fun deleteCategory(id: Long) = categoryService.deleteCategory(id)

    @Transactional
    fun bulkUpdateProducts(request: BulkUpdateProductsRequest): BulkUpdateProductsResponse {
        if (request.stockQuantity == null && request.stockDelta == null &&
            request.price == null && request.isActive == null
        ) {
            throw BadRequestException("Provide at least one field to bulk update.")
        }

        val products = productRepository.findAllById(request.productIds.distinct())
        if (products.size != request.productIds.distinct().size) {
            throw NotFoundException("One or more products could not be found.")
        }

        products.forEach { product ->
            request.stockQuantity?.let { product.stockQuantity = it.coerceAtLeast(0) }
            request.stockDelta?.let {
                product.stockQuantity = (product.stockQuantity + it).coerceAtLeast(0)
            }
            request.price?.let { product.price = it.setScale(2, RoundingMode.HALF_UP) }
            request.isActive?.let { product.isActive = it }
        }

        val saved = productRepository.saveAll(products)
        return BulkUpdateProductsResponse(
            updatedCount = saved.size,
            products = saved.map { it.toAdminResponse() },
        )
    }

    private fun buildSort(sort: String?): Sort =
        when (sort) {
            "price-asc" -> Sort.by("price").ascending()
            "price-desc" -> Sort.by("price").descending()
            "name-asc" -> Sort.by("name").ascending()
            "name-desc" -> Sort.by("name").descending()
            "stock-asc" -> Sort.by("stockQuantity").ascending()
            "stock-desc" -> Sort.by("stockQuantity").descending()
            else -> Sort.by("createdAt").descending()
        }
}
