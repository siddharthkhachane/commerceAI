package com.commerceai.config

import com.commerceai.catalog.category.Category
import com.commerceai.catalog.category.CategoryRepository
import com.commerceai.catalog.product.Product
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.catalog.slugify
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.math.BigDecimal
import kotlin.random.Random

@Configuration
@Profile("local")
class CatalogDataSeeder {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun seedCatalog(
        categoryRepository: CategoryRepository,
        productRepository: ProductRepository,
    ): CommandLineRunner = CommandLineRunner {
        if (productRepository.count() >= 100) {
            log.info("Catalog already seeded with {} products", productRepository.count())
            return@CommandLineRunner
        }

        categoryRepository.deleteAll()
        productRepository.deleteAll()

        val categories = createCategories(categoryRepository)
        val products = buildProducts(categories)
        productRepository.saveAll(products)

        log.info("Seeded {} categories and {} products", categories.size, products.size)
    }

    private fun createCategories(categoryRepository: CategoryRepository): List<Category> {
        val definitions = listOf(
            Triple("Women", "Elevated essentials designed for everyday elegance.", 101),
            Triple("Men", "Refined staples with a modern, minimal sensibility.", 102),
            Triple("Home", "Thoughtful pieces that make every room feel considered.", 103),
            Triple("Accessories", "Finishing touches that complete the look.", 104),
            Triple("Footwear", "Comfort-first silhouettes in premium materials.", 105),
            Triple("Beauty", "Clean formulas for a polished daily ritual.", 106),
            Triple("Travel", "Compact companions for life on the move.", 107),
            Triple("Gifts", "Curated finds for every occasion.", 108),
        )

        return definitions.map { (name, description, seed) ->
            categoryRepository.save(
                Category(
                    name = name,
                    slug = slugify(name),
                    description = description,
                    imageUrl = "https://picsum.photos/seed/category-$seed/1200/800",
                ),
            )
        }
    }

    private fun buildProducts(categories: List<Category>): List<Product> {
        val adjectives = listOf(
            "Essential", "Classic", "Modern", "Premium", "Soft", "Lightweight",
            "Tailored", "Relaxed", "Heritage", "Studio", "Everyday", "Signature",
        )
        val materials = listOf(
            "Cashmere", "Organic Cotton", "Linen", "Merino Wool", "Silk",
            "Leather", "Bamboo", "Recycled Blend", "Italian Wool", "Suede",
        )
        val productTypes = mapOf(
            "Women" to listOf("Midi Dress", "Knit Sweater", "Wide-Leg Pant", "Silk Blouse", "Trench Coat", "Cardigan", "Wrap Skirt", "Tank Top", "Lounge Set", "Wool Coat"),
            "Men" to listOf("Oxford Shirt", "Chino Pant", "Crewneck Sweater", "Bomber Jacket", "Polo Shirt", "Denim Shirt", "Wool Blazer", "Jogger Pant", "Henley Tee", "Field Jacket"),
            "Home" to listOf("Throw Blanket", "Linen Sheet Set", "Ceramic Vase", "Scented Candle", "Wool Rug", "Table Lamp", "Bath Towel Set", "Pillow Cover", "Serving Bowl", "Diffuser"),
            "Accessories" to listOf("Leather Tote", "Crossbody Bag", "Cashmere Scarf", "Leather Belt", "Silk Bandana", "Weekender Bag", "Card Case", "Beanie", "Sunglasses", "Watch Strap"),
            "Footwear" to listOf("Leather Loafer", "Suede Sneaker", "Chelsea Boot", "Slide Sandal", "Running Shoe", "Ankle Boot", "Espadrille", "Driving Moccasin", "Hiking Boot", "Ballet Flat"),
            "Beauty" to listOf("Face Serum", "Body Lotion", "Lip Balm", "Hand Cream", "Cleansing Oil", "Hair Oil", "Eye Cream", "Body Wash", "Perfume Mist", "Night Cream"),
            "Travel" to listOf("Packing Cube Set", "Travel Pouch", "Carry-On Sleeve", "Passport Wallet", "Garment Bag", "Tech Organizer", "Travel Bottle Set", "Neck Pillow", "Luggage Tag", "Compression Bag"),
            "Gifts" to listOf("Gift Box", "Candle Trio", "Skincare Set", "Scarf Gift Set", "Coffee Mug Set", "Notebook Duo", "Tea Collection", "Bath Gift Set", "Wallet Gift Set", "Home Fragrance Kit"),
        )

        val products = mutableListOf<Product>()
        var index = 1

        categories.forEach { category ->
            val types = productTypes[category.name].orEmpty()
            repeat(12) { offset ->
                val type = types[offset % types.size]
                val adjective = adjectives[(index + offset) % adjectives.size]
                val material = materials[(index * 2 + offset) % materials.size]
                val name = "$adjective $material $type"
                val slug = "${slugify(name)}-$index"
                val price = BigDecimal.valueOf(Random.nextInt(2900, 24900) / 100.0)
                    .setScale(2, java.math.RoundingMode.HALF_UP)

                products += Product(
                    name = name,
                    slug = slug,
                    description = buildDescription(name, category.name, material),
                    price = price,
                    imageUrl = "https://picsum.photos/seed/product-$index/900/1200",
                    category = category,
                    stockQuantity = Random.nextInt(8, 120),
                    isActive = true,
                )
                index++
            }
        }

        while (products.size < 100) {
            val category = categories[products.size % categories.size]
            val type = productTypes[category.name]!!.random()
            val name = "Limited ${materials.random()} $type"
            products += Product(
                name = name,
                slug = "${slugify(name)}-$index",
                description = buildDescription(name, category.name, materials.random()),
                price = BigDecimal.valueOf(Random.nextInt(3500, 19900) / 100.0)
                    .setScale(2, java.math.RoundingMode.HALF_UP),
                imageUrl = "https://picsum.photos/seed/product-$index/900/1200",
                category = category,
                stockQuantity = Random.nextInt(5, 60),
                isActive = true,
            )
            index++
        }

        return products.take(100)
    }

    private fun buildDescription(name: String, category: String, material: String): String =
        "$name is part of our $category collection. Crafted from $material with a focus on quality, " +
            "comfort, and timeless design. A versatile piece made to elevate your everyday wardrobe."
}
