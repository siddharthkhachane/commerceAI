package com.commerceai.catalog.category

import com.commerceai.catalog.dto.CategoryResponse
import com.commerceai.catalog.dto.CreateCategoryRequest
import com.commerceai.catalog.dto.UpdateCategoryRequest
import com.commerceai.catalog.dto.toResponse
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.catalog.slugify
import com.commerceai.common.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
) {

    fun listCategories(): List<CategoryResponse> =
        categoryRepository.findAll()
            .sortedBy { it.name }
            .map { category ->
                val count = productRepository.findActiveProducts(category.slug, null, org.springframework.data.domain.Pageable.unpaged())
                    .totalElements
                category.toResponse(count)
            }

    fun getCategoryBySlug(slug: String): CategoryResponse {
        val category = categoryRepository.findBySlug(slug)
            .orElseThrow { NotFoundException("Category not found") }
        val count = productRepository.findActiveProducts(slug, null, org.springframework.data.domain.Pageable.unpaged())
            .totalElements
        return category.toResponse(count)
    }

    @Transactional
    fun createCategory(request: CreateCategoryRequest): CategoryResponse {
        val slug = uniqueCategorySlug(slugify(request.name.trim()))
        val category = categoryRepository.save(
            Category(
                name = request.name.trim(),
                slug = slug,
                description = request.description?.trim(),
                imageUrl = request.imageUrl,
            ),
        )
        return category.toResponse(0)
    }

    @Transactional
    fun updateCategory(id: Long, request: UpdateCategoryRequest): CategoryResponse {
        val category = categoryRepository.findById(id)
            .orElseThrow { NotFoundException("Category not found") }

        val newSlug = uniqueCategorySlug(slugify(request.name.trim()), id)
        category.name = request.name.trim()
        category.slug = newSlug
        category.description = request.description?.trim()
        category.imageUrl = request.imageUrl

        return category.toResponse()
    }

    @Transactional
    fun deleteCategory(id: Long) {
        if (!categoryRepository.existsById(id)) {
            throw NotFoundException("Category not found")
        }
        categoryRepository.deleteById(id)
    }

    fun getCategoryEntity(id: Long): Category =
        categoryRepository.findById(id)
            .orElseThrow { NotFoundException("Category not found") }

    private fun uniqueCategorySlug(baseSlug: String, excludeId: Long? = null): String {
        var slug = baseSlug
        var counter = 1
        while (true) {
            val existing = categoryRepository.findBySlug(slug)
            if (existing.isEmpty || existing.get().id == excludeId) {
                return slug
            }
            slug = "$baseSlug-$counter"
            counter++
        }
    }
}

private fun Category.toResponse(): CategoryResponse = toResponse(null)
