package com.commerceai.catalog

import com.commerceai.cart.CartItemRepository
import com.commerceai.catalog.category.Category
import com.commerceai.catalog.category.CategoryRepository
import com.commerceai.catalog.product.Product
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.order.OrderRepository
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
class AdminCatalogManagementTest {
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
    private var categoryId: Long = 0
    private var productAId: Long = 0
    private var productBId: Long = 0

    @BeforeEach
    fun setUp() {
        testDataCleaner.cleanAll()

        userRepository.save(
            User(
                email = "admin@test.com",
                passwordHash = passwordEncoder.encode("admin12345"),
                fullName = "Admin",
                role = Role.ADMIN,
            ),
        )

        categoryId = categoryRepository.save(
            Category(name = "Men", slug = "men", description = "Menswear"),
        ).id

        productAId = productRepository.save(
            Product(
                name = "Oxford Shirt",
                slug = "oxford-shirt",
                description = "Classic shirt",
                price = BigDecimal("79.00"),
                imageUrl = "https://picsum.photos/seed/shirt/900/1200",
                category = categoryRepository.findById(categoryId).get(),
                stockQuantity = 5,
            ),
        ).id

        productBId = productRepository.save(
            Product(
                name = "Chino Pant",
                slug = "chino-pant",
                description = "Everyday pant",
                price = BigDecimal("89.00"),
                imageUrl = "https://picsum.photos/seed/pant/900/1200",
                category = categoryRepository.findById(categoryId).get(),
                stockQuantity = 40,
            ),
        ).id

        val loginResponse = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf("email" to "admin@test.com", "password" to "admin12345"),
            )
        }.andReturn().response.contentAsString

        adminToken = objectMapper.readTree(loginResponse).get("token").asText()
    }

    @Test
    fun `admin manages categories products search filters and bulk update`() {
        mockMvc.get("/api/admin/products?search=Oxford&lowStockOnly=true") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[0].name") { value("Oxford Shirt") }
            jsonPath("$.content[0].isActive") { value(true) }
        }

        mockMvc.post("/api/admin/products/bulk-update") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf(
                    "productIds" to listOf(productAId, productBId),
                    "stockDelta" to 10,
                    "isActive" to true,
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.updatedCount") { value(2) }
            jsonPath("$.products[0].stockQuantity") { exists() }
        }

        mockMvc.post("/api/admin/categories") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf(
                    "name" to "Travel",
                    "description" to "Travel essentials",
                    "imageUrl" to "https://picsum.photos/seed/travel/900/1200",
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.slug") { value("travel") }
        }

        mockMvc.put("/api/admin/products/$productAId") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf(
                    "name" to "Oxford Shirt Updated",
                    "description" to "Updated shirt",
                    "price" to 99.0,
                    "imageUrl" to "https://picsum.photos/seed/shirt-new/900/1200",
                    "categoryId" to categoryId,
                    "stockQuantity" to 25,
                    "isActive" to true,
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Oxford Shirt Updated") }
        }
    }
}
