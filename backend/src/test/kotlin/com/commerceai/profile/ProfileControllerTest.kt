package com.commerceai.profile

import com.commerceai.auth.dto.RegisterRequest
import com.commerceai.catalog.category.Category
import com.commerceai.catalog.category.CategoryRepository
import com.commerceai.catalog.product.Product
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.order.OrderRepository
import com.commerceai.profile.dto.UpdateUserProfileRequest
import com.commerceai.support.TestDataCleaner
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var testDataCleaner: TestDataCleaner

    private lateinit var token: String
    private var productId: Long = 0

    @BeforeEach
    fun setUp() {
        testDataCleaner.cleanAll()

        val registerResponse = mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                RegisterRequest(
                    fullName = "Profile User",
                    email = "profile@example.com",
                    password = "password123",
                ),
            )
        }.andReturn().response.contentAsString

        token = objectMapper.readTree(registerResponse).get("token").asText()

        val category = categoryRepository.save(
            Category(name = "Women", slug = "women", description = "Women"),
        )

        productId = productRepository.save(
            Product(
                name = "Silk Blouse",
                slug = "silk-blouse",
                description = "Elegant blouse",
                price = BigDecimal("89.99"),
                stockQuantity = 10,
                imageUrl = "https://example.com/blouse.jpg",
                category = category,
            ),
        ).id
    }

    @Test
    fun `get and update profile`() {
        mockMvc.get("/api/profile") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.fullName") { value("Profile User") }
            jsonPath("$.email") { value("profile@example.com") }
            jsonPath("$.preferences.emailNotifications") { value(true) }
        }

        mockMvc.put("/api/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                UpdateUserProfileRequest(
                    fullName = "Updated Name",
                    stylePreference = "minimal",
                    preferredCategorySlug = "women",
                    budgetPreference = BigDecimal("150.00"),
                    emailNotifications = false,
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.fullName") { value("Updated Name") }
            jsonPath("$.preferences.stylePreference") { value("minimal") }
            jsonPath("$.preferences.preferredCategorySlug") { value("women") }
            jsonPath("$.preferences.emailNotifications") { value(false) }
        }
    }

    @Test
    fun `save list and remove saved items`() {
        mockMvc.post("/api/profile/saved-items/$productId") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.product.name") { value("Silk Blouse") }
        }

        mockMvc.get("/api/profile/saved-items") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
        }

        mockMvc.get("/api/profile") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.savedItemCount") { value(1) }
        }

        mockMvc.delete("/api/profile/saved-items/$productId") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.get("/api/profile/saved-items") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(0) }
        }
    }
}
