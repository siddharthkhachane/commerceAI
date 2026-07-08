package com.commerceai.assistant.chat

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

enum class AssistantMessageRole {
    USER,
    ASSISTANT,
    TOOL,
}

@Entity
@Table(name = "assistant_messages")
class AssistantMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    val conversation: AssistantConversation,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: AssistantMessageRole,
    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,
    @Column
    val toolName: String? = null,
    @Column(columnDefinition = "TEXT")
    val toolPayload: String? = null,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
)
