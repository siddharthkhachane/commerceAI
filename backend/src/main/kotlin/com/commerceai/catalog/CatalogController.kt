package com.commerceai.catalog

import com.commerceai.catalog.category.CategoryService
import com.commerceai.catalog.dto.PageResponse
import com.commerceai.catalog.dto.ProductDetailResponse
import com.commerceai.catalog.dto.ProductSummaryResponse
import com.commerceai.catalog.product.ProductService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class CatalogController(
    private val categoryService: CategoryService,
    private val productService: ProductService,
) {

    @GetMapping("/categories")
    fun listCategories() = categoryService.listCategories()

    @GetMapping("/categories/{slug}")
    fun getCategory(@PathVariable slug: String) = categoryService.getCategoryBySlug(slug)

    @GetMapping("/products")
    fun listProducts(
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "24") size: Int,
        @RequestParam(required = false) sort: String?,
    ): PageResponse<ProductSummaryResponse> =
        productService.listProducts(category, search, page, size, sort)

    @GetMapping("/products/{slug}")
    fun getProduct(@PathVariable slug: String): ProductDetailResponse =
        productService.getProductBySlug(slug)
}
