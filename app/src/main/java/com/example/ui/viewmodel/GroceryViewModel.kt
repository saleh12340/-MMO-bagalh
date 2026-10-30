package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.dao.InvoiceWithCustomerAndItems
import com.example.data.local.entities.*
import com.example.utils.toSafeBigDecimal
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

class GroceryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val customerDao = db.customerDao()
    private val productDao = db.productDao()
    private val invoiceDao = db.invoiceDao()
    private val transactionDao = db.transactionDao()
    private val stockMovementDao = db.stockMovementDao()
    private val auditLogDao = db.auditLogDao()

    val customers: StateFlow<List<CustomerEntity>> = customerDao.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val products: StateFlow<List<ProductEntity>> = productDao.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val transactions: StateFlow<List<FinancialTransactionEntity>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalDebt: StateFlow<BigDecimal?> = customerDao.getTotalDebtFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, BigDecimal.ZERO)

    val totalCredit: StateFlow<BigDecimal?> = customerDao.getTotalCreditFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, BigDecimal.ZERO)

    val totalSales: StateFlow<BigDecimal?> = invoiceDao.getTotalSalesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, BigDecimal.ZERO)

    val totalCollected: StateFlow<BigDecimal?> = invoiceDao.getTotalCollectedFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, BigDecimal.ZERO)

    init {
        viewModelScope.launch {
            seedInitialDataIfEmpty()
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        val currentCusts = customerDao.getAllCustomers().firstOrNull()
        if (currentCusts.isNullOrEmpty()) {
            // Seed Customers from reference UI
            val c1Id = customerDao.insertCustomer(
                CustomerEntity(name = "أحمد محمد علي", phone = "775123456", currentBalance = BigDecimal("125000"), notes = "عميل مميز")
            )
            val c2Id = customerDao.insertCustomer(
                CustomerEntity(name = "محمد عبدالله", phone = "771234567", currentBalance = BigDecimal("-45000"), notes = null)
            )
            val c3Id = customerDao.insertCustomer(
                CustomerEntity(name = "فاطمة أحمد", phone = "777654321", currentBalance = BigDecimal("80500"), notes = null)
            )
            val c4Id = customerDao.insertCustomer(
                CustomerEntity(name = "سعيد راجح", phone = "770111222", currentBalance = BigDecimal("-12000"), notes = null)
            )
            val c5Id = customerDao.insertCustomer(
                CustomerEntity(name = "خالد علي", phone = "777000999", currentBalance = BigDecimal("67800"), notes = null)
            )
            val c6Id = customerDao.insertCustomer(
                CustomerEntity(name = "عبدالله محمد", phone = "774445566", currentBalance = BigDecimal("-3500"), notes = null)
            )

            // Seed Products from reference UI (Screen 6)
            productDao.insertProduct(ProductEntity(name = "أرز فاخر 10 كيلو", unit = "كيس", barcode = "1001", purchasePrice = BigDecimal("6500"), salePrice = BigDecimal("7500"), currentStock = BigDecimal("12"), minStockAlert = BigDecimal("2")))
            productDao.insertProduct(ProductEntity(name = "سكر 5 كيلو", unit = "كيس", barcode = "1002", purchasePrice = BigDecimal("6000"), salePrice = BigDecimal("7010"), currentStock = BigDecimal("5"), minStockAlert = BigDecimal("2")))
            productDao.insertProduct(ProductEntity(name = "زيت نباتي 1 لتر", unit = "حبة", barcode = "1003", purchasePrice = BigDecimal("5800"), salePrice = BigDecimal("6810"), currentStock = BigDecimal("2"), minStockAlert = BigDecimal("2")))
            productDao.insertProduct(ProductEntity(name = "دقيق 50 كيلو", unit = "شوال", barcode = "1004", purchasePrice = BigDecimal("21000"), salePrice = BigDecimal("24000"), currentStock = BigDecimal("0"), minStockAlert = BigDecimal("1")))
            productDao.insertProduct(ProductEntity(name = "شاي ناعم", unit = "علبة", barcode = "1005", purchasePrice = BigDecimal("1800"), salePrice = BigDecimal("2200"), currentStock = BigDecimal("3"), minStockAlert = BigDecimal("1")))
            productDao.insertProduct(ProductEntity(name = "معكرونة 1 كيلو", unit = "كيس", barcode = "1006", purchasePrice = BigDecimal("950"), salePrice = BigDecimal("1200"), currentStock = BigDecimal("1"), minStockAlert = BigDecimal("1")))
            productDao.insertProduct(ProductEntity(name = "حليب بودرة 1 كيلو", unit = "علبة", barcode = "1007", purchasePrice = BigDecimal("2900"), salePrice = BigDecimal("3500"), currentStock = BigDecimal("4"), minStockAlert = BigDecimal("1")))

            // Seed initial transactions for Ahmed Mohamed Ali (Screen 2)
            transactionDao.insertTransaction(FinancialTransactionEntity(
                customerId = c1Id,
                transactionType = "فاتورة رقم 0021",
                direction = "مدين (عليه)",
                amount = BigDecimal("67500"),
                balanceAfter = BigDecimal("125000"),
                description = "مبيعات مواد غذائية"
            ))
            transactionDao.insertTransaction(FinancialTransactionEntity(
                customerId = c1Id,
                transactionType = "دفع نقدي",
                direction = "دائن (له)",
                amount = BigDecimal("20000"),
                balanceAfter = BigDecimal("57500"),
                description = "سداد دفعة نقدية"
            ))
            transactionDao.insertTransaction(FinancialTransactionEntity(
                customerId = c1Id,
                transactionType = "فاتورة رقم 0018",
                direction = "مدين (عليه)",
                amount = BigDecimal("38000"),
                balanceAfter = BigDecimal("77500"),
                description = "مبيعات معلبات وزيوت"
            ))
            transactionDao.insertTransaction(FinancialTransactionEntity(
                customerId = c1Id,
                transactionType = "عملية بيع",
                direction = "مدين (عليه)",
                amount = BigDecimal("15500"),
                balanceAfter = BigDecimal("39500"),
                description = "شراء سكر وأرز"
            ))

            // Seed sample invoice 0021
            val invId = invoiceDao.insertInvoice(InvoiceEntity(
                invoiceNumber = "0021",
                customerId = c1Id,
                subtotal = BigDecimal("28130"),
                discountAmount = BigDecimal.ZERO,
                totalAmount = BigDecimal("28130"),
                paidAmount = BigDecimal("10000"),
                remainingAmount = BigDecimal("18130"),
                invoiceType = "آجلة",
                paymentStatus = "جزئية",
                notes = "فاتورة مواد تموينية"
            ))

            invoiceDao.insertItems(listOf(
                InvoiceItemEntity(invoiceId = invId, productId = 1, productName = "أرز فاخر 10 كيلو", quantity = BigDecimal("1"), unitPrice = BigDecimal("7500"), lineTotal = BigDecimal("7500")),
                InvoiceItemEntity(invoiceId = invId, productId = 2, productName = "سكر 5 كيلو", quantity = BigDecimal("1"), unitPrice = BigDecimal("7010"), lineTotal = BigDecimal("7010")),
                InvoiceItemEntity(invoiceId = invId, productId = 3, productName = "زيت نباتي 1 لتر", quantity = BigDecimal("2"), unitPrice = BigDecimal("6810"), lineTotal = BigDecimal("13620"))
            ))
        }
    }

    fun addCustomer(name: String, phone: String?, initialBalance: BigDecimal, isDebtor: Boolean, notes: String?) {
        viewModelScope.launch {
            db.withTransaction {
                val signedBalance = if (isDebtor) initialBalance else initialBalance.negate()
                val customer = CustomerEntity(name = name, phone = phone, currentBalance = signedBalance, notes = notes)
                val id = customerDao.insertCustomer(customer)
                if (signedBalance.compareTo(BigDecimal.ZERO) != 0) {
                    val tx = FinancialTransactionEntity(
                        customerId = id,
                        transactionType = "رصيد افتتاحي",
                        direction = if (isDebtor) "مدين (عليه)" else "دائن (له)",
                        amount = initialBalance,
                        balanceAfter = signedBalance,
                        description = "رصيد افتتاحي للحساب"
                    )
                    transactionDao.insertTransaction(tx)
                }
                auditLogDao.insertAuditLog(AuditLogEntity(actionType = "CREATE", entityName = "Customer", entityId = id.toString(), details = "إضافة عميل جديد: $name"))
            }
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            db.withTransaction {
                customerDao.deleteCustomer(customer)
                auditLogDao.insertAuditLog(AuditLogEntity(actionType = "DELETE", entityName = "Customer", entityId = customer.id.toString(), details = "حذف العميل: ${customer.name}"))
            }
        }
    }

    fun addProduct(barcode: String?, name: String, unit: String, purchasePrice: BigDecimal, salePrice: BigDecimal, initialStock: BigDecimal, minStock: BigDecimal, category: String?) {
        viewModelScope.launch {
            db.withTransaction {
                val product = ProductEntity(
                    barcode = barcode?.ifBlank { null },
                    name = name,
                    unit = unit,
                    purchasePrice = purchasePrice,
                    salePrice = salePrice,
                    currentStock = initialStock,
                    minStockAlert = minStock,
                    category = category
                )
                val id = productDao.insertProduct(product)
                if (initialStock.compareTo(BigDecimal.ZERO) != 0) {
                    stockMovementDao.insertMovement(StockMovementEntity(
                        productId = id,
                        movementType = "IN_PURCHASE",
                        quantityChange = initialStock,
                        stockAfter = initialStock,
                        notes = "رصيد افتتاحي للمخزون"
                    ))
                }
                auditLogDao.insertAuditLog(AuditLogEntity(actionType = "CREATE", entityName = "Product", entityId = id.toString(), details = "إضافة صنف: $name"))
            }
        }
    }

    /**
     * إنشاء فاتورة جديدة بشكل آمن ومتزامن.
     * كل الكتابات (الفاتورة، الأصناف، المخزون، رصيد العميل، السجل) تتم داخل معاملة واحدة:
     * إما تنجح جميعها أو يتم التراجع عنها جميعاً، فلا تتلف البيانات عند أي فشل.
     *
     * @return معرف الفاتورة الجديدة (أكبر من 0 عند النجاح).
     */
    suspend fun createInvoice(
        customerId: Long,
        items: List<Triple<ProductEntity?, String, BigDecimal>>, // Product, Qty, UnitPrice
        discount: BigDecimal,
        paidAmount: BigDecimal,
        invoiceType: String,
        notes: String?,
        customInvoiceNumber: String? = null
    ): Long = db.withTransaction {
        var subtotal = BigDecimal.ZERO
        val calculatedItems = items.map { (product, qtyStr, price) ->
            val qty = qtyStr.toSafeBigDecimal(BigDecimal.ONE)
            val lineTotal = price.multiply(qty)
            subtotal = subtotal.add(lineTotal)
            Triple(product, qty, Pair(price, lineTotal))
        }

        val totalAmount = subtotal.subtract(discount).max(BigDecimal.ZERO)
        val remainingAmount = totalAmount.subtract(paidAmount).max(BigDecimal.ZERO)
        val paymentStatus = when {
            remainingAmount.compareTo(BigDecimal.ZERO) == 0 -> "مدفوعة"
            paidAmount.compareTo(BigDecimal.ZERO) > 0 -> "جزئية"
            else -> "آجلة"
        }

        // ترقيم آمن اعتماداً على قاعدة البيانات وليس على حالة الواجهة، لتجنب تكرار الأرقام
        val invoiceNumber = customInvoiceNumber?.takeIf { it.isNotBlank() }
            ?: String.format("%04d", invoiceDao.getInvoiceCount() + 1)

        val invoice = InvoiceEntity(
            invoiceNumber = invoiceNumber,
            customerId = customerId,
            subtotal = subtotal,
            discountAmount = discount,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            remainingAmount = remainingAmount,
            invoiceType = invoiceType,
            paymentStatus = paymentStatus,
            notes = notes
        )
        val invoiceId = invoiceDao.insertInvoice(invoice)

        val itemEntities = calculatedItems.map { (product, qty, pair) ->
            val (price, lineTotal) = pair
            val prodId = product?.id
            val prodName = product?.name ?: "صنف غير مسجل"

            if (prodId != null) {
                val currentProd = productDao.getProductById(prodId)
                if (currentProd != null) {
                    val newStock = currentProd.currentStock.subtract(qty)
                    productDao.updateStock(prodId, newStock)
                    stockMovementDao.insertMovement(StockMovementEntity(
                        productId = prodId,
                        invoiceId = invoiceId,
                        movementType = "OUT_SALE",
                        quantityChange = qty.negate(),
                        stockAfter = newStock,
                        notes = "بيع فاتورة $invoiceNumber"
                    ))
                }
            }

            InvoiceItemEntity(
                invoiceId = invoiceId,
                productId = prodId,
                productName = prodName,
                quantity = qty,
                unitPrice = price,
                lineTotal = lineTotal
            )
        }
        invoiceDao.insertItems(itemEntities)

        val customer = customerDao.getCustomerById(customerId)
        if (customer != null && remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            val newBalance = customer.currentBalance.add(remainingAmount)
            customerDao.updateBalance(customerId, newBalance)
            transactionDao.insertTransaction(FinancialTransactionEntity(
                customerId = customerId,
                invoiceId = invoiceId,
                transactionType = "فاتورة رقم $invoiceNumber",
                direction = "مدين (عليه)",
                amount = remainingAmount,
                balanceAfter = newBalance,
                description = "فاتورة مبيعات رقم $invoiceNumber"
            ))
        }

        auditLogDao.insertAuditLog(AuditLogEntity(actionType = "CREATE", entityName = "Invoice", entityId = invoiceId.toString(), details = "إنشاء فاتورة رقم $invoiceNumber بقيمة $totalAmount"))

        invoiceId
    }

    fun addTransaction(customerId: Long, amount: BigDecimal, isDebtor: Boolean, transactionType: String, description: String?) {
        viewModelScope.launch {
            db.withTransaction {
                val customer = customerDao.getCustomerById(customerId) ?: return@withTransaction
                val signedAmount = if (isDebtor) amount else amount.negate()
                val newBalance = customer.currentBalance.add(signedAmount)
                customerDao.updateBalance(customerId, newBalance)
                transactionDao.insertTransaction(FinancialTransactionEntity(
                    customerId = customerId,
                    transactionType = transactionType,
                    direction = if (isDebtor) "عليه" else "لنا",
                    amount = amount,
                    balanceAfter = newBalance,
                    description = description
                ))
                auditLogDao.insertAuditLog(AuditLogEntity(actionType = "CREATE", entityName = "Transaction", entityId = customerId.toString(), details = "عملية مالية $transactionType بمبلغ $amount للعميل ${customer.name}"))
            }
        }
    }

    fun recordPayment(customerId: Long, amount: BigDecimal, paymentMethod: String, notes: String?) {
        viewModelScope.launch {
            db.withTransaction {
                val customer = customerDao.getCustomerById(customerId) ?: return@withTransaction
                val newBalance = customer.currentBalance.subtract(amount)
                customerDao.updateBalance(customerId, newBalance)
                transactionDao.insertTransaction(FinancialTransactionEntity(
                    customerId = customerId,
                    transactionType = "دفع نقدي",
                    direction = "لنا",
                    amount = amount,
                    balanceAfter = newBalance,
                    paymentMethod = paymentMethod,
                    description = notes ?: "سداد دفعة نقدية"
                ))
                auditLogDao.insertAuditLog(AuditLogEntity(actionType = "CREATE", entityName = "Payment", entityId = customerId.toString(), details = "تسجيل سداد بقيمة $amount للعميل ${customer.name}"))
            }
        }
    }

    suspend fun getInvoiceDetails(invoiceId: Long): InvoiceWithCustomerAndItems? {
        return invoiceDao.getInvoiceWithDetails(invoiceId)
    }
}
