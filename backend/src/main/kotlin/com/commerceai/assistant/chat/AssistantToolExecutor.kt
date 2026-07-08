package com.commerceai.assistant.chat

import com.commerceai.assistant.chat.dto.ShoppingContextRequest
import com.commerceai.assistant.dto.ProductCompareAssistantRequest
import com.commerceai.assistant.dto.ProductSearchAssistantRequest
import com.commerceai.assistant.dto.ShoppingAssistantRequest
import com.commerceai.assistant.ProductCompareAssistantService
import com.commerceai.assistant.ProductSearchAssistantService
import com.commerceai.assistant.ShoppingAssistantService
import com.commerceai.assistant.chat.dto.ChatRecommendation
import com.commerceai.assistant.chat.dto.ToolCallResult
import com.commerceai.recommendation.RecommendationService
import org.springframework.stereotype.Service
import java.math.BigDecimal

data class ToolExecutionResult(
    val toolCalls: List<ToolCallResult>,
    val recommendations: List<ChatRecommendation>,
    val avoidedRepeatCount: Int,
)

data class ToolPlan(
    val toolName: String,
    val message: String,
    val budget: BigDecimal?,
    val productIds: List<Long> = emptyList(),
    val productSlug: String? = null,
)

@Service
class AssistantToolExecutor(
    private val shoppingAssistantService: ShoppingAssistantService,
    private val productSearchAssistantService: ProductSearchAssistantService,
    private val productCompareAssistantService: ProductCompareAssistantService,
    private val recommendationService: RecommendationService,
) {
    fun execute(
        plans: List<ToolPlan>,
        excludedProductIds: Set<Long>,
        userId: Long?,
    ): ToolExecutionResult {
        val toolCalls = mutableListOf<ToolCallResult>()
        val recommendations = mutableListOf<ChatRecommendation>()
        var avoidedRepeatCount = 0

        for (plan in plans) {
            when (plan.toolName) {
                TOOL_RECOMMEND -> {
                    val result = shoppingAssistantService.recommend(
                        ShoppingAssistantRequest(message = plan.message, budget = plan.budget),
                    )
                    val filtered = result.recommendations.filter { item ->
                        if (item.product.id in excludedProductIds) {
                            avoidedRepeatCount++
                            false
                        } else {
                            true
                        }
                    }
                    toolCalls += ToolCallResult(
                        toolName = TOOL_RECOMMEND,
                        summary = "Recommended ${filtered.size} products for \"${result.intent}\".",
                    )
                    recommendations += filtered.map {
                        ChatRecommendation(
                            product = it.product,
                            reason = it.reason,
                            toolName = TOOL_RECOMMEND,
                        )
                    }
                }

                TOOL_SEARCH -> {
                    val result = productSearchAssistantService.search(
                        ProductSearchAssistantRequest(query = plan.message, page = 0, size = 6),
                    )
                    val filtered = result.products.content.filter { item ->
                        if (item.id in excludedProductIds) {
                            avoidedRepeatCount++
                            false
                        } else {
                            true
                        }
                    }
                    toolCalls += ToolCallResult(
                        toolName = TOOL_SEARCH,
                        summary = "Found ${filtered.size} products matching your search.",
                    )
                    recommendations += filtered.map {
                        ChatRecommendation(
                            product = it,
                            reason = "Matches your search filters.",
                            toolName = TOOL_SEARCH,
                        )
                    }
                }

                TOOL_COMPARE -> {
                    val ids = plan.productIds.distinct().take(4)
                    if (ids.size < 2) {
                        toolCalls += ToolCallResult(
                            toolName = TOOL_COMPARE,
                            summary = "Need at least 2 products to compare.",
                        )
                        continue
                    }
                    val result = productCompareAssistantService.compare(
                        ProductCompareAssistantRequest(productIds = ids, question = plan.message),
                    )
                    toolCalls += ToolCallResult(
                        toolName = TOOL_COMPARE,
                        summary = result.recommendation,
                    )
                    recommendations += result.comparisons.map {
                        ChatRecommendation(
                            product = it.product,
                            reason = it.pros.firstOrNull() ?: "Included in comparison.",
                            toolName = TOOL_COMPARE,
                        )
                    }
                }

                TOOL_PERSONALIZED -> {
                    if (userId == null) {
                        toolCalls += ToolCallResult(
                            toolName = TOOL_PERSONALIZED,
                            summary = "Sign in to unlock personalized picks from your order history.",
                        )
                        continue
                    }
                    val result = recommendationService.recommendationsForUser(userId)
                    val filtered = result.recommendations.filter { item ->
                        if (item.product.id in excludedProductIds) {
                            avoidedRepeatCount++
                            false
                        } else {
                            true
                        }
                    }
                    toolCalls += ToolCallResult(
                        toolName = TOOL_PERSONALIZED,
                        summary = "Surfaced ${filtered.size} picks based on your shopping history.",
                    )
                    recommendations += filtered.map {
                        ChatRecommendation(
                            product = it.product,
                            reason = it.reason,
                            toolName = TOOL_PERSONALIZED,
                        )
                    }
                }

                TOOL_SIMILAR -> {
                    val slug = plan.productSlug ?: continue
                    val result = recommendationService.similarProducts(slug)
                    val filtered = result.recommendations.filter { item ->
                        if (item.product.id in excludedProductIds) {
                            avoidedRepeatCount++
                            false
                        } else {
                            true
                        }
                    }
                    toolCalls += ToolCallResult(
                        toolName = TOOL_SIMILAR,
                        summary = "Found ${filtered.size} alternatives similar to what you're viewing.",
                    )
                    recommendations += filtered.map {
                        ChatRecommendation(
                            product = it.product,
                            reason = it.reason,
                            toolName = TOOL_SIMILAR,
                        )
                    }
                }
            }
        }

        return ToolExecutionResult(
            toolCalls = toolCalls,
            recommendations = recommendations.distinctBy { it.product.id },
            avoidedRepeatCount = avoidedRepeatCount,
        )
    }

    companion object {
        const val TOOL_RECOMMEND = "recommend_products"
        const val TOOL_SEARCH = "search_products"
        const val TOOL_COMPARE = "compare_products"
        const val TOOL_PERSONALIZED = "personalized_recommendations"
        const val TOOL_SIMILAR = "similar_products"
    }
}

