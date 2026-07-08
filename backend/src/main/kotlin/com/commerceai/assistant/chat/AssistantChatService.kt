package com.commerceai.assistant.chat

import com.commerceai.assistant.chat.dto.AssistantChatRequest
import com.commerceai.assistant.chat.dto.AssistantChatResponse
import com.commerceai.assistant.chat.dto.ConversationDetailResponse
import com.commerceai.assistant.chat.dto.ConversationSummaryResponse
import com.commerceai.assistant.chat.dto.toResponse
import com.commerceai.assistant.chat.dto.toSummary
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.common.NotFoundException
import com.commerceai.user.User
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class AssistantChatService(
    private val conversationRepository: AssistantConversationRepository,
    private val messageRepository: AssistantMessageRepository,
    private val recommendationMemoryRepository: RecommendationMemoryRepository,
    private val productRepository: ProductRepository,
    private val shoppingContextBuilder: ShoppingContextBuilder,
    private val toolRouter: AssistantToolRouter,
    private val toolExecutor: AssistantToolExecutor,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun chat(user: User?, request: AssistantChatRequest): AssistantChatResponse {
        val sessionId = resolveSessionId(request)
        val conversation = resolveConversation(user, request, sessionId)
        val shoppingContext = shoppingContextBuilder.build(user, request.context)
        val history = messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversation.id)
        val memoryProductIds = recommendationMemoryRepository
            .findProductIdsByConversationId(conversation.id)
            .toSet()

        messageRepository.save(
            AssistantMessage(
                conversation = conversation,
                role = AssistantMessageRole.USER,
                content = request.message.trim(),
            ),
        )

        val usedMemory = history.isNotEmpty() ||
            request.message.lowercase().let { msg ->
                listOf("those", "them", "again", "more", "same").any(msg::contains)
            }

        val toolPlans = toolRouter.route(
            message = request.message.trim(),
            budget = request.budget,
            context = shoppingContext,
            history = history,
            memoryProductIds = memoryProductIds,
        )

        val execution = toolExecutor.execute(
            plans = toolPlans,
            excludedProductIds = memoryProductIds,
            userId = shoppingContext.userId,
        )

        for (toolCall in execution.toolCalls) {
            messageRepository.save(
                AssistantMessage(
                    conversation = conversation,
                    role = AssistantMessageRole.TOOL,
                    content = toolCall.summary,
                    toolName = toolCall.toolName,
                    toolPayload = objectMapper.writeValueAsString(toolCall),
                ),
            )
        }

        val reply = buildReply(execution, shoppingContext, usedMemory)
        messageRepository.save(
            AssistantMessage(
                conversation = conversation,
                role = AssistantMessageRole.ASSISTANT,
                content = reply,
            ),
        )

        for (recommendation in execution.recommendations) {
            val product = productRepository.findById(recommendation.product.id).orElse(null) ?: continue
            recommendationMemoryRepository.save(
                RecommendationMemory(
                    conversation = conversation,
                    product = product,
                    reason = recommendation.reason,
                    toolName = recommendation.toolName,
                ),
            )
        }

        if (conversation.title == "New conversation" || conversation.title.length > 80) {
            conversation.title = request.message.trim().take(80).ifBlank { "Shopping chat" }
        }
        conversation.updatedAt = Instant.now()
        conversationRepository.save(conversation)

        return AssistantChatResponse(
            conversationId = conversation.id,
            sessionId = conversation.sessionId,
            reply = reply,
            toolCalls = execution.toolCalls,
            recommendations = execution.recommendations,
            shoppingContext = shoppingContextBuilder.toSummary(shoppingContext),
            usedConversationMemory = usedMemory,
            avoidedRepeatRecommendations = execution.avoidedRepeatCount,
        )
    }

    @Transactional(readOnly = true)
    fun listConversations(user: User): List<ConversationSummaryResponse> =
        conversationRepository.findAllByUserId(user.id).map { conversation ->
            val count = messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversation.id).size
            conversation.toSummary(count)
        }

    @Transactional(readOnly = true)
    fun getConversation(user: User?, conversationId: Long, sessionId: String?): ConversationDetailResponse {
        val conversation = when {
            user != null -> conversationRepository.findByIdAndUserId(conversationId, user.id)
            sessionId != null -> conversationRepository.findByIdAndSessionId(conversationId, sessionId)
            else -> throw NotFoundException("Conversation not found")
        }.orElseThrow { NotFoundException("Conversation not found") }

        val messages = messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversation.id)
        return ConversationDetailResponse(
            id = conversation.id,
            title = conversation.title,
            sessionId = conversation.sessionId,
            messages = messages.map { it.toResponse() },
        )
    }

    private fun resolveSessionId(request: AssistantChatRequest): String =
        request.sessionId?.trim()?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

    private fun resolveConversation(
        user: User?,
        request: AssistantChatRequest,
        sessionId: String,
    ): AssistantConversation {
        request.conversationId?.let { id ->
            val existing = when {
                user != null -> conversationRepository.findByIdAndUserId(id, user.id)
                else -> conversationRepository.findByIdAndSessionId(id, sessionId)
            }
            if (existing.isPresent) {
                return existing.get()
            }
        }

        return conversationRepository.save(
            AssistantConversation(
                user = user,
                sessionId = sessionId,
                title = "New conversation",
            ),
        )
    }

    private fun buildReply(
        execution: ToolExecutionResult,
        context: ResolvedShoppingContext,
        usedMemory: Boolean,
    ): String {
        val parts = mutableListOf<String>()

        if (usedMemory) {
            parts += "I used our earlier conversation to refine these results."
        }

        if (context.cartProductIds.isNotEmpty() || context.savedProductIds.isNotEmpty()) {
            val contextBits = buildList {
                if (context.cartProductIds.isNotEmpty()) add("${context.cartProductIds.size} cart item(s)")
                if (context.savedProductIds.isNotEmpty()) add("${context.savedProductIds.size} saved favorite(s)")
            }
            parts += "I factored in your shopping context (${contextBits.joinToString(", ")})."
        }

        if (execution.recommendations.isEmpty()) {
            parts += execution.toolCalls.joinToString(" ") { it.summary }
            if (execution.avoidedRepeatCount > 0) {
                parts += "I skipped ${execution.avoidedRepeatCount} products I already recommended in this chat."
            }
            return parts.joinToString(" ")
        }

        parts += execution.toolCalls.joinToString(" ") { it.summary }
        parts += "Here are ${execution.recommendations.size} picks for you."

        if (execution.avoidedRepeatCount > 0) {
            parts += "I avoided repeating ${execution.avoidedRepeatCount} earlier recommendations."
        }

        return parts.joinToString(" ")
    }
}
