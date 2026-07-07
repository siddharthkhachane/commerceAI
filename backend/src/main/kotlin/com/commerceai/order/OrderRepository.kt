package com.commerceai.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface OrderRepository : JpaRepository<Order, Long> {

    @Query(
        """
        SELECT o FROM Order o
        LEFT JOIN FETCH o.items
        WHERE o.user.id = :userId
        ORDER BY o.createdAt DESC
        """,
    )
    fun findAllByUserId(@Param("userId") userId: Long): List<Order>

    @Query(
        """
        SELECT o FROM Order o
        LEFT JOIN FETCH o.items
        WHERE o.id = :orderId AND o.user.id = :userId
        """,
    )
    fun findByIdAndUserId(
        @Param("orderId") orderId: Long,
        @Param("userId") userId: Long,
    ): Optional<Order>
}
