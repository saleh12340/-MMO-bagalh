package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "financial_transactions")
data class FinancialTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val customerId: Long,
    val invoiceId: Long? = null,
    val transactionType: String, // بيع آجل, سداد نقدي, دفعة جزئية, دفعة كاملة, تسوية, خصم
    val direction: String, // مدين (عليه) / دائن (له)
    val amount: BigDecimal,
    val balanceAfter: BigDecimal,
    val paymentMethod: String = "كاش",
    val description: String?,
    val createdAt: Long = System.currentTimeMillis()
)
