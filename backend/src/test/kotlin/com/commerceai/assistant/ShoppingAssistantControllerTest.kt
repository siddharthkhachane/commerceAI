package com.commerceai.assistant

import com.commerceai.cart.CartItemRepository
import com.commerceai.catalog.category.Category
import com.commerceai.catalog.category.CategoryRepository
import com.commerceai.catalog.product.Product
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.order.OrderRepository
import com.commerceai.support.TestDataCleaner
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
class ShoppingAssistantControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var testDataCleaner: TestDataCleaner

    @BeforeEach
    fun cleanDatabase() {
        testDataCleaner.cleanAll()
    }

    @Test
    fun `returns explained recommendations for natural language intent`() {
        val winter = categoryRepository.save(
            Category(
                name = "Winter Gear",
                slug = "winter-gear",
                description = "Cold weather products",
            ),
        )

        productRepository.save(
            Product(
                name = "Alpine Ski Jacket",
                slug = "alpine-ski-jacket",
                description = "Waterproof jacket for skiing and snowboarding days.",
                price = BigDecimal("159.00"),
                imageUrl = "https://picsum.photos/seed/ski-jacket/900/1200",
                category = winter,
                stockQuantity = 25,
            ),
        )

        productRepository.save(
            Product(
                name = "Thermal Winter Gloves",
                slug = "thermal-winter-gloves",
                description = "Insulated gloves for freezing mountain conditions.",
                price = BigDecimal("49.00"),
                imageUrl = "https://picsum.photos/seed/ski-gloves/900/1200",
                category = winter,
                stockQuantity = 12,
            ),
        )

        mockMvc.post("/api/assistant/shopping") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf(
                    "message" to "I'm going skiing. Budget $300.",
                    "budget" to 300,
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.recommendations.length()") { value(2) }
            jsonPath("$.recommendations[0].product.slug") { value("alpine-ski-jacket") }
            jsonPath("$.recommendations[0].reason") { exists() }
        }
    }
}
