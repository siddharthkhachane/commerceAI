package com.commerceai.recommendation

import com.commerceai.cart.CartItemRepository
import com.commerceai.catalog.category.Category
import com.commerceai.catalog.category.CategoryRepository
import com.commerceai.catalog.product.Product
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.order.OrderRepository
import com.commerceai.support.TestDataCleaner
import com.commerceai.user.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
class RecommendationControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    @Autowired
    private lateinit var testDataCleaner: TestDataCleaner

    private var unavailableId: Long = 0
    private var similarId: Long = 0

    @BeforeEach
    fun setUp() {
        testDataCleaner.cleanAll()

        val men = categoryRepository.save(Category(name = "Men", slug = "men", description = "Menswear"))
        val accessories = categoryRepository.save(
            Category(name = "Accessories", slug = "accessories", description = "Accessories"),
        )

        unavailableId = productRepository.save(
            Product(
                name = "Classic Wool Blazer",
                slug = "classic-wool-blazer",
                description = "Unavailable blazer",
                price = BigDecimal("180.00"),
                imageUrl = "https://picsum.photos/seed/blazer-oos/900/1200",
                category = men,
                stockQuantity = 0,
            ),
        ).id

        similarId = productRepository.save(
            Product(
                name = "Classic Wool Jacket",
                slug = "classic-wool-jacket",
                description = "In-stock alternative",
                price = BigDecimal("175.00"),
                imageUrl = "https://picsum.photos/seed/jacket/900/1200",
                category = men,
                stockQuantity = 12,
            ),
        ).id

        productRepository.save(
            Product(
                name = "Leather Belt",
                slug = "leather-belt",
                description = "Accessory complement",
                price = BigDecimal("49.00"),
                imageUrl = "https://picsum.photos/seed/belt/900/1200",
                category = accessories,
                stockQuantity = 20,
            ),
        )
    }

    @Test
    fun `returns similar in-stock alternatives for unavailable product`() {
        mockMvc.get("/api/recommendations/similar/classic-wool-blazer").andExpect {
            status { isOk() }
            jsonPath("$.type") { value("similar") }
            jsonPath("$.recommendations[0].product.slug") { value("classic-wool-jacket") }
            jsonPath("$.recommendations[0].reason") { exists() }
        }
    }

    @Test
    fun `returns related products based on purchased product ids`() {
        mockMvc.get("/api/recommendations/related?productIds=$unavailableId").andExpect {
            status { isOk() }
            jsonPath("$.type") { value("related") }
            jsonPath("$.recommendations[0].product.slug") { exists() }
            jsonPath("$.recommendations[0].reason") { exists() }
        }
    }
}
