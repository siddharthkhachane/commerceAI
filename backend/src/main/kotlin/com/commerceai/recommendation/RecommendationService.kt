package com.commerceai.recommendation

import com.commerceai.catalog.product.Product
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.common.NotFoundException
import com.commerceai.order.OrderRepository
import com.commerceai.recommendation.dto.ProductRecommendation
import com.commerceai.recommendation.dto.RecommendationResponse
import com.commerceai.catalog.dto.toSummaryResponse
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class RecommendationService(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
) {
    @Transactional(readOnly = true)
    fun similarProducts(slug: String): RecommendationResponse {
        val source = productRepository.findActiveBySlug(slug)
            .orElseThrow { NotFoundException("Product not found") }

        val candidates = loadCandidates(excludeId = source.id, inStockOnly = true)
        val recommendations = candidates
            .map { candidate -> scoreSimilar(source, candidate) }
            .sortedByDescending { it.score }
            .take(4)
            .map {
                ProductRecommendation(
                    product = it.product.toSummaryResponse(),
                    reason = it.reason,
                )
            }

        return RecommendationResponse(type = "similar", recommendations = recommendations)
    }

    @Transactional(readOnly = true)
    fun relatedProducts(productIds: List<Long>): RecommendationResponse {
        val purchased = productRepository.findAllById(productIds.distinct())
        if (purchased.isEmpty()) {
            return RecommendationResponse(type = "related", recommendations = emptyList())
        }

        val excludeIds = purchased.map { it.id }.toSet()
        val candidates = loadCandidates(excludeIds = excludeIds, inStockOnly = true)
        val recommendations = candidates
            .map { candidate -> scoreRelated(purchased, candidate) }
            .filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(4)
            .map {
                ProductRecommendation(
                    product = it.product.toSummaryResponse(),
                    reason = it.reason,
                )
            }

        return RecommendationResponse(type = "related", recommendations = recommendations)
    }

    @Transactional(readOnly = true)
    fun recommendationsForUser(userId: Long): RecommendationResponse {
        val orders = orderRepository.findAllByUserId(userId)
        val purchasedProductIds = orders
            .flatMap { it.items }
            .map { it.productId }
            .distinct()

        if (purchasedProductIds.isEmpty()) {
            val fallback = productRepository.findActiveProducts(null, null, PageRequest.of(0, 4))
                .content
                .map {
                    ProductRecommendation(
                        product = it.toSummaryResponse(),
                        reason = "Popular right now in our catalog.",
                    )
                }
            return RecommendationResponse(type = "for-you", recommendations = fallback)
        }

        val related = relatedProducts(purchasedProductIds)
        return RecommendationResponse(type = "for-you", recommendations = related.recommendations)
    }

    private fun loadCandidates(
        excludeId: Long? = null,
        excludeIds: Set<Long> = emptySet(),
        inStockOnly: Boolean,
    ): List<Product> {
        val page = productRepository.findActiveProducts(null, null, PageRequest.of(0, 200))
        return page.content.filter { product ->
            product.id != excludeId &&
                product.id !in excludeIds &&
                product.isActive &&
                (!inStockOnly || product.stockQuantity > 0)
        }
    }

    private fun scoreSimilar(source: Product, candidate: Product): ScoredRecommendation {
        var score = 0
        val reasons = mutableListOf<String>()

        if (candidate.category.id == source.category.id) {
            score += 12
            reasons += "Same ${source.category.name} category"
        }

        val sharedTokens = sharedNameTokens(source.name, candidate.name)
        if (sharedTokens.isNotEmpty()) {
            score += sharedTokens.size * 4
            reasons += "Similar style: ${sharedTokens.take(2).joinToString(", ")}"
        }

        val priceDelta = priceDistance(source.price, candidate.price)
        if (priceDelta <= 0.25) {
            score += 6
            reasons += "Close to your selected price"
        }

        if (candidate.stockQuantity > 0) {
            score += 2
        }

        return ScoredRecommendation(
            product = candidate,
            score = score,
            reason = reasons.take(2).joinToString(". ").ifBlank { "A strong alternative in our catalog." },
        )
    }

    private fun scoreRelated(purchased: List<Product>, candidate: Product): ScoredRecommendation {
        var score = 0
        val reasons = mutableListOf<String>()

        val purchasedCategories = purchased.map { it.category.id }.toSet()
        if (candidate.category.id in purchasedCategories) {
            score += 10
            reasons += "Pairs with your ${candidate.category.name} purchase"
        } else if (isComplementaryCategory(purchased.first().category.name, candidate.category.name)) {
            score += 8
            reasons += "Complements your recent order"
        }

        purchased.forEach { item ->
            val overlap = sharedNameTokens(item.name, candidate.name)
            if (overlap.isNotEmpty()) {
                score += overlap.size * 3
            }
        }

        if (reasons.isEmpty()) {
            reasons += "Customers who bought similar items also liked this"
        }

        return ScoredRecommendation(
            product = candidate,
            score = score,
            reason = reasons.first(),
        )
    }

    private fun sharedNameTokens(a: String, b: String): List<String> {
        val stopWords = setOf("the", "and", "with", "for", "premium", "essential", "classic", "modern")
        val tokensA = a.lowercase().split(Regex("\\s+")).filter { it.length >= 4 && it !in stopWords }.toSet()
        val tokensB = b.lowercase().split(Regex("\\s+")).filter { it.length >= 4 && it !in stopWords }.toSet()
        return tokensA.intersect(tokensB).toList()
    }

    private fun priceDistance(a: BigDecimal, b: BigDecimal): Double {
        val max = maxOf(a, b)
        if (max.compareTo(BigDecimal.ZERO) == 0) return 0.0
        return a.subtract(b).abs().divide(max, 4, RoundingMode.HALF_UP).toDouble()
    }

    private fun isComplementaryCategory(purchasedCategory: String, candidateCategory: String): Boolean {
        val complements = mapOf(
            "Women" to setOf("Accessories", "Footwear", "Beauty"),
            "Men" to setOf("Accessories", "Footwear"),
            "Footwear" to setOf("Accessories", "Travel"),
            "Home" to setOf("Gifts", "Accessories"),
            "Travel" to setOf("Accessories", "Footwear"),
        )
        return candidateCategory in complements.getOrDefault(purchasedCategory, emptySet())
    }
}

private data class ScoredRecommendation(
    val product: Product,
    val score: Int,
    val reason: String,
)
