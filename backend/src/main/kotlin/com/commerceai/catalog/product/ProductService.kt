package com.commerceai.catalog.product

import com.commerceai.catalog.category.CategoryService
import com.commerceai.catalog.dto.CreateProductRequest
import com.commerceai.catalog.dto.PageResponse
import com.commerceai.catalog.dto.ProductDetailResponse
import com.commerceai.catalog.dto.ProductSummaryResponse
import com.commerceai.catalog.dto.UpdateProductRequest
import com.commerceai.catalog.dto.toDetailResponse
import com.commerceai.catalog.dto.toSummaryResponse
import com.commerceai.catalog.slugify
import com.commerceai.common.NotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryService: CategoryService,
) {

    fun listProducts(
        categorySlug: String?,
        search: String?,
        page: Int,
        size: Int,
        sort: String?,
    ): PageResponse<ProductSummaryResponse> {
        val pageable = buildPageable(page, size, sort)
        val result = productRepository.findActiveProducts(
            categorySlug = categorySlug?.takeIf { it.isNotBlank() },
            search = search?.trim()?.takeIf { it.isNotBlank() },
            pageable = pageable,
        )

        return PageResponse(
            content = result.content.map { it.toSummaryResponse() },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    fun getProductBySlug(slug: String): ProductDetailResponse =
        productRepository.findActiveBySlug(slug)
            .map { it.toDetailResponse() }
            .orElseThrow { NotFoundException("Product not found") }

    @Transactional
    fun createProduct(request: CreateProductRequest): ProductDetailResponse {
        val category = categoryService.getCategoryEntity(request.categoryId)
        val slug = uniqueProductSlug(slugify(request.name.trim()))

        val product = productRepository.save(
            Product(
                name = request.name.trim(),
                slug = slug,
                description = request.description.trim(),
                price = request.price,
                imageUrl = request.imageUrl.trim(),
                category = category,
                stockQuantity = request.stockQuantity,
                isActive = request.isActive,
            ),
        )

        return product.toDetailResponse()
    }

    @Transactional
    fun updateProduct(id: Long, request: UpdateProductRequest): ProductDetailResponse {
        val product = productRepository.findById(id)
            .orElseThrow { NotFoundException("Product not found") }

        val category = categoryService.getCategoryEntity(request.categoryId)
        val newSlug = uniqueProductSlug(slugify(request.name.trim()), id)

        product.name = request.name.trim()
        product.slug = newSlug
        product.description = request.description.trim()
        product.price = request.price
        product.imageUrl = request.imageUrl.trim()
        product.category = category
        product.stockQuantity = request.stockQuantity
        product.isActive = request.isActive

        return product.toDetailResponse()
    }

    @Transactional
    fun deleteProduct(id: Long) {
        if (!productRepository.existsById(id)) {
            throw NotFoundException("Product not found")
        }
        productRepository.deleteById(id)
    }

    private fun buildPageable(page: Int, size: Int, sort: String?): PageRequest {
        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 48)
        val sortSpec = when (sort) {
            "price-asc" -> Sort.by("price").ascending()
            "price-desc" -> Sort.by("price").descending()
            "name-asc" -> Sort.by("name").ascending()
            "name-desc" -> Sort.by("name").descending()
            else -> Sort.by("createdAt").descending()
        }
        return PageRequest.of(safePage, safeSize, sortSpec)
    }

    private fun uniqueProductSlug(baseSlug: String, excludeId: Long? = null): String {
        var slug = baseSlug
        var counter = 1
        while (true) {
            val existing = productRepository.findBySlug(slug)
            if (existing.isEmpty || existing.get().id == excludeId) {
                return slug
            }
            slug = "$baseSlug-$counter"
            counter++
        }
    }
}