@Service
class AssistantToolRouter {
    fun route(
        message: String,
        budget: BigDecimal?,
        context: ResolvedShoppingContext,
        history: List<AssistantMessage>,
        memoryProductIds: Set<Long>,
    ): List<ToolPlan> {
        val lower = message.lowercase()
        val resolvedBudget = budget
            ?: extractBudget(lower)
            ?: context.budgetPreference
            ?: extractBudgetFromHistory(history)

        val enrichedMessage = enrichMessageWithMemory(message, history, memoryProductIds, context)
        val plans = mutableListOf<ToolPlan>()

        if (isCompareIntent(lower) && context.compareProductIds.size >= 2) {
            plans += ToolPlan(
                toolName = AssistantToolExecutor.TOOL_COMPARE,
                message = enrichedMessage,
                budget = resolvedBudget,
                productIds = context.compareProductIds,
            )
        }

        if (isPersonalizedIntent(lower) && context.userId != null) {
            plans += ToolPlan(
                toolName = AssistantToolExecutor.TOOL_PERSONALIZED,
                message = enrichedMessage,
                budget = resolvedBudget,
            )
        }

        if (context.viewingProductSlug != null && isSimilarIntent(lower)) {
            plans += ToolPlan(
                toolName = AssistantToolExecutor.TOOL_SIMILAR,
                message = enrichedMessage,
                budget = resolvedBudget,
                productSlug = context.viewingProductSlug,
            )
        }

        if (isSearchIntent(lower)) {
            plans += ToolPlan(
                toolName = AssistantToolExecutor.TOOL_SEARCH,
                message = enrichedMessage,
                budget = resolvedBudget,
            )
        }

        if (plans.isEmpty() || isRecommendIntent(lower)) {
            plans += ToolPlan(
                toolName = AssistantToolExecutor.TOOL_RECOMMEND,
                message = buildRecommendMessage(enrichedMessage, context),
                budget = resolvedBudget,
            )
        }

        return plans.distinctBy { it.toolName }
    }

    private fun enrichMessageWithMemory(
        message: String,
        history: List<AssistantMessage>,
        memoryProductIds: Set<Long>,
        context: ResolvedShoppingContext,
    ): String {
        val lower = message.lowercase()
        if (!referencesPriorTurn(lower)) {
            return message
        }

        val priorUserMessages = history
            .filter { it.role == AssistantMessageRole.USER }
            .takeLast(3)
            .joinToString(" ") { it.content }

        val contextHints = buildList {
            if (context.stylePreference != null) add("style: ${context.stylePreference}")
            if (context.preferredCategorySlug != null) add("category: ${context.preferredCategorySlug}")
            if (memoryProductIds.isNotEmpty()) add("avoid repeating ${memoryProductIds.size} prior picks")
        }

        return listOf(message, priorUserMessages, contextHints.joinToString(", "))
            .filter { it.isNotBlank() }
            .joinToString(". ")
    }

    private fun buildRecommendMessage(message: String, context: ResolvedShoppingContext): String {
        val hints = buildList {
            context.stylePreference?.let { add("$it style") }
            context.preferredCategorySlug?.let { add("category $it") }
            if (context.savedProductIds.isNotEmpty()) {
                add("complements saved favorites")
            }
            if (context.cartProductIds.isNotEmpty()) {
                add("works with cart items")
            }
        }
        if (hints.isEmpty()) return message
        return "$message. Preferences: ${hints.joinToString(", ")}."
    }

    private fun referencesPriorTurn(lower: String): Boolean =
        listOf("those", "them", "that", "again", "more like", "another", "same", "earlier", "before")
            .any(lower::contains)

    private fun isCompareIntent(lower: String): Boolean =
        listOf("compare", "which is better", "difference between", "versus", " vs ").any(lower::contains)

    private fun isSearchIntent(lower: String): Boolean =
        listOf("find", "search", "show me", "looking for", "browse", "under $", "below $").any(lower::contains)

    private fun isPersonalizedIntent(lower: String): Boolean =
        listOf("for me", "personalized", "my style", "my orders", "order history", "based on my").any(lower::contains)

    private fun isSimilarIntent(lower: String): Boolean =
        listOf("similar", "alternative", "like this", "instead").any(lower::contains)

    private fun isRecommendIntent(lower: String): Boolean =
        listOf("recommend", "suggest", "help me", "going", "need", "want", "budget").any(lower::contains)

    private fun extractBudget(message: String): BigDecimal? {
        val match = Regex("""(?:\$|usd\s*)(\d+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?: return null
        return runCatching { BigDecimal(match) }.getOrNull()
    }

    private fun extractBudgetFromHistory(history: List<AssistantMessage>): BigDecimal? =
        history.asReversed()
            .filter { it.role == AssistantMessageRole.USER }
            .mapNotNull { extractBudget(it.content.lowercase()) }
            .firstOrNull()
}
