package com.commerceai.assistant.chat

import com.commerceai.assistant.chat.dto.AssistantChatRequest
import com.commerceai.assistant.chat.dto.AssistantChatResponse
import com.commerceai.assistant.chat.dto.ConversationDetailResponse
import com.commerceai.assistant.chat.dto.ConversationSummaryResponse
import com.commerceai.user.CurrentUserService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/assistant")
class AssistantChatController(
    private val assistantChatService: AssistantChatService,
    private val currentUserService: CurrentUserService,
) {
    @PostMapping("/chat")
    fun chat(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @Valid @RequestBody request: AssistantChatRequest,
    ): AssistantChatResponse {
        val user = userDetails?.let { currentUserService.findUser(it) }
        return assistantChatService.chat(user, request)
    }

    @GetMapping("/conversations")
    fun listConversations(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): List<ConversationSummaryResponse> {
        val user = currentUserService.requireUser(userDetails)
        return assistantChatService.listConversations(user)
    }

    @GetMapping("/conversations/{conversationId}")
    fun getConversation(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable conversationId: Long,
        @RequestParam(required = false) sessionId: String?,
    ): ConversationDetailResponse {
        val user = userDetails?.let { currentUserService.findUser(it) }
        return assistantChatService.getConversation(user, conversationId, sessionId)
    }
}
