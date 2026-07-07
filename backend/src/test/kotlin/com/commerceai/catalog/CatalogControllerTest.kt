package com.commerceai.catalog

import com.commerceai.cart.CartItemRepository
import com.commerceai.catalog.category.CategoryRepository
import com.commerceai.catalog.dto.CreateCategoryRequest
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.order.OrderRepository
import com.commerceai.user.UserRepository
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
class CatalogControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @BeforeEach
    fun cleanDatabase() {
        orderRepository.deleteAll()
        cartItemRepository.deleteAll()
        productRepository.deleteAll()
        categoryRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `list products and categories publicly`() {
        mockMvc.post("/api/admin/categories") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                CreateCategoryRequest(name = "Women", description = "Women's collection"),
            )
        }.andExpect {
            status { isForbidden() }
        }

        val category = categoryRepository.save(
            com.commerceai.catalog.category.Category(
                name = "Women",
                slug = "women",
                description = "Women's collection",
            ),
        )

        productRepository.save(
            com.commerceai.catalog.product.Product(
                name = "Cashmere Sweater",
                slug = "cashmere-sweater",
                description = "Soft everyday sweater.",
                price = BigDecimal("89.00"),
                imageUrl = "https://picsum.photos/seed/test/900/1200",
                category = category,
                stockQuantity = 20,
            ),
        )

        mockMvc.get("/api/categories").andExpect {
            status { isOk() }
            jsonPath("$[0].slug") { value("women") }
        }

        mockMvc.get("/api/products").andExpect {
            status { isOk() }
            jsonPath("$.content[0].slug") { value("cashmere-sweater") }
            jsonPath("$.totalElements") { value(1) }
        }

        mockMvc.get("/api/products/cashmere-sweater").andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Cashmere Sweater") }
        }
    }
}
