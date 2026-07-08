package com.commerceai.assistant.chat.dto

import com.commerceai.assistant.chat.AssistantConversation
import com.commerceai.assistant.chat.AssistantMessage
import com.commerceai.assistant.chat.AssistantMessageRole
import com.commerceai.catalog.dto.ProductSummaryResponse
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant

data class ShoppingContextRequest(
    val cartProductIds: List<Long> = emptyList(),
    val savedProductIds: List<Long> = emptyList(),
    val compareProductIds: List<Long> = emptyList(),
    val viewingProductSlug: String? = null,
)

data class ShoppingContextSummary(
    val cartProductCount: Int,
    val savedProductCount: Int,
    val compareProductCount: Int,
    val viewingProductSlug: String?,
    val stylePreference: String?,
    val preferredCategorySlug: String?,
    val budgetPreference: BigDecimal?,
)

data class AssistantChatRequest(
    val conversationId: Long? = null,
    @field:NotBlank @field:Size(max = 1000) val message: String,
    val sessionId: String? = null,
    val budget: BigDecimal? = null,
    val context: ShoppingContextRequest = ShoppingContextRequest(),
)

data class ToolCallResult(
    val toolName: String,
    val summary: String,
)

data class ChatRecommendation(
    val product: ProductSummaryResponse,
    val reason: String,
    val toolName: String,
)

data class AssistantChatResponse(
    val conversationId: Long,
    val sessionId: String,
    val reply: String,
    val toolCalls: List<ToolCallResult>,
    val recommendations: List<ChatRecommendation>,
    val shoppingContext: ShoppingContextSummary,
    val usedConversationMemory: Boolean,
    val avoidedRepeatRecommendations: Int,
)

data class ChatMessageResponse(
    val id: Long,
    val role: AssistantMessageRole,
    val content: String,
    val toolName: String?,
    val createdAt: Instant,
)

data class ConversationSummaryResponse(
    val id: Long,
    val title: String,
    val updatedAt: Instant,
    val messageCount: Int,
)

data class ConversationDetailResponse(
    val id: Long,
    val title: String,
    val sessionId: String,
    val messages: List<ChatMessageResponse>,
)

fun AssistantMessage.toResponse(): ChatMessageResponse =
    ChatMessageResponse(
        id = id,
        role = role,
        content = content,
        toolName = toolName,
        createdAt = createdAt,
    )

fun AssistantConversation.toSummary(messageCount: Int): ConversationSummaryResponse =
    ConversationSummaryResponse(
        id = id,
        title = title,
        updatedAt = updatedAt,
        messageCount = messageCount,
    )
