package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.ProductEntity
import com.example.utils.toSafeBigDecimal
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreateScreen(
    customers: List<CustomerEntity>,
    products: List<ProductEntity>,
    preselectedCustomerId: Long?,
    onBack: () -> Unit,
    onSaveInvoice: (Long, List<Triple<ProductEntity?, String, BigDecimal>>, BigDecimal, BigDecimal, String, String?) -> Unit
) {
    var selectedCustomer by remember {
        mutableStateOf(customers.find { it.id == preselectedCustomerId } ?: customers.firstOrNull())
    }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var itemProductName by remember { mutableStateOf("") }
    var itemQty by remember { mutableStateOf("1") }
    var itemPrice by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val invoiceItems = remember { mutableStateListOf<Triple<ProductEntity?, String, BigDecimal>>() } // Product, Qty, Price

    var discount by remember { mutableStateOf("0") }
    var paidAmount by remember { mutableStateOf("0") }
    var invoiceType by remember { mutableStateOf("آجلة") }
    var notes by remember { mutableStateOf("") }

    val subtotal = invoiceItems.sumOf { (_, qty, price) ->
        val q = qty.toSafeBigDecimal(BigDecimal.ONE)
        price.multiply(q)
    }
    val disc = discount.toSafeBigDecimal(BigDecimal.ZERO)
    val total = subtotal.subtract(disc).max(BigDecimal.ZERO)
    val paid = paidAmount.toSafeBigDecimal(BigDecimal.ZERO)
    val remaining = total.subtract(paid).max(BigDecimal.ZERO)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إنشاء فاتورة جديدة", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الإجمالي: ${total.toPlainString()} ر.ي", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "المتبقي (دين): ${remaining.toPlainString()} ر.ي",
                            fontWeight = FontWeight.Bold,
                            color = if (remaining > BigDecimal.ZERO) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (selectedCustomer == null) {
                                errorMessage = "يرجى اختيار العميل أولاً"
                                return@Button
                            }
                            if (invoiceItems.isEmpty()) {
                                errorMessage = "يرجى إضافة صنف واحد على الأقل إلى الفاتورة"
                                return@Button
                            }
                            onSaveInvoice(selectedCustomer!!.id, invoiceItems, disc, paid, invoiceType, notes.ifBlank { null })
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ الفاتورة")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Error banner if any
            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                    }
                }
            }

            // Customer Selector
            ExposedDropdownMenuBox(
                expanded = customerDropdownExpanded,
                onExpandedChange = { customerDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCustomer?.name ?: "اختر العميل",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("العميل") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = customerDropdownExpanded,
                    onDismissRequest = { customerDropdownExpanded = false }
                ) {
                    customers.forEach { customer ->
                        DropdownMenuItem(
                            text = { Text(customer.name) },
                            onClick = {
                                selectedCustomer = customer
                                customerDropdownExpanded = false
                                errorMessage = null
                            }
                        )
                    }
                }
            }

            // Quick Product suggestions from inventory
            if (products.isNotEmpty()) {
                Text("أصناف من المخزون:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(products.take(10)) { prod ->
                        SuggestionChip(
                            onClick = {
                                itemProductName = prod.name
                                itemPrice = prod.salePrice.toPlainString()
                                errorMessage = null
                            },
                            label = { Text("${prod.name} (${prod.salePrice})") }
                        )
                    }
                }
            }

            // Add Item Row
            OutlinedTextField(
                value = itemProductName,
                onValueChange = { query ->
                    itemProductName = query
                    errorMessage = null
                    val matched = products.find { it.name.equals(query.trim(), true) || it.barcode == query.trim() }
                    if (matched != null && itemPrice.isBlank()) {
                        itemPrice = matched.salePrice.toPlainString()
                    }
                },
                label = { Text("اسم الصنف أو الباركود") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = itemQty,
                    onValueChange = {
                        itemQty = it
                        errorMessage = null
                    },
                    label = { Text("الكمية") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = itemPrice,
                    onValueChange = {
                        itemPrice = it
                        errorMessage = null
                    },
                    label = { Text("سعر الوحدة") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Button(
                    onClick = {
                        val parsedQty = itemQty.toSafeBigDecimal(BigDecimal.ONE)
                        var parsedPrice = itemPrice.toSafeBigDecimal(BigDecimal.ZERO)

                        val matchedProduct = products.find {
                            it.name.equals(itemProductName.trim(), ignoreCase = true) || it.barcode == itemProductName.trim()
                        }

                        if (parsedPrice.compareTo(BigDecimal.ZERO) == 0 && matchedProduct != null) {
                            parsedPrice = matchedProduct.salePrice
                        }

                        if (itemProductName.isBlank()) {
                            errorMessage = "يرجى كتابة اسم الصنف"
                            return@Button
                        }

                        if (parsedPrice.compareTo(BigDecimal.ZERO) <= 0) {
                            errorMessage = "يرجى إدخال سعر صحيح للصنف"
                            return@Button
                        }

                        invoiceItems.add(Triple(matchedProduct, parsedQty.toPlainString(), parsedPrice))
                        itemProductName = ""
                        itemPrice = ""
                        itemQty = "1"
                        errorMessage = null
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة")
                }
            }

            // Items list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(invoiceItems) { (product, qty, price) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product?.name ?: itemProductName, fontWeight = FontWeight.Bold)
                                val lineTot = price.multiply(qty.toSafeBigDecimal(BigDecimal.ONE))
                                Text("الكمية: $qty × السعر: $price = $lineTot ر.ي", fontSize = 12.sp)
                            }
                            IconButton(onClick = { invoiceItems.remove(Triple(product, qty, price)) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                            }
                        }
                    }
                }
            }

            // Payment and discount row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("الخصم") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = paidAmount,
                    onValueChange = { paidAmount = it },
                    label = { Text("المدفوع") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Button(
                    onClick = {
                        paidAmount = total.toPlainString()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("كامل")
                }
            }
        }
    }
}
