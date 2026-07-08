package com.commerceai.assistant

import com.commerceai.assistant.dto.ProductCompareAssistantRequest
import com.commerceai.assistant.dto.ProductCompareAssistantResponse
import com.commerceai.assistant.dto.ProductComparisonInsight
import com.commerceai.catalog.dto.toSummaryResponse
import com.commerceai.catalog.product.Product
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.common.BadRequestException
import com.commerceai.common.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.math.roundToInt

@Service
class ProductCompareAssistantService(
    private val productRepository: ProductRepository,
) {
    private val materials = listOf(
        "Cashmere", "Organic Cotton", "Linen", "Merino Wool", "Silk",
        "Leather", "Bamboo", "Recycled Blend", "Italian Wool", "Suede",
    )

    private val premiumMaterials = setOf(
        "Cashmere", "Silk", "Italian Wool", "Merino Wool", "Leather", "Suede",
    )

    private val styleAdjectives = listOf(
        "Essential", "Classic", "Modern", "Premium", "Soft", "Lightweight",
        "Tailored", "Relaxed", "Heritage", "Studio", "Everyday", "Signature",
    )

    @Transactional(readOnly = true)
    fun compare(request: ProductCompareAssistantRequest): ProductCompareAssistantResponse {
        val uniqueIds = request.productIds.distinct()
        if (uniqueIds.size < 2 || uniqueIds.size > 4) {
            throw BadRequestException("Select between 2 and 4 products to compare.")
        }

        val products = productRepository.findAllById(uniqueIds)
            .filter { it.isActive }
            .sortedBy { uniqueIds.indexOf(it.id) }

        if (products.size != uniqueIds.size) {
            throw NotFoundException("One or more selected products could not be found.")
        }

        val prices = products.map { it.price }
        val minPrice = prices.minOrNull() ?: BigDecimal.ZERO
        val maxPrice = prices.maxOrNull() ?: BigDecimal.ZERO

        val comparisons = products.map { product ->
            buildInsight(product, minPrice, maxPrice, products)
        }

        val winner = comparisons.maxBy { overallScore(it) }
        val question = request.question?.trim()?.takeIf { it.isNotBlank() }
            ?: "Which one is better?"

        return ProductCompareAssistantResponse(
            question = question,
            comparisons = comparisons,
            recommendedProductId = winner.product.id,
            recommendation = buildRecommendation(winner, comparisons, question),
        )
    }

    private fun buildInsight(
        product: Product,
        minPrice: BigDecimal,
        maxPrice: BigDecimal,
        allProducts: List<Product>,
    ): ProductComparisonInsight {
        val material = extractMaterial(product)
        val qualityScore = scoreQuality(product, material)
        val priceScore = scorePrice(product.price, minPrice, maxPrice)
        val styleScore = scoreStyle(product)
        val pros = buildPros(product, material, qualityScore, priceScore, styleScore, allProducts)
        val cons = buildCons(product, material, qualityScore, priceScore, allProducts)

        return ProductComparisonInsight(
            product = product.toSummaryResponse(),
            material = material,
            qualityScore = qualityScore,
            priceScore = priceScore,
            styleScore = styleScore,
            pros = pros,
            cons = cons,
        )
    }

    private fun extractMaterial(product: Product): String {
        val haystack = "${product.name} ${product.description}"
        return materials.firstOrNull { haystack.contains(it, ignoreCase = true) }
            ?: "Premium blend"
    }

    private fun scoreQuality(product: Product, material: String): Int {
        var score = when {
            material in premiumMaterials -> 9
            material == "Premium blend" -> 6
            else -> 7
        }

        if (product.name.contains("Premium", ignoreCase = true)) score += 1
        if (product.stockQuantity > 20) score += 1
        if (product.stockQuantity == 0) score -= 3

        return score.coerceIn(1, 10)
    }

    private fun scorePrice(price: BigDecimal, minPrice: BigDecimal, maxPrice: BigDecimal): Int {
        if (minPrice == maxPrice) return 8
        val range = maxPrice.subtract(minPrice)
        val position = price.subtract(minPrice).divide(range, 4, java.math.RoundingMode.HALF_UP)
        return (10 - position.toDouble() * 6).roundToInt().coerceIn(2, 10)
    }

    private fun scoreStyle(product: Product): Int {
        var score = 6
        styleAdjectives.forEach { adjective ->
            if (product.name.contains(adjective, ignoreCase = true)) {
                score += when (adjective) {
                    "Premium", "Heritage", "Tailored", "Modern" -> 2
                    "Classic", "Signature", "Studio" -> 1
                    else -> 0
                }
            }
        }
        if (product.category.name in setOf("Women", "Men", "Accessories")) score += 1
        return score.coerceIn(1, 10)
    }

    private fun buildPros(
        product: Product,
        material: String,
        qualityScore: Int,
        priceScore: Int,
        styleScore: Int,
        allProducts: List<Product>,
    ): List<String> {
        val pros = mutableListOf<String>()

        if (material in premiumMaterials) {
            pros += "Crafted from premium $material."
        } else {
            pros += "Uses dependable $material construction."
        }

        if (qualityScore >= 8) pros += "Strong overall build quality for everyday wear."
        if (priceScore >= 8) pros += "Best value in this comparison set."
        if (styleScore >= 8) pros += "Polished style that fits modern wardrobes."
        if (product.stockQuantity > 0) pros += "Available now with ${product.stockQuantity} units in stock."

        val cheapest = allProducts.minByOrNull { it.price }
        if (cheapest?.id == product.id) {
            pros += "Lowest price among the selected options."
        }

        return pros.distinct().take(4)
    }

    private fun buildCons(
        product: Product,
        material: String,
        qualityScore: Int,
        priceScore: Int,
        allProducts: List<Product>,
    ): List<String> {
        val cons = mutableListOf<String>()

        if (product.stockQuantity == 0) cons += "Currently out of stock."
        if (priceScore <= 4) cons += "Higher price than other picks in this set."
        if (qualityScore <= 5) cons += "Material tier is more entry-level than premium alternatives."
        if (material == "Recycled Blend") cons += "Blend fabric may feel less luxurious than natural fibers."

        val mostExpensive = allProducts.maxByOrNull { it.price }
        if (mostExpensive?.id == product.id && allProducts.size > 1) {
            cons += "Most expensive option in this comparison."
        }

        if (cons.isEmpty()) cons += "Few notable drawbacks for this use case."

        return cons.distinct().take(3)
    }

    private fun overallScore(insight: ProductComparisonInsight): Double =
        insight.qualityScore * 0.4 +
            insight.priceScore * 0.3 +
            insight.styleScore * 0.3

    private fun buildRecommendation(
        winner: ProductComparisonInsight,
        comparisons: List<ProductComparisonInsight>,
        question: String,
    ): String {
        val runnerUp = comparisons
            .filter { it.product.id != winner.product.id }
            .maxByOrNull { overallScore(it) }

        val base = "For \"$question\", ${winner.product.name} is the best overall pick. " +
            "It balances quality (${winner.qualityScore}/10), price value (${winner.priceScore}/10), " +
            "and style (${winner.styleScore}/10) better than the other options."

        return if (runnerUp != null) {
            "$base If you want to save more, consider ${runnerUp.product.name} instead."
        } else {
            base
        }
    }
}
