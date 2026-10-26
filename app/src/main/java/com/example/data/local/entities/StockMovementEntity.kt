package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val productId: Long,
    val invoiceId: Long? = null,
    val movementType: String, // IN_PURCHASE, OUT_SALE, ADJUSTMENT, RETURN
    val quantityChange: BigDecimal,
    val stockAfter: BigDecimal,
    val notes: String?,
    val createdAt: Long = System.currentTimeMillis()
)
