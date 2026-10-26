package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val barcode: String?,
    val name: String,
    val unit: String = "قطعة",
    val purchasePrice: BigDecimal = BigDecimal.ZERO,
    val salePrice: BigDecimal = BigDecimal.ZERO,
    val currentStock: BigDecimal = BigDecimal.ZERO,
    val minStockAlert: BigDecimal = BigDecimal.valueOf(5),
    val category: String? = "عام",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
