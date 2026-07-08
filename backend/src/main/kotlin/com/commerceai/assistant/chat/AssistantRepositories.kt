package com.commerceai.assistant.chat

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface AssistantConversationRepository : JpaRepository<AssistantConversation, Long> {
    fun findByIdAndUserId(id: Long, userId: Long): Optional<AssistantConversation>

    fun findByIdAndSessionId(id: Long, sessionId: String): Optional<AssistantConversation>

    @Query(
        """
        SELECT c FROM AssistantConversation c
        WHERE c.user.id = :userId
        ORDER BY c.updatedAt DESC
        """,
    )
    fun findAllByUserId(@Param("userId") userId: Long): List<AssistantConversation>

    fun findBySessionIdAndUserIsNull(sessionId: String): List<AssistantConversation>
}

interface AssistantMessageRepository : JpaRepository<AssistantMessage, Long> {
    fun findAllByConversationIdOrderByCreatedAtAsc(conversationId: Long): List<AssistantMessage>
}

interface RecommendationMemoryRepository : JpaRepository<RecommendationMemory, Long> {
    @Query(
        """
        SELECT rm.product.id FROM RecommendationMemory rm
        WHERE rm.conversation.id = :conversationId
        """,
    )
    fun findProductIdsByConversationId(@Param("conversationId") conversationId: Long): List<Long>
}
