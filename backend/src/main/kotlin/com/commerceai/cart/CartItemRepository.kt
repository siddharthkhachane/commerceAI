package com.commerceai.cart

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface CartItemRepository : JpaRepository<CartItem, Long> {

    @Query(
        """
        SELECT ci FROM CartItem ci
        JOIN FETCH ci.product p
        JOIN FETCH p.category
        WHERE ci.user.id = :userId
        ORDER BY ci.id ASC
        """,
    )
    fun findAllByUserId(@Param("userId") userId: Long): List<CartItem>

    fun findByUserIdAndProductId(userId: Long, productId: Long): Optional<CartItem>

    fun deleteByUserIdAndProductId(userId: Long, productId: Long)

    fun deleteAllByUserId(userId: Long)

    fun countByUserId(userId: Long): Long
}
