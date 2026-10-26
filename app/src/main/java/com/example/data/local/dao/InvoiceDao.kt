package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.InvoiceEntity
import com.example.data.local.entities.InvoiceItemEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

data class InvoiceWithCustomerAndItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: com.example.data.local.entities.CustomerEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItemEntity>
)

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getInvoicesForCustomer(customerId: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    suspend fun getInvoiceById(invoiceId: Long): InvoiceEntity?

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    suspend fun getInvoiceWithDetails(invoiceId: Long): InvoiceWithCustomerAndItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InvoiceItemEntity>)

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: Long): List<InvoiceItemEntity>

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Query("UPDATE invoices SET isCancelled = 1 WHERE id = :invoiceId")
    suspend fun cancelInvoice(invoiceId: Long)

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE isCancelled = 0")
    fun getTotalSalesFlow(): Flow<BigDecimal?>

    @Query("SELECT SUM(paidAmount) FROM invoices WHERE isCancelled = 0")
    fun getTotalCollectedFlow(): Flow<BigDecimal?>
}
