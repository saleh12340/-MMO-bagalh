package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    suspend fun getCustomerById(customerId: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun getCustomerByIdFlow(customerId: Long): Flow<CustomerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET currentBalance = :newBalance, updatedAt = :updatedAt WHERE id = :customerId")
    suspend fun updateBalance(customerId: Long, newBalance: BigDecimal, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("SELECT SUM(currentBalance) FROM customers WHERE currentBalance > 0 AND isArchived = 0")
    fun getTotalDebtFlow(): Flow<BigDecimal?>

    @Query("SELECT SUM(currentBalance) FROM customers WHERE currentBalance < 0 AND isArchived = 0")
    fun getTotalCreditFlow(): Flow<BigDecimal?>
}
