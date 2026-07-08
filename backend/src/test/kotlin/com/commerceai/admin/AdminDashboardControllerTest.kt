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
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
class AdminDashboardControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

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
                fullName = "Admin User",
                role = Role.ADMIN,
            ),
        )

        val customer = userRepository.save(
            User(
                email = "customer@test.com",
                passwordHash = passwordEncoder.encode("password123"),
                fullName = "Customer User",
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
                stockQuantity = 8,
            ),
        )

        val order = orderRepository.save(
            Order(
                user = customer,
                status = OrderStatus.CONFIRMED,
                totalAmount = BigDecimal("120.00"),
                shippingName = "Customer User",
                shippingEmail = "customer@test.com",
                shippingAddress = "123 Main St",
                shippingCity = "Los Angeles",
                shippingState = "CA",
                shippingPostalCode = "90001",
            ),
        )

        order.items.add(
            OrderItem(
                order = order,
                productId = product.id,
                productName = product.name,
                productSlug = product.slug,
                imageUrl = product.imageUrl,
                unitPrice = product.price,
                quantity = 1,
            ),
        )
        orderRepository.save(order)

        val loginResponse = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf("email" to admin.email, "password" to "admin12345"),
            )
        }.andReturn().response.contentAsString

        adminToken = objectMapper.readTree(loginResponse).get("token").asText()
    }

    @Test
    fun `returns dashboard metrics for admin`() {
        mockMvc.get("/api/admin/dashboard") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalRevenue") { value(120.0) }
            jsonPath("$.totalOrders") { value(1) }
            jsonPath("$.totalProducts") { value(1) }
            jsonPath("$.lowStockProducts") { value(1) }
            jsonPath("$.totalCustomers") { value(1) }
        }
    }

    @Test
    fun `returns analytics charts data for admin`() {
        mockMvc.get("/api/admin/analytics?days=30") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.revenueSeries[0].date") { exists() }
            jsonPath("$.ordersSeries[0].date") { exists() }
            jsonPath("$.topProducts[0].productName") { value("Wool Blazer") }
            jsonPath("$.categoryBreakdown[0].categoryName") { value("Men") }
        }
    }
}
