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
class ProductSearchAssistantControllerTest {
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
    fun `converts natural language to structured filters and filtered query`() {
        val men = categoryRepository.save(Category(name = "Men", slug = "men", description = "Menswear"))
        val gifts = categoryRepository.save(Category(name = "Gifts", slug = "gifts", description = "Gifting"))
        val home = categoryRepository.save(Category(name = "Home", slug = "home", description = "Homeware"))

        productRepository.save(
            Product(
                name = "Formal Wool Blazer",
                slug = "formal-wool-blazer",
                description = "Elegant wedding-ready blazer.",
                price = BigDecimal("199.00"),
                imageUrl = "https://picsum.photos/seed/p1/900/1200",
                category = men,
                stockQuantity = 10,
            ),
        )
        productRepository.save(
            Product(
                name = "Silk Ceremony Tie",
                slug = "silk-ceremony-tie",
                description = "Refined formal accessory for a wedding outfit.",
                price = BigDecimal("49.00"),
                imageUrl = "https://picsum.photos/seed/p2/900/1200",
                category = gifts,
                stockQuantity = 8,
            ),
        )
        productRepository.save(
            Product(
                name = "Home Throw Blanket",
                slug = "home-throw-blanket",
                description = "Cozy home decor piece.",
                price = BigDecimal("89.00"),
                imageUrl = "https://picsum.photos/seed/p3/900/1200",
                category = home,
                stockQuantity = 12,
            ),
        )
        productRepository.save(
            Product(
                name = "Luxury Formal Blazer",
                slug = "luxury-formal-blazer",
                description = "Wedding blazer above budget.",
                price = BigDecimal("320.00"),
                imageUrl = "https://picsum.photos/seed/p4/900/1200",
                category = men,
                stockQuantity = 4,
            ),
        )

        mockMvc.post("/api/assistant/product-search") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf(
                    "query" to "I'm attending a wedding. Need clothes under $250.",
                    "page" to 0,
                    "size" to 24,
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.filters.occasion") { value("wedding") }
            jsonPath("$.filters.maxPrice") { value(250) }
            jsonPath("$.filters.categorySlugs.length()") { value(5) }
            jsonPath("$.products.totalElements") { value(2) }
        }
    }
}
