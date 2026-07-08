package com.commerceai.assistant

import com.commerceai.assistant.dto.ProductSearchAssistantRequest
import com.commerceai.assistant.dto.ProductSearchAssistantResponse
import com.commerceai.assistant.dto.ProductSearchFilters
import com.commerceai.catalog.dto.PageResponse
import com.commerceai.catalog.dto.toSummaryResponse
import com.commerceai.catalog.product.ProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class ProductSearchAssistantService(
    private val productRepository: ProductRepository,
) {
    @Transactional(readOnly = true)
    fun search(request: ProductSearchAssistantRequest): ProductSearchAssistantResponse {
        val normalized = request.query.trim()
        val maxPrice = extractMaxPrice(normalized)
        val occasion = detectOccasion(normalized)
        val categorySlugs = mapOccasionToCategories(occasion)
        val keywords = extractKeywords(normalized)

        val pageable = PageRequest.of(
            request.page.coerceAtLeast(0),
            request.size.coerceIn(1, 48),
            buildSort(request.sort),
        )

        val searchText = keywords.joinToString(" ").takeIf { it.isNotBlank() }
        val categoryFilterOff = categorySlugs.isEmpty()
        val safeSlugs = if (categoryFilterOff) listOf("__all__") else categorySlugs

        val result = productRepository.findActiveProductsByFilters(
            categoryFilterOff = categoryFilterOff,
            categorySlugs = safeSlugs,
            search = searchText,
            maxPrice = maxPrice,
            pageable = pageable,
        )

        return ProductSearchAssistantResponse(
            filters = ProductSearchFilters(
                normalizedQuery = normalized,
                occasion = occasion,
                categorySlugs = categorySlugs,
                keywords = keywords,
                maxPrice = maxPrice,
            ),
            products = PageResponse(
                content = result.content.map { it.toSummaryResponse() },
                page = result.number,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
            ),
        )
    }

    private fun extractMaxPrice(query: String): BigDecimal? {
        val patterns = listOf(
            Regex("""(?:under|below|less than)\s*\$?\s*(\d+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
            Regex("""\$+\s*(\d+(?:\.\d{1,2})?)"""),
            Regex("""budget\s*\$?\s*(\d+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        )
        val raw = patterns.asSequence()
            .mapNotNull { it.find(query)?.groupValues?.getOrNull(1) }
            .firstOrNull()
            ?: return null
        return runCatching { BigDecimal(raw) }.getOrNull()
    }

    private fun detectOccasion(query: String): String? {
        val lower = query.lowercase()
        return when {
            listOf("wedding", "ceremony", "reception", "formal event").any(lower::contains) -> "wedding"
            listOf("ski", "skiing", "snow", "winter trip").any(lower::contains) -> "skiing"
            listOf("beach", "vacation", "resort", "summer trip").any(lower::contains) -> "beach"
            listOf("business", "office", "work", "meeting").any(lower::contains) -> "work"
            listOf("gym", "workout", "training", "running").any(lower::contains) -> "fitness"
            else -> null
        }
    }

    private fun mapOccasionToCategories(occasion: String?): List<String> =
        when (occasion) {
            "wedding" -> listOf("women", "men", "accessories", "footwear", "gifts")
            "skiing" -> listOf("men", "women", "accessories", "footwear", "travel")
            "beach" -> listOf("women", "men", "accessories", "footwear", "travel")
            "work" -> listOf("men", "women", "footwear", "accessories")
            "fitness" -> listOf("men", "women", "footwear", "accessories")
            else -> emptyList()
        }

    private fun extractKeywords(query: String): List<String> {
        val stopWords = setOf(
            "i", "im", "i'm", "attending", "a", "an", "the", "need", "needs", "want", "wanting",
            "for", "to", "with", "under", "below", "less", "than", "budget", "clothes", "clothing",
            "outfit", "something", "some", "and", "or", "of", "on", "at", "is", "are",
        )
        return query.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 3 && it !in stopWords && it.toDoubleOrNull() == null }
            .distinct()
            .take(8)
    }

    private fun buildSort(sort: String?): Sort =
        when (sort) {
            "price-asc" -> Sort.by("price").ascending()
            "price-desc" -> Sort.by("price").descending()
            "name-asc" -> Sort.by("name").ascending()
            "name-desc" -> Sort.by("name").descending()
            else -> Sort.by("createdAt").descending()
        }
}
