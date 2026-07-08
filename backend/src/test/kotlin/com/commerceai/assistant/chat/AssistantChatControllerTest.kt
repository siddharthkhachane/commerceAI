package com.commerceai.assistant.chat

import com.commerceai.assistant.chat.dto.AssistantChatRequest
import com.commerceai.assistant.chat.dto.ShoppingContextRequest
import com.commerceai.auth.dto.RegisterRequest
import com.commerceai.catalog.category.Category
import com.commerceai.catalog.category.CategoryRepository
import com.commerceai.catalog.product.Product
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.support.TestDataCleaner
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class AssistantChatControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var testDataCleaner: TestDataCleaner

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    private val sessionId = UUID.randomUUID().toString()

    @BeforeEach
    fun setUp() {
        testDataCleaner.cleanAll()

        val category = categoryRepository.save(
            Category(name = "Winter Gear", slug = "winter-gear", description = "Cold weather"),
        )

        productRepository.save(
            Product(
                name = "Merino Base Layer",
                slug = "merino-base-layer",
                description = "Warm skiing base layer",
                price = BigDecimal("89.99"),
                stockQuantity = 20,
                imageUrl = "https://example.com/merino.jpg",
                category = category,
            ),
        )
        productRepository.save(
            Product(
                name = "Insulated Ski Jacket",
                slug = "insulated-ski-jacket",
                description = "Waterproof ski jacket",
                price = BigDecimal("249.99"),
                stockQuantity = 12,
                imageUrl = "https://example.com/jacket.jpg",
                category = category,
            ),
        )
    }

    @Test
    fun `chat uses tool calling and stores conversation memory`() {
        val firstResponse = mockMvc.post("/api/assistant/chat") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                AssistantChatRequest(
                    message = "I'm going skiing. Budget $300.",
                    sessionId = sessionId,
                    budget = BigDecimal("300"),
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.toolCalls.length()") { value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)) }
            jsonPath("$.recommendations.length()") { value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)) }
        }.andReturn().response.contentAsString

        val conversationId = objectMapper.readTree(firstResponse).get("conversationId").asLong()

        mockMvc.post("/api/assistant/chat") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                AssistantChatRequest(
                    conversationId = conversationId,
                    message = "Show me more like those under $150",
                    sessionId = sessionId,
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.usedConversationMemory") { value(true) }
            jsonPath("$.conversationId") { value(conversationId) }
        }

        mockMvc.get("/api/assistant/conversations/$conversationId") {
            param("sessionId", sessionId)
        }.andExpect {
            status { isOk() }
            jsonPath("$.messages.length()") { value(org.hamcrest.Matchers.greaterThanOrEqualTo(4)) }
        }
    }

    @Test
    fun `chat routes compare tool when compare context is provided`() {
        val products = productRepository.findAll()
        val ids = products.map { it.id }

        mockMvc.post("/api/assistant/chat") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                AssistantChatRequest(
                    message = "Compare these jackets for skiing",
                    sessionId = sessionId,
                    context = ShoppingContextRequest(compareProductIds = ids),
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.toolCalls[0].toolName") { value("compare_products") }
        }
    }

    @Test
    fun `authenticated user can list conversations`() {
        val registerResponse = mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                RegisterRequest(
                    fullName = "Chat User",
                    email = "chat@example.com",
                    password = "password123",
                ),
            )
        }.andReturn().response.contentAsString

        val token = objectMapper.readTree(registerResponse).get("token").asText()

        mockMvc.post("/api/assistant/chat") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                AssistantChatRequest(message = "Find winter jackets under $200"),
            )
        }.andExpect {
            status { isOk() }
        }

        mockMvc.get("/api/assistant/conversations") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
        }
    }
}
