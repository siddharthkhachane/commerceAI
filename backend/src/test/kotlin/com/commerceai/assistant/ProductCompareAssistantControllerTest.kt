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
class ProductCompareAssistantControllerTest {
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

    private var jacketId: Long = 0
    private var glovesId: Long = 0

    @BeforeEach
    fun cleanDatabase() {
        testDataCleaner.cleanAll()

        val winter = categoryRepository.save(
            Category(name = "Winter Gear", slug = "winter-gear", description = "Cold weather"),
        )

        jacketId = productRepository.save(
            Product(
                name = "Premium Merino Wool Jacket",
                slug = "premium-merino-wool-jacket",
                description = "Heritage tailored jacket for cold weather.",
                price = BigDecimal("189.00"),
                imageUrl = "https://picsum.photos/seed/jacket/900/1200",
                category = winter,
                stockQuantity = 15,
            ),
        ).id

        glovesId = productRepository.save(
            Product(
                name = "Essential Recycled Blend Gloves",
                slug = "essential-recycled-blend-gloves",
                description = "Everyday gloves with basic warmth.",
                price = BigDecimal("39.00"),
                imageUrl = "https://picsum.photos/seed/gloves/900/1200",
                category = winter,
                stockQuantity = 30,
            ),
        ).id
    }

    @Test
    fun `compares products with scores pros cons and recommendation`() {
        mockMvc.post("/api/assistant/compare") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf(
                    "productIds" to listOf(jacketId, glovesId),
                    "question" to "Which one is better?",
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.comparisons.length()") { value(2) }
            jsonPath("$.comparisons[0].material") { exists() }
            jsonPath("$.comparisons[0].qualityScore") { exists() }
            jsonPath("$.comparisons[0].priceScore") { exists() }
            jsonPath("$.comparisons[0].styleScore") { exists() }
            jsonPath("$.comparisons[0].pros[0]") { exists() }
            jsonPath("$.comparisons[0].cons[0]") { exists() }
            jsonPath("$.recommendedProductId") { exists() }
            jsonPath("$.recommendation") { exists() }
        }
    }
}
