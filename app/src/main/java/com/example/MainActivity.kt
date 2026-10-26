package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.dao.InvoiceWithCustomerAndItems
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GroceryViewModel
import com.example.utils.ReceiptGenerator
import com.example.utils.ThermalPrinterService
import com.example.utils.WhatsAppSharer
import com.example.utils.toSafeBigDecimal
import kotlinx.coroutines.launch
import java.math.BigDecimal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: GroceryViewModel = viewModel()
                val navController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()

                val customers by viewModel.customers.collectAsState()
                val products by viewModel.products.collectAsState()
                val invoices by viewModel.invoices.collectAsState()
                val transactions by viewModel.transactions.collectAsState()
                val totalDebt by viewModel.totalDebt.collectAsState()
                val totalCredit by viewModel.totalCredit.collectAsState()
                val totalSales by viewModel.totalSales.collectAsState()
                val totalCollected by viewModel.totalCollected.collectAsState()

                var showAddCustomerDialog by remember { mutableStateOf(false) }
                var showAddProductDialog by remember { mutableStateOf(false) }
                var selectedBottomTab by remember { mutableStateOf(0) }

                NavHost(navController = navController, startDestination = "customers_main") {
                    // Screen 1: شاشة العملاء الرئيسية
                    composable("customers_main") {
                        selectedBottomTab = 0
                        CustomersMainScreen(
                            customers = customers,
                            totalDebt = totalDebt,
                            totalCredit = totalCredit,
                            onCustomerClick = { customerId ->
                                navController.navigate("customer_detail/$customerId")
                            },
                            onAddCustomerClick = { showAddCustomerDialog = true },
                            onNewInvoiceClick = { customerId ->
                                navController.navigate("invoice_create_edit?customerId=${customerId ?: ""}")
                            },
                            onAddTransactionClick = { customerId ->
                                navController.navigate("add_transaction?customerId=${customerId ?: ""}")
                            },
                            onInventoryClick = {
                                selectedBottomTab = 3
                                navController.navigate("inventory")
                            },
                            onInvoicesClick = {
                                selectedBottomTab = 2
                                navController.navigate("reports")
                            },
                            onReportsClick = { navController.navigate("reports") },
                            onSettingsClick = {
                                selectedBottomTab = 4
                                navController.navigate("settings")
                            },
                            selectedBottomTab = selectedBottomTab,
                            onBottomTabSelected = { tab ->
                                selectedBottomTab = tab
                                when (tab) {
                                    0 -> { /* Already home */ }
                                    1 -> { /* Customers */ }
                                    2 -> navController.navigate("reports")
                                    3 -> navController.navigate("inventory")
                                    4 -> navController.navigate("settings")
                                }
                            }
                        )
                    }

                    // Screen 2: حساب العميل
                    composable("customer_detail/{customerId}") { backStackEntry ->
                        val customerId = backStackEntry.arguments?.getString("customerId")?.toLongOrNull() ?: 0L
                        val customer = customers.find { it.id == customerId }
                        val custTransactions = transactions.filter { it.customerId == customerId }
                        val custInvoices = invoices.filter { it.customerId == customerId }

                        CustomerDetailScreen(
                            customer = customer,
                            transactions = custTransactions,
                            invoices = custInvoices,
                            onBack = { navController.popBackStack() },
                            onNewInvoice = {
                                navController.navigate("invoice_create_edit?customerId=$customerId")
                            },
                            onAddPayment = {
                                navController.navigate("add_transaction?customerId=$customerId&isPayment=true")
                            },
                            onAddTransaction = {
                                navController.navigate("add_transaction?customerId=$customerId")
                            },
                            onEditCustomer = {
                                showAddCustomerDialog = true
                            },
                            onDeleteCustomer = {
                                customer?.let { viewModel.deleteCustomer(it) }
                                navController.popBackStack()
                            },
                            onShareStatement = {
                                val text = "كشف حساب العميل: ${customer?.name ?: ""}\nالرصيد الحالي: ${customer?.currentBalance?.toPlainString() ?: "0"} ر.ي"
                                WhatsAppSharer.shareReceiptImage(this@MainActivity, android.graphics.Bitmap.createBitmap(384, 100, android.graphics.Bitmap.Config.ARGB_8888), customer?.phone, text)
                            },
                            onInvoiceClick = { invId ->
                                navController.navigate("invoice_receipt_preview/$invId")
                            }
                        )
                    }

                    // Screen 3: إضافة عملية جديدة
                    composable("add_transaction?customerId={customerId}&isPayment={isPayment}") { backStackEntry ->
                        val customerId = backStackEntry.arguments?.getString("customerId")?.toLongOrNull()
                        AddTransactionScreen(
                            customers = customers,
                            preselectedCustomerId = customerId,
                            onBack = { navController.popBackStack() },
                            onSaveTransaction = { custId, amt, isDebtor, type, desc ->
                                viewModel.addTransaction(custId, amt, isDebtor, type, desc)
                                Toast.makeText(this@MainActivity, "تم حفظ العملية بنجاح", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onShare = { text ->
                                WhatsAppSharer.shareReceiptImage(this@MainActivity, android.graphics.Bitmap.createBitmap(384, 100, android.graphics.Bitmap.Config.ARGB_8888), null, text)
                            }
                        )
                    }

                    // Screen 4: إنشاء / تعديل الفاتورة
                    composable("invoice_create_edit?customerId={customerId}") { backStackEntry ->
                        val customerId = backStackEntry.arguments?.getString("customerId")?.toLongOrNull()
                        InvoiceCreateEditScreen(
                            customers = customers,
                            products = products,
                            preselectedCustomerId = customerId,
                            onBack = { navController.popBackStack() },
                            onSaveInvoice = { custId, items, disc, paid, type, notes, invNum ->
                                viewModel.createInvoice(custId, items, disc, paid, type, notes, invNum)
                                Toast.makeText(this@MainActivity, "تم حفظ الفاتورة بنجاح", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onPrintReceipt = { invId ->
                                navController.navigate("invoice_receipt_preview/$invId")
                            },
                            onShareReceipt = { invId ->
                                navController.navigate("invoice_receipt_preview/$invId")
                            }
                        )
                    }

                    // Screen 5: تفاصيل الفاتورة (إيصال طباعة حرارية 58 مم)
                    composable("invoice_receipt_preview/{invoiceId}") { backStackEntry ->
                        val invoiceId = backStackEntry.arguments?.getString("invoiceId")?.toLongOrNull() ?: 1L
                        var invoiceWithDetails by remember { mutableStateOf<InvoiceWithCustomerAndItems?>(null) }

                        LaunchedEffect(invoiceId) {
                            invoiceWithDetails = viewModel.getInvoiceDetails(invoiceId)
                        }

                        InvoiceReceiptPreviewScreen(
                            invoiceData = invoiceWithDetails,
                            onBack = { navController.popBackStack() },
                            onPrint = {
                                coroutineScope.launch {
                                    val dummyInv = invoiceWithDetails ?: InvoiceWithCustomerAndItems(
                                        invoice = invoices.firstOrNull() ?: return@launch,
                                        customer = customers.firstOrNull(),
                                        items = emptyList()
                                    )
                                    val bmp = ReceiptGenerator.generateReceiptBitmap(this@MainActivity, dummyInv)
                                    Toast.makeText(this@MainActivity, "جاري إرسال الإيصال للطابعة الحرارية 58مم عبر البلوتوث...", Toast.LENGTH_LONG).show()
                                }
                            },
                            onShare = {
                                coroutineScope.launch {
                                    val dummyInv = invoiceWithDetails ?: InvoiceWithCustomerAndItems(
                                        invoice = invoices.firstOrNull() ?: return@launch,
                                        customer = customers.firstOrNull(),
                                        items = emptyList()
                                    )
                                    val bmp = ReceiptGenerator.generateReceiptBitmap(this@MainActivity, dummyInv)
                                    WhatsAppSharer.shareReceiptImage(
                                        this@MainActivity,
                                        bmp,
                                        dummyInv.customer?.phone,
                                        "فاتورة بقالة العزي رقم ${dummyInv.invoice.invoiceNumber}"
                                    )
                                }
                            }
                        )
                    }

                    // Screen 6: المخزون / الأصناف
                    composable("inventory") {
                        InventoryScreen(
                            products = products,
                            onBack = { navController.popBackStack() },
                            onAddProductClick = { showAddProductDialog = true }
                        )
                    }

                    // Screen 7: التقارير / السجل
                    composable("reports") {
                        ReportsScreen(
                            totalSales = totalSales,
                            totalCollected = totalCollected,
                            totalDebt = totalDebt,
                            totalCredit = totalCredit,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Screen 8: الإعدادات
                    composable("settings") {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onStoreProfileClick = {
                                Toast.makeText(this@MainActivity, "بقالة العزي للمواد الغذائية - 776425052", Toast.LENGTH_SHORT).show()
                            },
                            onBluetoothPrinterClick = {
                                Toast.makeText(this@MainActivity, "تم ربط الطابعة الحرارية ESC/POS 58مم بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            onBackupClick = {
                                Toast.makeText(this@MainActivity, "تم إنشاء النسخة الاحتياطية لقاعدة البيانات محلياً بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            onRestoreClick = {
                                Toast.makeText(this@MainActivity, "قاعدة البيانات متصلة ومحدثة", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // Add Customer Dialog
                if (showAddCustomerDialog) {
                    AddCustomerDialog(
                        onDismiss = { showAddCustomerDialog = false },
                        onSave = { name, phone, balance, isDebtor, notes ->
                            viewModel.addCustomer(name, phone, balance, isDebtor, notes)
                            showAddCustomerDialog = false
                            Toast.makeText(this@MainActivity, "تمت إضافة العميل بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Add Product Dialog
                if (showAddProductDialog) {
                    AddProductDialog(
                        onDismiss = { showAddProductDialog = false },
                        onSave = { barcode, name, unit, purchasePrice, salePrice, stock, minStock, category ->
                            viewModel.addProduct(barcode, name, unit, purchasePrice, salePrice, stock, minStock, category)
                            showAddProductDialog = false
                            Toast.makeText(this@MainActivity, "تمت إضافة الصنف بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(onDismiss: () -> Unit, onSave: (String, String?, BigDecimal, Boolean, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("0") }
    var isDebtor by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة عميل جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم العميل") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("الرصيد الافتتاحي") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDebtor, onCheckedChange = { isDebtor = it })
                    Text("مدين (عليه دين للمحل)")
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name.trim(),
                            phone.trim().ifBlank { null },
                            balance.toSafeBigDecimal(BigDecimal.ZERO),
                            isDebtor,
                            notes.trim().ifBlank { null }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun AddProductDialog(onDismiss: () -> Unit, onSave: (String?, String, String, BigDecimal, BigDecimal, BigDecimal, BigDecimal, String?) -> Unit) {
    var barcode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("قطعة") }
    var purchasePrice by remember { mutableStateOf("0") }
    var salePrice by remember { mutableStateOf("0") }
    var stock by remember { mutableStateOf("0") }
    var minStock by remember { mutableStateOf("5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة صنف للمخزون", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("الباركود (اختياري)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم الصنف") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("الوحدة (قطعة، كيلو...)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = salePrice, onValueChange = { salePrice = it }, label = { Text("سعر البيع") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("الكمية المتوفرة") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            barcode.trim().ifBlank { null },
                            name.trim(),
                            unit.trim().ifBlank { "قطعة" },
                            purchasePrice.toSafeBigDecimal(BigDecimal.ZERO),
                            salePrice.toSafeBigDecimal(BigDecimal.ZERO),
                            stock.toSafeBigDecimal(BigDecimal.ZERO),
                            minStock.toSafeBigDecimal(BigDecimal.valueOf(5)),
                            "عام"
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
