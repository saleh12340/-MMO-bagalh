package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val invoiceNumber: String,
    val customerId: Long,
    val subtotal: BigDecimal,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal,
    val paidAmount: BigDecimal,
    val remainingAmount: BigDecimal,
    val invoiceType: String, // نقدية, آجلة, مختلطة
    val paymentStatus: String, // مدفوعة, جزئية, آجلة
    val isCancelled: Boolean = false,
    val notes: String?,
    val createdAt: Long = System.currentTimeMillis()
)
