package com.commerceai.admin

import com.commerceai.cart.CartItemRepository
import com.commerceai.catalog.category.Category
import com.commerceai.catalog.category.CategoryRepository
import com.commerceai.catalog.product.Product
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.order.Order
import com.commerceai.order.OrderItem
import com.commerceai.order.OrderRepository
import com.commerceai.order.OrderStatus
import com.commerceai.support.TestDataCleaner
import com.commerceai.user.Role
import com.commerceai.user.User
import com.commerceai.user.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest
@AutoConfigureMockMvc
class BusinessAssistantControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    @Autowired
    private lateinit var testDataCleaner: TestDataCleaner

    private lateinit var adminToken: String

    @BeforeEach
    fun setUp() {
        testDataCleaner.cleanAll()

        val admin = userRepository.save(
            User(
                email = "admin@test.com",
                passwordHash = passwordEncoder.encode("admin12345"),
                fullName = "Admin",
                role = Role.ADMIN,
            ),
        )

        val customer = userRepository.save(
            User(
                email = "customer@test.com",
                passwordHash = passwordEncoder.encode("password123"),
                fullName = "Customer",
                role = Role.CUSTOMER,
            ),
        )

        val category = categoryRepository.save(
            Category(name = "Men", slug = "men", description = "Menswear"),
        )

        val product = productRepository.save(
            Product(
                name = "Wool Blazer",
                slug = "wool-blazer",
                description = "Tailored blazer",
                price = BigDecimal("120.00"),
                imageUrl = "https://picsum.photos/seed/blazer/900/1200",
                category = category,
                stockQuantity = 5,
            ),
        )

        orderRepository.save(
            buildOrder(customer, product, BigDecimal("120.00"), Instant.now().minus(2, ChronoUnit.DAYS)),
        )
        orderRepository.save(
            buildOrder(customer, product, BigDecimal("240.00"), Instant.now().minus(10, ChronoUnit.DAYS)),
        )
        orderRepository.save(
            buildOrder(customer, product, BigDecimal("240.00"), Instant.now().minus(11, ChronoUnit.DAYS)),
        )

        val loginResponse = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf("email" to admin.email, "password" to "admin12345"),
            )
        }.andReturn().response.contentAsString

        adminToken = objectMapper.readTree(loginResponse).get("token").asText()
    }

    @Test
    fun `generates business report from orders inventory and analytics`() {
        mockMvc.post("/api/admin/assistant/chat") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf("question" to "Why did sales decrease?"),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.summary") { exists() }
            jsonPath("$.insights[0].title") { exists() }
            jsonPath("$.recommendations[0]") { exists() }
            jsonPath("$.snapshot.currentPeriodOrders") { exists() }
        }
    }

    private fun buildOrder(user: User, product: Product, total: BigDecimal, createdAt: Instant): Order {
        val order = orderRepository.save(
            Order(
                user = user,
                status = OrderStatus.CONFIRMED,
                totalAmount = total,
                shippingName = user.fullName,
                shippingEmail = user.email,
                shippingAddress = "123 Main St",
                shippingCity = "Los Angeles",
                shippingState = "CA",
                shippingPostalCode = "90001",
                createdAt = createdAt,
            ),
        )
        order.items.add(
            OrderItem(
                order = order,
                productId = product.id,
                productName = product.name,
                productSlug = product.slug,
                imageUrl = product.imageUrl,
                unitPrice = total,
                quantity = 1,
            ),
        )
        return orderRepository.save(order)
    }
}
