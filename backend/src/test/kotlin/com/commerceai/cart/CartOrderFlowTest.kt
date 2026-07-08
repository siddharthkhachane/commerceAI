package com.commerceai.cart

import com.commerceai.auth.dto.RegisterRequest
import com.commerceai.cart.dto.AddCartItemRequest
import com.commerceai.catalog.category.Category
import com.commerceai.catalog.category.CategoryRepository
import com.commerceai.catalog.product.Product
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.order.OrderRepository
import com.commerceai.order.dto.CheckoutRequest
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

@SpringBootTest
@AutoConfigureMockMvc
class CartOrderFlowTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var testDataCleaner: TestDataCleaner

    @BeforeEach
    fun cleanDatabase() {
        testDataCleaner.cleanAll()
    }

    @Test
    fun `add to cart checkout and create order`() {
        val registerResponse = mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                RegisterRequest(
                    fullName = "Cart Customer",
                    email = "cart@example.com",
                    password = "password123",
                ),
            )
        }.andReturn().response.contentAsString

        val token = objectMapper.readTree(registerResponse).get("token").asText()

        val category = categoryRepository.save(
            Category(
                name = "Women",
                slug = "women",
                description = "Women",
            ),
        )

        val product = productRepository.save(
            Product(
                name = "Cashmere Sweater",
                slug = "cashmere-sweater",
                description = "Soft sweater",
                price = BigDecimal("89.00"),
                imageUrl = "https://picsum.photos/seed/test/900/1200",
                category = category,
                stockQuantity = 10,
            ),
        )

        mockMvc.post("/api/cart/items") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer $token")
            content = objectMapper.writeValueAsString(
                AddCartItemRequest(productId = product.id, quantity = 2),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.itemCount") { value(2) }
        }

        val checkoutResponse = mockMvc.post("/api/orders/checkout") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer $token")
            content = objectMapper.writeValueAsString(
                CheckoutRequest(
                    shippingName = "Cart Customer",
                    shippingEmail = "cart@example.com",
                    shippingAddress = "123 Main St",
                    shippingCity = "Los Angeles",
                    shippingState = "CA",
                    shippingPostalCode = "90001",
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("CONFIRMED") }
            jsonPath("$.totalAmount") { value(178.00) }
        }.andReturn().response.contentAsString

        val orderId = objectMapper.readTree(checkoutResponse).get("id").asLong()

        mockMvc.get("/api/cart") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.itemCount") { value(0) }
        }

        mockMvc.get("/api/orders/$orderId") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.items[0].quantity") { value(2) }
        }

        val updatedProduct = productRepository.findById(product.id).get()
        assert(updatedProduct.stockQuantity == 8)
    }
}
