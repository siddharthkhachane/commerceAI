package com.commerceai.assistant

import com.commerceai.assistant.dto.ShoppingAssistantRecommendation
import com.commerceai.assistant.dto.ShoppingAssistantRequest
import com.commerceai.assistant.dto.ShoppingAssistantResponse
import com.commerceai.catalog.dto.toSummaryResponse
import com.commerceai.catalog.product.Product
import com.commerceai.catalog.product.ProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.math.max

@Service
class ShoppingAssistantService(
    private val productRepository: ProductRepository,
) {
    @Transactional(readOnly = true)
    fun recommend(request: ShoppingAssistantRequest): ShoppingAssistantResponse {
        val cleanedMessage = request.message.trim()
        val budget = request.budget ?: extractBudget(cleanedMessage)
        val intentTokens = extractTokens(cleanedMessage)

        val candidates = productRepository.findActiveProducts(
            categorySlug = null,
            search = null,
            pageable = PageRequest.of(0, 200, Sort.by("createdAt").descending()),
        ).content

        val recommendations = candidates
            .map { product -> scoreProduct(product, intentTokens, budget) }
            .filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(6)
            .distinctBy { it.product.id }
            .take(3)
            .map {
                ShoppingAssistantRecommendation(
                    product = it.product.toSummaryResponse(),
                    reason = buildReason(it, intentTokens, budget),
                )
            }

        return ShoppingAssistantResponse(
            intent = cleanedMessage,
            budget = budget,
            recommendations = recommendations,
        )
    }

    private fun extractTokens(message: String): Set<String> {
        val stopWords = setOf(
            "i", "im", "i'm", "going", "need", "want", "for", "with", "and",
            "the", "a", "an", "to", "my", "me", "under", "budget", "dollars",
        )

        return message.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 3 && it !in stopWords }
            .toSet()
    }

    private fun extractBudget(message: String): BigDecimal? {
        val matches = Regex("""(?:\$|usd\s*)(\d+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?: return null
        return runCatching { BigDecimal(matches) }.getOrNull()
    }

    private fun scoreProduct(
        product: Product,
        tokens: Set<String>,
        budget: BigDecimal?,
    ): ScoredProduct {
        val haystackName = product.name.lowercase()
        val haystackDescription = product.description.lowercase()
        val haystackCategory = product.category.name.lowercase()

        var keywordScore = 0
        for (token in tokens) {
            if (haystackName.contains(token)) keywordScore += 9
            if (haystackDescription.contains(token)) keywordScore += 4
            if (haystackCategory.contains(token)) keywordScore += 7
        }

        val budgetScore = when {
            budget == null -> 0
            product.price <= budget -> 8
            else -> -max(1, product.price.subtract(budget).toInt() / 20)
        }
        val stockScore = if (product.stockQuantity > 0) 3 else -8
        val total = keywordScore + budgetScore + stockScore

        return ScoredProduct(
            product = product,
            score = total,
            matchedTokens = tokens.filter {
                haystackName.contains(it) || haystackDescription.contains(it) || haystackCategory.contains(it)
            },
        )
    }

    private fun buildReason(
        scored: ScoredProduct,
        intentTokens: Set<String>,
        budget: BigDecimal?,
    ): String {
        val parts = mutableListOf<String>()

        if (scored.matchedTokens.isNotEmpty()) {
            parts += "Matches your ${scored.matchedTokens.joinToString(", ")} intent."
        } else if (intentTokens.isNotEmpty()) {
            parts += "Fits your outdoor activity request."
        }

        if (budget != null) {
            if (scored.product.price <= budget) {
                parts += "Within your $$budget budget."
            } else {
                parts += "Slightly above budget, but highly relevant."
            }
        }

        if (scored.product.stockQuantity > 0) {
            parts += "In stock right now."
        }

        return parts.joinToString(" ")
    }
}

private data class ScoredProduct(
    val product: Product,
    val score: Int,
    val matchedTokens: List<String>,
)
