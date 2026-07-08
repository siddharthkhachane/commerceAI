package com.commerceai.order

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.Instant
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

    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<Order>

    fun countByStatus(status: com.commerceai.order.OrderStatus): Long

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = com.commerceai.order.OrderStatus.CONFIRMED")
    fun sumConfirmedRevenue(): BigDecimal

    @Query(
        """
        SELECT o FROM Order o
        WHERE o.status = com.commerceai.order.OrderStatus.CONFIRMED
        AND o.createdAt >= :since
        ORDER BY o.createdAt ASC
        """,
    )
    fun findConfirmedSince(@Param("since") since: Instant): List<Order>

    @Query(
        """
        SELECT o FROM Order o
        WHERE o.status = com.commerceai.order.OrderStatus.CONFIRMED
        AND o.createdAt >= :start AND o.createdAt < :end
        ORDER BY o.createdAt ASC
        """,
    )
    fun findConfirmedBetween(
        @Param("start") start: Instant,
        @Param("end") end: Instant,
    ): List<Order>
}
